package com.gestionap.dao;

import com.gestionap.model.Habitacion;
import com.gestionap.model.Habitacion.Estado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NOTA sobre cobertura: buscarPorId() y listarTodas() incluyen
 * DATEDIFF(CURDATE(), ct.fecha_inicio) para "dias_alquilada", y H2 solo
 * soporta la forma ANSI de 3 argumentos de DATEDIFF (datepart, a, b) — la
 * forma MySQL de 2 argumentos que usa esta query no tiene equivalente en
 * ningún modo de H2 (confirmado en la documentación oficial de H2). No se
 * tocó la query de producción para acomodar el test — es correcta contra
 * MySQL real. Como consecuencia, buscarPorId()/listarTodas() no tienen
 * cobertura automatizada aquí; el resto de HabitacionDAO sí. Verificamos
 * estado directamente por SQL o vía listarDisponibles() (que no usa
 * DATEDIFF) en su lugar.
 */
class HabitacionDAOTest {

    private Connection conexion;
    private HabitacionDAO dao;
    private int idPiso;

    @BeforeEach
    void setUp() throws Exception {
        conexion = DaoTestSupport.nuevaBaseDeTest();
        dao = new HabitacionDAO();

        int idCiudad = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Ciudades (nombre) VALUES (?)", "Madrid");
        idPiso = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Pisos (direccion, numero_habitaciones, id_ciudad) VALUES (?, ?, ?)",
                "Calle Falsa 123", 3, idCiudad);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conexion.close();
    }

    private int crearHabitacion(int numero, Estado estado) throws SQLException {
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES (?, ?, ?, ?)",
                numero, new BigDecimal("450.00"), estado.name(), idPiso);
    }

    private Estado leerEstado(int idHabitacion) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT estado FROM Habitaciones WHERE id_habitacion = ?")) {
            ps.setInt(1, idHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Habitación " + idHabitacion + " no encontrada");
                return Estado.valueOf(rs.getString(1));
            }
        }
    }

    private boolean existeHabitacion(int idHabitacion) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT 1 FROM Habitaciones WHERE id_habitacion = ?")) {
            ps.setInt(1, idHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Optional<Habitacion> buscarEnDisponibles(int id, List<Habitacion> lista) {
        return lista.stream().filter(h -> h.getIdHabitacion() == id).findFirst();
    }

    @Test
    void insertar_seReflejaEnListarDisponiblesConLosMismosDatos() throws SQLException {
        Habitacion h = new Habitacion();
        h.setNumero(7);
        h.setPrecio(new BigDecimal("500.00"));
        h.setEstado(Estado.disponible);
        h.setIdPiso(idPiso);

        int id = dao.insertar(h);
        Habitacion cargada = buscarEnDisponibles(id, dao.listarDisponibles())
                .orElseThrow(() -> new AssertionError("No apareció en listarDisponibles()"));

        assertEquals(7, cargada.getNumero());
        assertEquals(0, new BigDecimal("500.00").compareTo(cargada.getPrecio()));
        assertEquals(Estado.disponible, cargada.getEstado());
        assertEquals("Calle Falsa 123", cargada.getDireccionPiso());
        assertEquals("Madrid", cargada.getNombreCiudad());
    }

    @Test
    void listarDisponibles_soloIncluyeLasDisponibles() throws SQLException {
        crearHabitacion(1, Estado.disponible);
        crearHabitacion(2, Estado.alquilada);
        crearHabitacion(3, Estado.mantenimiento);
        crearHabitacion(4, Estado.disponible);

        List<Habitacion> disponibles = dao.listarDisponibles();

        assertEquals(2, disponibles.size());
        assertTrue(disponibles.stream().allMatch(h -> h.getEstado() == Estado.disponible));
    }

    @Test
    void reservar_habitacionDisponible_pasaAAlquiladaYDevuelveTrue() throws SQLException {
        int id = crearHabitacion(1, Estado.disponible);

        boolean ok = dao.reservar(id);

        assertTrue(ok);
        assertEquals(Estado.alquilada, leerEstado(id));
        assertTrue(buscarEnDisponibles(id, dao.listarDisponibles()).isEmpty(),
                "Ya no debería aparecer como disponible");
    }

    @Test
    void reservar_habitacionYaAlquilada_devuelveFalseYNoCambiaNada() throws SQLException {
        int id = crearHabitacion(1, Estado.alquilada);

        boolean ok = dao.reservar(id);

        assertFalse(ok, "No debe poder reservarse una habitación que ya no está disponible");
        assertEquals(Estado.alquilada, leerEstado(id), "El estado no debe alterarse en el intento fallido");
    }

    @Test
    void reservar_habitacionEnMantenimiento_devuelveFalse() throws SQLException {
        int id = crearHabitacion(1, Estado.mantenimiento);

        assertFalse(dao.reservar(id));
        assertEquals(Estado.mantenimiento, leerEstado(id));
    }

    @Test
    void liberar_habitacionAlquilada_pasaADisponibleYDevuelveTrue() throws SQLException {
        int id = crearHabitacion(1, Estado.alquilada);

        boolean ok = dao.liberar(id);

        assertTrue(ok);
        assertEquals(Estado.disponible, leerEstado(id));
        assertTrue(buscarEnDisponibles(id, dao.listarDisponibles()).isPresent());
    }

    @Test
    void liberar_habitacionYaDisponible_devuelveFalse() throws SQLException {
        int id = crearHabitacion(1, Estado.disponible);

        assertFalse(dao.liberar(id), "No debe 'liberar' una habitación que ya estaba disponible");
    }

    @Test
    void actualizarEstado_cambioIncondicional_siempreFunciona() throws SQLException {
        int id = crearHabitacion(1, Estado.disponible);

        boolean ok = dao.actualizarEstado(id, Estado.mantenimiento);

        assertTrue(ok);
        assertEquals(Estado.mantenimiento, leerEstado(id));
    }

    @Test
    void eliminar_habitacionExistente_laBorraYDevuelveTrue() throws SQLException {
        int id = crearHabitacion(1, Estado.disponible);

        boolean ok = dao.eliminar(id);

        assertTrue(ok);
        assertFalse(existeHabitacion(id));
    }

    @Test
    void eliminar_habitacionInexistente_devuelveFalse() throws SQLException {
        assertFalse(dao.eliminar(999999));
    }
}
