package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Incidencia;
import com.gestionap.model.Incidencia.Estado;
import com.gestionap.model.Incidencia.Prioridad;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class IncidenciaDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public List<Incidencia> listarTodas() throws SQLException {
        String sql = """
                SELECT inc.id_incidencia, inc.id_habitacion, inc.id_inquilino,
                       inc.descripcion, inc.estado, inc.fecha,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino,
                       h.numero AS numero_habitacion
                FROM Incidencias inc
                JOIN Inquilinos   i ON inc.id_inquilino  = i.id_inquilino
                JOIN Habitaciones h ON inc.id_habitacion = h.id_habitacion
                ORDER BY inc.fecha DESC
                """;
        List<Incidencia> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        cargarCostesEnLista(lista);
        return lista;
    }

    public List<Incidencia> listarPendientes() throws SQLException {
        String sql = """
                SELECT inc.id_incidencia, inc.id_habitacion, inc.id_inquilino,
                       inc.descripcion, inc.estado, inc.fecha,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino,
                       h.numero AS numero_habitacion
                FROM Incidencias inc
                JOIN Inquilinos   i ON inc.id_inquilino  = i.id_inquilino
                JOIN Habitaciones h ON inc.id_habitacion = h.id_habitacion
                WHERE inc.estado = 'pendiente'
                ORDER BY inc.fecha ASC
                """;
        List<Incidencia> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        cargarCostesEnLista(lista);
        return lista;
    }

    public Incidencia buscarPorId(int idIncidencia) throws SQLException {
        String sql = """
                SELECT inc.id_incidencia, inc.id_habitacion, inc.id_inquilino,
                       inc.descripcion, inc.estado, inc.fecha,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino,
                       h.numero AS numero_habitacion
                FROM Incidencias inc
                JOIN Inquilinos   i ON inc.id_inquilino  = i.id_inquilino
                JOIN Habitaciones h ON inc.id_habitacion = h.id_habitacion
                WHERE inc.id_incidencia = ?
                """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Incidencia inc = mapearFila(rs);
                    inc.setCosteReparacion(cargarCosteIndividual(idIncidencia));
                    return inc;
                }
            }
        }
        return null;
    }

    public int insertar(Incidencia inc) throws SQLException {
        String sql = "INSERT INTO Incidencias (id_habitacion, id_inquilino, descripcion, estado, fecha) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inc.getIdHabitacion());
            ps.setInt(2, inc.getIdInquilino());
            ps.setString(3, inc.getDescripcion());
            ps.setString(4, inc.getEstado().name());
            ps.setDate(5, java.sql.Date.valueOf(inc.getFecha()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para la incidencia.");
    }

    public boolean actualizarEstado(int idIncidencia, Estado nuevoEstado) throws SQLException {
        String sql = "UPDATE Incidencias SET estado = ? WHERE id_incidencia = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idIncidencia);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idIncidencia) throws SQLException {
        String sql = "DELETE FROM Incidencias WHERE id_incidencia = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            return ps.executeUpdate() > 0;
        }
    }

    private Incidencia mapearFila(ResultSet rs) throws SQLException {
        Incidencia inc = new Incidencia();
        inc.setIdIncidencia(rs.getInt("id_incidencia"));
        inc.setIdHabitacion(rs.getInt("id_habitacion"));
        inc.setIdInquilino(rs.getInt("id_inquilino"));
        inc.setDescripcion(rs.getString("descripcion"));
        Estado estado = Estado.valueOf(rs.getString("estado"));
        inc.setEstado(estado);
        java.sql.Date fi = rs.getDate("fecha");
        inc.setFecha(fi != null ? fi.toLocalDate() : LocalDate.now());
        inc.setNombreInquilino(rs.getString("nombre_inquilino"));
        inc.setNumeroHabitacion(rs.getInt("numero_habitacion"));
        inc.setCosteReparacion(BigDecimal.ZERO);
        // Derive priority from workflow state
        inc.setPrioridad(switch (estado) {
            case pendiente  -> Prioridad.Alta;
            case en_proceso -> Prioridad.Media;
            case resuelta   -> Prioridad.Baja;
        });
        return inc;
    }

    private void cargarCostesEnLista(List<Incidencia> lista) {
        if (lista.isEmpty()) return;
        try {
            String ids = lista.stream()
                    .map(i -> String.valueOf(i.getIdIncidencia()))
                    .collect(Collectors.joining(","));
            String sql = "SELECT id_incidencia, COALESCE(coste_reparacion, 0) AS coste " +
                         "FROM Incidencias WHERE id_incidencia IN (" + ids + ")";
            try (Connection con = getConexion();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                Map<Integer, BigDecimal> costes = new HashMap<>();
                while (rs.next()) costes.put(rs.getInt(1), rs.getBigDecimal(2));
                for (Incidencia inc : lista)
                    inc.setCosteReparacion(costes.getOrDefault(inc.getIdIncidencia(), BigDecimal.ZERO));
            }
        } catch (SQLException e) {
            // coste_reparacion puede no existir si no se corrió
            // sql/alter_incidencias_coste.sql — se degrada a coste 0, pero
            // se registra por si la causa es otra (conexión, permisos...).
            System.err.println("[IncidenciaDAO] No se pudieron cargar costes de incidencias: " + e.getMessage());
        }
    }

    private BigDecimal cargarCosteIndividual(int idIncidencia) {
        try {
            String sql = "SELECT COALESCE(coste_reparacion, 0) FROM Incidencias WHERE id_incidencia = ?";
            try (Connection con = getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idIncidencia);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[IncidenciaDAO] No se pudo cargar coste de incidencia " + idIncidencia + ": " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
