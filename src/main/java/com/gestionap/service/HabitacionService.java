package com.gestionap.service;

import com.gestionap.dao.HabitacionDAO;
import com.gestionap.model.Habitacion;
import com.gestionap.model.Habitacion.Estado;

import java.sql.SQLException;
import java.util.List;


public class HabitacionService {

    private final HabitacionDAO habitacionDAO = new HabitacionDAO();


    public List<Habitacion> listarTodas() throws SQLException {
        return habitacionDAO.listarTodas();
    }


    public List<Habitacion> listarDisponibles() throws SQLException {
        return habitacionDAO.listarDisponibles();
    }


    public Habitacion buscarPorId(int idHabitacion) throws SQLException {
        Habitacion h = habitacionDAO.buscarPorId(idHabitacion);
        if (h == null) {
            throw new IllegalArgumentException("No existe una habitacion con ID " + idHabitacion);
        }
        return h;
    }


    public void cambiarEstado(int idHabitacion, Estado nuevoEstado) throws SQLException {
        Habitacion h = buscarPorId(idHabitacion);
        Estado actual = h.getEstado();

        if (actual == nuevoEstado) {
            throw new IllegalStateException(
                "La habitacion ya tiene el estado '" + nuevoEstado + "'.");
        }
        if (nuevoEstado == Estado.alquilada && actual != Estado.disponible) {
            throw new IllegalStateException(
                "Solo se puede alquilar una habitacion que este disponible. Estado actual: " + actual);
        }

        boolean actualizado = habitacionDAO.actualizarEstado(idHabitacion, nuevoEstado);
        if (!actualizado) {
            throw new SQLException("No se pudo actualizar el estado de la habitacion ID " + idHabitacion);
        }
    }


    public void liberarHabitacion(int idHabitacion) throws SQLException {
        habitacionDAO.actualizarEstado(idHabitacion, Estado.disponible);
    }
}
