package com.gestionap.dao;

import com.gestionap.model.Habitacion.Estado;
import com.gestionap.model.Notificacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NOTA sobre cobertura: generarContratosVencen(), generarIncidenciasAntiguas()
 * y generarMantenimientoLargo() usan DATE_ADD()/DATE_SUB() con sintaxis
 * MySQL — H2 no tiene esas funciones en NINGÚN modo (confirmado en la
 * documentación oficial de H2; el equivalente H2 es DATEADD(campo, n,
 * fecha), sintaxis distinta). No se tocó la query de producción para
 * acomodar el test — es correcta contra MySQL real. Como consecuencia, esos
 * 3 generadores no tienen cobertura automatizada de su lógica de fecha
 * aquí; solo generarPagosPendientes() (que usa CURDATE/MONTH/YEAR,
 * portables) se prueba de punta a punta.
 *
 * Esto no deja sin probar el fix que más importa de esta sesión (commit
 * 4e4e11f: un generador que falla no debe bloquear a los demás) — lo
 * probamos igual: rompemos deliberadamente Incidencias Y aprovechamos que
 * los otros 2 generadores con DATE_ADD/DATE_SUB también van a fallar por la
 * limitación de H2, y confirmamos que aun así, con 3 de 4 generadores
 * fallando por razones distintas, el cuarto (pagos pendientes) sigue
 * funcionando y generarNotificaciones() no lanza excepción.
 */
class NotificacionDAOTest {

    private Connection conexion;
    private NotificacionDAO dao;
    private int idUsuario;
    private int idInquilino;
    private int idPiso;

    @BeforeEach
    void setUp() throws Exception {
        conexion = DaoTestSupport.nuevaBaseDeTest();
        dao = new NotificacionDAO();

        idUsuario = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Usuarios (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, ?, 1)",
                "Admin", "admin@example.com", "hash", "admin");
        idInquilino = DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Inquilinos (nombre, apellidos, dni) VALUES (?, ?, ?)",
                "Ana", "García", "12345678A");
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

    private int crearHabitacion(Estado estado) throws SQLException {
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES (?, ?, ?, ?)",
                1, new BigDecimal("450.00"), estado.name(), idPiso);
    }

    private int crearContrato(int idHabitacion, LocalDate inicio, LocalDate fin) throws SQLException {
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Contratos (id_habitacion, id_inquilino, fecha_inicio, fecha_fin, precio_mensual) VALUES (?, ?, ?, ?, ?)",
                idHabitacion, idInquilino, inicio, fin, new BigDecimal("450.00"));
    }

    private List<Notificacion> notificaciones() throws SQLException {
        return dao.listarTodas(idUsuario);
    }

    private boolean hayNotificacionDeTipo(String tipo) throws SQLException {
        return notificaciones().stream().anyMatch(n -> n.getTipo().equals(tipo));
    }

    // ── generarPagosPendientes() — el único de los 4 portable a H2 ──

    @Test
    void generarNotificaciones_contratoActivoSinPagoEsteMes_generaAviso() throws SQLException {
        int idHabitacion = crearHabitacion(Estado.alquilada);
        crearContrato(idHabitacion, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4));

        dao.generarNotificaciones(idUsuario);

        assertTrue(hayNotificacionDeTipo("pago_pendiente"));
    }

    @Test
    void generarNotificaciones_contratoConPagoEsteMes_noGeneraAviso() throws SQLException {
        int idHabitacion = crearHabitacion(Estado.alquilada);
        int idContrato = crearContrato(idHabitacion, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4));
        DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES (?, ?, ?, ?)",
                idContrato, new BigDecimal("450.00"), "transferencia", LocalDate.now());

        dao.generarNotificaciones(idUsuario);

        assertFalse(hayNotificacionDeTipo("pago_pendiente"));
    }

    @Test
    void generarNotificaciones_contratoInactivo_noGeneraAvisoDePago() throws SQLException {
        int idHabitacion = crearHabitacion(Estado.disponible);
        crearContrato(idHabitacion, LocalDate.now().minusMonths(6), LocalDate.now().minusDays(1)); // ya vencido

        dao.generarNotificaciones(idUsuario);

        assertFalse(hayNotificacionDeTipo("pago_pendiente"));
    }

    @Test
    void generarNotificaciones_esIdempotente_noDuplicaElMismoDia() throws SQLException {
        int idHabitacion = crearHabitacion(Estado.alquilada);
        crearContrato(idHabitacion, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4));

        dao.generarNotificaciones(idUsuario);
        dao.generarNotificaciones(idUsuario);

        long cuantas = notificaciones().stream().filter(n -> n.getTipo().equals("pago_pendiente")).count();
        assertEquals(1, cuantas, "No debe duplicar la misma notificación el mismo día");
    }

    @Test
    void generarNotificaciones_generadoresRotos_noBloqueanAlQueSiFunciona() throws SQLException {
        int idHabitacion = crearHabitacion(Estado.alquilada);
        crearContrato(idHabitacion, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4));

        // Rompe deliberadamente generarIncidenciasAntiguas (además de los 2
        // que ya fallan por DATE_ADD/DATE_SUB en H2 — ver nota de clase).
        try (Statement st = conexion.createStatement()) {
            st.execute("DROP TABLE Incidencias");
        }

        assertDoesNotThrow(() -> dao.generarNotificaciones(idUsuario),
                "3 de 4 generadores fallando no debe propagar la excepción al llamador");

        assertTrue(hayNotificacionDeTipo("pago_pendiente"),
                "El generador que sí funciona no debe verse afectado por los otros 3 fallando");
    }

    // ── Consultas / marcado de leídas (no dependen de DATE_ADD/DATE_SUB) ──

    @Test
    void listarNoLeidas_soloDevuelveLasNoLeidas() throws SQLException {
        int id1 = insertarNotificacionDirecta("tipo_a", "ref1");
        insertarNotificacionDirecta("tipo_b", "ref2");
        dao.marcarLeida(id1);

        List<Notificacion> noLeidas = dao.listarNoLeidas(idUsuario);

        assertEquals(1, noLeidas.size());
        assertEquals("tipo_b", noLeidas.get(0).getTipo());
    }

    @Test
    void contarNoLeidas_cuentaCorrectamente() throws SQLException {
        insertarNotificacionDirecta("tipo_a", "ref1");
        insertarNotificacionDirecta("tipo_b", "ref2");

        assertEquals(2, dao.contarNoLeidas(idUsuario));
    }

    @Test
    void marcarTodasLeidas_marcaTodasComoLeidas() throws SQLException {
        insertarNotificacionDirecta("tipo_a", "ref1");
        insertarNotificacionDirecta("tipo_b", "ref2");

        dao.marcarTodasLeidas(idUsuario);

        assertEquals(0, dao.contarNoLeidas(idUsuario));
    }

    private int insertarNotificacionDirecta(String tipo, String referencia) throws SQLException {
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Notificaciones (tipo, titulo, descripcion, referencia, id_usuario) VALUES (?, ?, ?, ?, ?)",
                tipo, "Título", "Descripción", referencia, idUsuario);
    }
}
