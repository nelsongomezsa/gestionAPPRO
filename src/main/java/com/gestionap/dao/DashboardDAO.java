package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    private static final Map<String, String> MESES_ES = Map.ofEntries(
            Map.entry("Jan", "Ene"), Map.entry("Feb", "Feb"), Map.entry("Mar", "Mar"),
            Map.entry("Apr", "Abr"), Map.entry("May", "May"), Map.entry("Jun", "Jun"),
            Map.entry("Jul", "Jul"), Map.entry("Aug", "Ago"), Map.entry("Sep", "Sep"),
            Map.entry("Oct", "Oct"), Map.entry("Nov", "Nov"), Map.entry("Dec", "Dic")
    );

    private Map<String, BigDecimal> traducirMesesBD(Map<String, BigDecimal> mapa) {
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        mapa.forEach((clave, valor) -> {
            String[] partes = clave.split(" ", 2);
            String mesEs = MESES_ES.getOrDefault(partes[0], partes[0]);
            resultado.put(partes.length > 1 ? mesEs + " " + partes[1] : mesEs, valor);
        });
        return resultado;
    }

    private Map<String, Integer> traducirMesesInt(Map<String, Integer> mapa) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        mapa.forEach((clave, valor) -> {
            String[] partes = clave.split(" ", 2);
            String mesEs = MESES_ES.getOrDefault(partes[0], partes[0]);
            resultado.put(partes.length > 1 ? mesEs + " " + partes[1] : mesEs, valor);
        });
        return resultado;
    }

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public int contarHabitaciones() throws SQLException {
        return contarFilas("SELECT COUNT(*) FROM Habitaciones");
    }

    public int contarHabitacionesPorEstado(String estado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Habitaciones WHERE estado = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int contarInquilinos() throws SQLException {
        return contarFilas("SELECT COUNT(*) FROM Inquilinos");
    }

    public int contarContratosActivos() throws SQLException {
        return contarFilas(
            "SELECT COUNT(*) FROM Contratos " +
            "WHERE fecha_inicio <= CURDATE() AND fecha_fin >= CURDATE()"
        );
    }

    public int contarIncidenciasPendientes() throws SQLException {
        return contarFilas("SELECT COUNT(*) FROM Incidencias WHERE estado = 'pendiente'");
    }

    public int contarIncidenciasEnProceso() throws SQLException {
        return contarFilas("SELECT COUNT(*) FROM Incidencias WHERE estado = 'en_proceso'");
    }

    public BigDecimal totalIngresosMensuales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(precio_mensual), 0) FROM Contratos " +
                     "WHERE fecha_inicio <= CURDATE() AND fecha_fin >= CURDATE()";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> ingresosPorMes() throws SQLException {
        String sql =
            "SELECT DATE_FORMAT(fecha_pago, '%b %Y') AS mes, " +
            "       YEAR(fecha_pago) AS anio, MONTH(fecha_pago) AS num_mes, " +
            "       SUM(cantidad) AS total " +
            "FROM Pagos " +
            "WHERE fecha_pago >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY anio, num_mes, mes " +
            "ORDER BY anio, num_mes";
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.put(rs.getString("mes"), rs.getBigDecimal("total"));
            }
        }
        return traducirMesesBD(resultado);
    }

    public Map<String, BigDecimal> ingresosPorMes12() throws SQLException {
        String sql =
            "SELECT DATE_FORMAT(fecha_pago, '%b %Y') AS mes, " +
            "       YEAR(fecha_pago) AS anio, MONTH(fecha_pago) AS num_mes, " +
            "       SUM(cantidad) AS total " +
            "FROM Pagos " +
            "WHERE fecha_pago >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY anio, num_mes, mes ORDER BY anio, num_mes";
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.put(rs.getString("mes"), rs.getBigDecimal("total"));
        }
        return traducirMesesBD(resultado);
    }

    public BigDecimal ingresosTotalAnio(int anio) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM Pagos WHERE YEAR(fecha_pago) = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    public Map<String, Integer> incidenciasPorMes() throws SQLException {
        String sql =
            "SELECT DATE_FORMAT(fecha, '%b %Y') AS mes, " +
            "       YEAR(fecha) AS anio, MONTH(fecha) AS num_mes, COUNT(*) AS total " +
            "FROM Incidencias " +
            "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY anio, num_mes, mes ORDER BY anio, num_mes";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.put(rs.getString("mes"), rs.getInt("total"));
        }
        return traducirMesesInt(resultado);
    }

    public Map<String, Integer> habitacionesPorEstado() throws SQLException {
        String sql = "SELECT estado, COUNT(*) AS cnt FROM Habitaciones GROUP BY estado";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.put(rs.getString("estado"), rs.getInt("cnt"));
        }
        return resultado;
    }

    public double tasaOcupacion() throws SQLException {
        int total = contarHabitaciones();
        if (total == 0) return 0;
        return (contarHabitacionesPorEstado("alquilada") * 100.0) / total;
    }

    private int contarFilas(String sql) throws SQLException {
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
