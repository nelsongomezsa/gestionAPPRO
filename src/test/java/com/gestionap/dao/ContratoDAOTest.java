package com.gestionap.dao;

import com.gestionap.model.Contrato;
import com.gestionap.model.Habitacion.Estado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cubre en particular crearConHabitacion() / finalizarYLiberarHabitacion(),
 * el fix de esta sesión para la condición de carrera de doble alquiler
 * (commit 3731e23): ambas operaciones deben ser atómicas vía
 * DatabaseConnection.ejecutarEnTransaccion(), y crearConHabitacion() no debe
 * crear el contrato si la habitación ya no está disponible.
 */
class ContratoDAOTest {

    private Connection conexion;
    private ContratoDAO contratoDAO;
    private HabitacionDAO habitacionDAO;
    private int idInquilino;

    @BeforeEach
    void setUp() throws Exception {
        conexion = DaoTestSupport.nuevaBaseDeTest();
        contratoDAO = new ContratoDAO();
        habitacionDAO = new HabitacionDAO();

        idInquilino = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Inquilinos (nombre, apellidos, dni, telefono, email) VALUES (?, ?, ?, ?, ?)",
                "Ana", "García", "12345678A", "600000000", "ana@example.com");
    }

    @AfterEach
    void tearDown() throws SQLException {
        conexion.close();
    }

    private int crearHabitacionDisponible() throws SQLException {
        int idCiudad = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Ciudades (nombre) VALUES (?)", "Madrid");
        int idPiso = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Pisos (direccion, numero_habitaciones, id_ciudad) VALUES (?, ?, ?)",
                "Calle Falsa 123", 3, idCiudad);
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES (?, ?, ?, ?)",
                1, new BigDecimal("450.00"), Estado.disponible.name(), idPiso);
    }

    private int contarContratos() throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT COUNT(*) FROM Contratos");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Estado leerEstadoHabitacion(int id) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT estado FROM Habitaciones WHERE id_habitacion = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return Estado.valueOf(rs.getString(1));
            }
        }
    }

    private Contrato nuevoContrato(int idHabitacion) {
        Contrato c = new Contrato();
        c.setIdHabitacion(idHabitacion);
        c.setIdInquilino(idInquilino);
        c.setFechaInicio(LocalDate.now());
        c.setFechaFin(LocalDate.now().plusMonths(6));
        c.setPrecioMensual(new BigDecimal("450.00"));
        return c;
    }

    @Test
    void crearConHabitacion_habitacionDisponible_creaContratoYMarcaAlquilada() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();

        int idContrato = contratoDAO.crearConHabitacion(nuevoContrato(idHabitacion));

        assertTrue(idContrato > 0);
        assertEquals(1, contarContratos());
        assertEquals(Estado.alquilada, leerEstadoHabitacion(idHabitacion));
    }

    @Test
    void crearConHabitacion_habitacionYaAlquilada_noCreaContratoYLanzaExcepcion() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();
        habitacionDAO.reservar(idHabitacion); // ya alquilada por "otro usuario"

        Contrato c = nuevoContrato(idHabitacion);
        SQLException ex = assertThrows(SQLException.class, () -> contratoDAO.crearConHabitacion(c));

        assertTrue(ex.getMessage().contains("ya no está disponible"));
        assertEquals(0, contarContratos(), "No debe haberse creado el contrato (todo o nada)");
        assertEquals(Estado.alquilada, leerEstadoHabitacion(idHabitacion));
    }

    @Test
    void finalizarYLiberarHabitacion_finalizaContratoYLiberaHabitacion() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();
        int idContrato = contratoDAO.crearConHabitacion(nuevoContrato(idHabitacion));

        contratoDAO.finalizarYLiberarHabitacion(idContrato, idHabitacion);

        Contrato finalizado = contratoDAO.buscarPorId(idContrato);
        assertEquals(LocalDate.now(), finalizado.getFechaFin());
        assertEquals(Estado.disponible, leerEstadoHabitacion(idHabitacion));
    }

    @Test
    void insertarYBuscarPorId_devuelveDatosConJoins() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();
        Contrato original = nuevoContrato(idHabitacion);

        int id = contratoDAO.insertar(original);
        Contrato cargado = contratoDAO.buscarPorId(id);

        assertNotNull(cargado);
        assertEquals("Ana García", cargado.getNombreInquilino());
        assertEquals("12345678A", cargado.getDniInquilino());
        assertEquals(1, cargado.getNumeroHabitacion());
        assertEquals("Calle Falsa 123", cargado.getDireccionPiso());
        assertEquals(0, new BigDecimal("450.00").compareTo(cargado.getPrecioMensual()));
    }

    @Test
    void listarActivos_soloIncluyeContratosVigentesHoy() throws SQLException {
        int idH1 = crearHabitacionDisponible();
        int idH2 = crearHabitacionDisponible();

        Contrato vigente = nuevoContrato(idH1);
        vigente.setFechaInicio(LocalDate.now().minusMonths(1));
        vigente.setFechaFin(LocalDate.now().plusMonths(1));
        contratoDAO.insertar(vigente);

        Contrato vencido = nuevoContrato(idH2);
        vencido.setFechaInicio(LocalDate.now().minusMonths(6));
        vencido.setFechaFin(LocalDate.now().minusDays(1));
        contratoDAO.insertar(vencido);

        List<Contrato> activos = contratoDAO.listarActivos();

        assertEquals(1, activos.size());
        assertEquals(idH1, activos.get(0).getIdHabitacion());
    }

    @Test
    void finalizarContrato_estableceFechaFinHoy() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();
        int id = contratoDAO.insertar(nuevoContrato(idHabitacion));

        boolean ok = contratoDAO.finalizarContrato(id);

        assertTrue(ok);
        assertEquals(LocalDate.now(), contratoDAO.buscarPorId(id).getFechaFin());
    }

    @Test
    void eliminar_contratoExistente_loBorra() throws SQLException {
        int idHabitacion = crearHabitacionDisponible();
        int id = contratoDAO.insertar(nuevoContrato(idHabitacion));

        assertTrue(contratoDAO.eliminar(id));
        assertNull(contratoDAO.buscarPorId(id));
    }
}
