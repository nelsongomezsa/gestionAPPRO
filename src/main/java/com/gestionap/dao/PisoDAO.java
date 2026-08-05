package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Piso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PisoDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public List<Piso> listarTodos() throws SQLException {
        String sql = """
                SELECT p.id_piso, p.direccion, p.numero_habitaciones, p.id_ciudad,
                       c.nombre AS nombre_ciudad
                FROM Pisos p
                JOIN Ciudades c ON p.id_ciudad = c.id_ciudad
                ORDER BY c.nombre, p.direccion
                """;
        List<Piso> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Piso p = new Piso();
                p.setIdPiso(rs.getInt("id_piso"));
                p.setDireccion(rs.getString("direccion"));
                p.setNumeroHabitaciones(rs.getInt("numero_habitaciones"));
                p.setIdCiudad(rs.getInt("id_ciudad"));
                p.setNombreCiudad(rs.getString("nombre_ciudad"));
                lista.add(p);
            }
        }
        return lista;
    }
}
