package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.AnalisisPiso;
import com.gestionap.model.AnalisisPiso.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class AnalisisPisoDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    // ── Lista completa de pisos con todas sus métricas ────────────

    public List<AnalisisPiso> listarTodos() throws SQLException {
        String sql = """
            SELECT
                p.id_piso,
                p.direccion,
                ci.nombre                                 AS ciudad,
                COUNT(DISTINCT h.id_habitacion)           AS total_habs,
                COALESCE(SUM(h.estado = 'alquilada'),   0) AS alquiladas,
                COALESCE(SUM(h.estado = 'disponible'),  0) AS disponibles,
                COALESCE(SUM(h.estado = 'mantenimiento'),0) AS mantenimiento,
                COALESCE(SUM(h.precio) * 12, 0)           AS precio_ref_max,
                COALESCE((
                    SELECT SUM(ct.precio_mensual)
                    FROM Contratos ct
                    WHERE ct.id_habitacion IN (
                        SELECT h2.id_habitacion FROM Habitaciones h2 WHERE h2.id_piso = p.id_piso
                    )
                    AND ct.fecha_inicio <= CURDATE() AND ct.fecha_fin >= CURDATE()
                ), 0)                                     AS ingresos_mes,
                COALESCE((
                    SELECT SUM(pg.cantidad)
                    FROM Pagos pg
                    JOIN Contratos ct ON pg.id_contrato = ct.id_contrato
                    WHERE ct.id_habitacion IN (
                        SELECT h2.id_habitacion FROM Habitaciones h2 WHERE h2.id_piso = p.id_piso
                    )
                ), 0)                                     AS total_cobrado,
                COALESCE((
                    SELECT COUNT(*)
                    FROM Contratos ct
                    WHERE ct.id_habitacion IN (
                        SELECT h2.id_habitacion FROM Habitaciones h2 WHERE h2.id_piso = p.id_piso
                    )
                    AND ct.fecha_inicio <= CURDATE() AND ct.fecha_fin >= CURDATE()
                ), 0)                                     AS contratos_activos,
                COALESCE((
                    SELECT COUNT(*)
                    FROM Contratos ct
                    WHERE ct.id_habitacion IN (
                        SELECT h2.id_habitacion FROM Habitaciones h2 WHERE h2.id_piso = p.id_piso
                    )
                ), 0)                                     AS contratos_total,
                COALESCE((
                    SELECT COUNT(*)
                    FROM Incidencias inc
                    WHERE inc.id_habitacion IN (
                        SELECT h2.id_habitacion FROM Habitaciones h2 WHERE h2.id_piso = p.id_piso
                    )
                ), 0)                                     AS total_incidencias
            FROM Pisos p
            JOIN Ciudades ci ON p.id_ciudad = ci.id_ciudad
            LEFT JOIN Habitaciones h ON h.id_piso = p.id_piso
            GROUP BY p.id_piso, p.direccion, ci.nombre
            ORDER BY ci.nombre, p.direccion
            """;

        List<AnalisisPiso> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AnalisisPiso a = new AnalisisPiso();
                a.setIdPiso(rs.getInt("id_piso"));
                a.setDireccion(rs.getString("direccion"));
                a.setCiudad(rs.getString("ciudad"));
                a.setTotalHabitaciones(rs.getInt("total_habs"));
                a.setAlquiladas(rs.getInt("alquiladas"));
                a.setDisponibles(rs.getInt("disponibles"));
                a.setMantenimiento(rs.getInt("mantenimiento"));
                a.setPrecioRefMax(rs.getBigDecimal("precio_ref_max"));
                a.setIngresosMes(rs.getBigDecimal("ingresos_mes"));
                a.setTotalCobrado(rs.getBigDecimal("total_cobrado"));
                a.setContratosActivos(rs.getInt("contratos_activos"));
                a.setContratosTotal(rs.getInt("contratos_total"));
                a.setTotalIncidencias(rs.getInt("total_incidencias"));
                a.setCosteIncidencias(queryCosteIncidencias(rs.getInt("id_piso")));
                a.setProximosVencimientos(queryProximosVencimientos(rs.getInt("id_piso")));
                a.calcularMetricas();
                lista.add(a);
            }
        }
        return lista;
    }

    // ── Resumen de toda la cartera ────────────────────────────────

    public Map<String, Object> resumenCartera() throws SQLException {
        String sql = """
            SELECT
                COUNT(DISTINCT p.id_piso)                AS total_pisos,
                COUNT(DISTINCT h.id_habitacion)          AS total_habs,
                COALESCE(SUM(h.estado='alquilada'),  0)  AS alquiladas,
                COALESCE(SUM(h.estado='disponible'), 0)  AS disponibles,
                COALESCE((
                    SELECT SUM(ct.precio_mensual) FROM Contratos ct
                    WHERE ct.fecha_inicio <= CURDATE() AND ct.fecha_fin >= CURDATE()
                ), 0)                                    AS ingresos_mes
            FROM Pisos p
            LEFT JOIN Habitaciones h ON h.id_piso = p.id_piso
            """;
        Map<String, Object> r = new LinkedHashMap<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                r.put("totalPisos",    rs.getInt("total_pisos"));
                r.put("totalHabs",     rs.getInt("total_habs"));
                r.put("alquiladas",    rs.getInt("alquiladas"));
                r.put("disponibles",   rs.getInt("disponibles"));
                r.put("ingresosMes",   rs.getBigDecimal("ingresos_mes"));
                int totalH = rs.getInt("total_habs");
                int alq    = rs.getInt("alquiladas");
                r.put("ocupacionMedia", totalH > 0 ? alq * 100.0 / totalH : 0.0);
            }
        }
        return r;
    }

    // ── Datos de detalle por piso ─────────────────────────────────

    public List<HabitacionDetalle> habitacionesDelPiso(int idPiso) throws SQLException {
        String sql = """
            SELECT h.id_habitacion, h.numero, h.precio, h.estado,
                   COALESCE(CONCAT(i.nombre, ' ', i.apellidos), '—') AS inquilino_actual,
                   ct.fecha_fin AS fin_contrato
            FROM Habitaciones h
            LEFT JOIN Contratos ct
                   ON ct.id_habitacion = h.id_habitacion
                  AND ct.fecha_inicio <= CURDATE() AND ct.fecha_fin >= CURDATE()
            LEFT JOIN Inquilinos i ON ct.id_inquilino = i.id_inquilino
            WHERE h.id_piso = ?
            ORDER BY h.numero
            """;
        List<HabitacionDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HabitacionDetalle d = new HabitacionDetalle();
                    d.idHabitacion    = rs.getInt("id_habitacion");
                    d.numero          = rs.getInt("numero");
                    d.precio          = rs.getBigDecimal("precio");
                    d.estado          = rs.getString("estado");
                    d.inquilinoActual = rs.getString("inquilino_actual");
                    java.sql.Date df = rs.getDate("fin_contrato");
                    d.finContrato = df != null ? df.toLocalDate() : null;
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public List<ContratoDetalle> contratosDelPiso(int idPiso) throws SQLException {
        String sql = """
            SELECT c.id_contrato, c.fecha_inicio, c.fecha_fin, c.precio_mensual,
                   CONCAT(i.nombre, ' ', i.apellidos) AS inquilino,
                   h.numero AS numero_hab
            FROM Contratos c
            JOIN Inquilinos   i ON c.id_inquilino  = i.id_inquilino
            JOIN Habitaciones h ON c.id_habitacion = h.id_habitacion
            WHERE h.id_piso = ?
            ORDER BY c.fecha_inicio DESC
            """;
        List<ContratoDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContratoDetalle d = new ContratoDetalle();
                    d.idContrato    = rs.getInt("id_contrato");
                    d.numeroHab     = rs.getInt("numero_hab");
                    d.inquilino     = rs.getString("inquilino");
                    java.sql.Date di = rs.getDate("fecha_inicio");
                    java.sql.Date df = rs.getDate("fecha_fin");
                    d.fechaInicio   = di != null ? di.toLocalDate() : LocalDate.now();
                    d.fechaFin      = df != null ? df.toLocalDate() : LocalDate.now();
                    d.precioMensual = rs.getBigDecimal("precio_mensual");
                    LocalDate hoy   = LocalDate.now();
                    d.activo = !d.fechaInicio.isAfter(hoy) && !d.fechaFin.isBefore(hoy);
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public List<PagoDetalle> pagosDelPiso(int idPiso) throws SQLException {
        String sql = """
            SELECT pg.fecha_pago, pg.cantidad, pg.metodo_pago,
                   CONCAT(i.nombre, ' ', i.apellidos) AS inquilino
            FROM Pagos pg
            JOIN Contratos    c  ON pg.id_contrato  = c.id_contrato
            JOIN Inquilinos   i  ON c.id_inquilino  = i.id_inquilino
            JOIN Habitaciones h  ON c.id_habitacion = h.id_habitacion
            WHERE h.id_piso = ?
            ORDER BY pg.fecha_pago DESC
            LIMIT 100
            """;
        List<PagoDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PagoDetalle d = new PagoDetalle();
                    java.sql.Date fp = rs.getDate("fecha_pago");
                    d.fechaPago   = fp != null ? fp.toLocalDate() : LocalDate.now();
                    d.cantidad    = rs.getBigDecimal("cantidad");
                    d.metodoPago  = rs.getString("metodo_pago");
                    d.inquilino   = rs.getString("inquilino");
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    public List<IncidenciaDetalle> incidenciasDelPiso(int idPiso) throws SQLException {
        String sql = """
            SELECT inc.id_incidencia, h.numero AS numero_hab,
                   CONCAT(i.nombre, ' ', i.apellidos) AS inquilino,
                   inc.descripcion, inc.estado, inc.fecha
            FROM Incidencias inc
            JOIN Habitaciones h  ON inc.id_habitacion = h.id_habitacion
            LEFT JOIN Inquilinos i ON inc.id_inquilino = i.id_inquilino
            WHERE h.id_piso = ?
            ORDER BY inc.fecha DESC
            """;
        List<IncidenciaDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IncidenciaDetalle d = new IncidenciaDetalle();
                    d.idIncidencia    = rs.getInt("id_incidencia");
                    d.numeroHab       = rs.getInt("numero_hab");
                    d.inquilino       = rs.getString("inquilino");
                    d.descripcion     = rs.getString("descripcion");
                    d.estado          = rs.getString("estado");
                    java.sql.Date fi = rs.getDate("fecha");
                    d.fecha           = fi != null ? fi.toLocalDate() : LocalDate.now();
                    d.costeReparacion = BigDecimal.ZERO;
                    lista.add(d);
                }
            }
        }
        // Intentar cargar coste_reparacion si la columna existe
        cargarCostesEnLista(idPiso, lista);
        return lista;
    }

    public Map<String, BigDecimal> ingresosPorMes(int idPiso) throws SQLException {
        String sql = """
            SELECT DATE_FORMAT(pg.fecha_pago,'%b %Y') AS mes,
                   YEAR(pg.fecha_pago)  AS anio,
                   MONTH(pg.fecha_pago) AS num_mes,
                   SUM(pg.cantidad)     AS total
            FROM Pagos pg
            JOIN Contratos    c ON pg.id_contrato  = c.id_contrato
            JOIN Habitaciones h ON c.id_habitacion = h.id_habitacion
            WHERE h.id_piso = ?
              AND pg.fecha_pago >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY anio, num_mes, mes
            ORDER BY anio, num_mes
            """;
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    resultado.put(rs.getString("mes"), rs.getBigDecimal("total"));
            }
        }
        return resultado;
    }

    // ── Helpers privados ──────────────────────────────────────────

    private BigDecimal queryCosteIncidencias(int idPiso) {
        try {
            String sql = """
                SELECT COALESCE(SUM(inc.coste_reparacion), 0)
                FROM Incidencias inc
                JOIN Habitaciones h ON inc.id_habitacion = h.id_habitacion
                WHERE h.id_piso = ?
                """;
            try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
                ps.setInt(1, idPiso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ignored) {
            // coste_reparacion column may not exist yet
        }
        return BigDecimal.ZERO;
    }

    private List<String> queryProximosVencimientos(int idPiso) {
        List<String> lista = new ArrayList<>();
        try {
            String sql = """
                SELECT CONCAT(i.nombre,' ',i.apellidos) AS nombre, c.fecha_fin
                FROM Contratos c
                JOIN Inquilinos   i ON c.id_inquilino  = i.id_inquilino
                JOIN Habitaciones h ON c.id_habitacion = h.id_habitacion
                WHERE h.id_piso = ?
                  AND c.fecha_fin BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
                  AND c.fecha_inicio <= CURDATE()
                ORDER BY c.fecha_fin ASC
                """;
            try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
                ps.setInt(1, idPiso);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        lista.add(rs.getString("nombre") + " → " + rs.getDate("fecha_fin"));
                }
            }
        } catch (SQLException ignored) {}
        return lista;
    }

    private void cargarCostesEnLista(int idPiso, List<IncidenciaDetalle> lista) {
        if (lista.isEmpty()) return;
        try {
            String sql = "SELECT id_incidencia, COALESCE(coste_reparacion,0) AS coste " +
                         "FROM Incidencias WHERE id_incidencia IN (" +
                         lista.stream().map(d -> String.valueOf(d.idIncidencia))
                              .collect(java.util.stream.Collectors.joining(",")) + ")";
            try (PreparedStatement ps = getConexion().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                Map<Integer, BigDecimal> costes = new HashMap<>();
                while (rs.next()) costes.put(rs.getInt(1), rs.getBigDecimal(2));
                for (IncidenciaDetalle d : lista)
                    d.costeReparacion = costes.getOrDefault(d.idIncidencia, BigDecimal.ZERO);
            }
        } catch (SQLException ignored) {
            // Column doesn't exist yet – keep ZERO
        }
    }
}
