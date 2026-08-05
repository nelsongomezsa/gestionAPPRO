package com.gestionap.service;

import com.gestionap.dao.InquilinoDAO;
import com.gestionap.model.Inquilino;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;


public class InquilinoService {

    private final InquilinoDAO inquilinoDAO = new InquilinoDAO();


    private static final Pattern DNI_PATRON = Pattern.compile("^[0-9XYZ][0-9]{7}[A-Z]$");


    public List<Inquilino> listarTodos() throws SQLException {
        return inquilinoDAO.listarTodos();
    }


    public Inquilino buscarPorDni(String dni) throws SQLException {
        Inquilino i = inquilinoDAO.buscarPorDni(dni.toUpperCase().trim());
        if (i == null) {
            throw new IllegalArgumentException("No existe ningun inquilino con DNI " + dni);
        }
        return i;
    }


    public Inquilino buscarPorId(int idInquilino) throws SQLException {
        Inquilino i = inquilinoDAO.buscarPorId(idInquilino);
        if (i == null) {
            throw new IllegalArgumentException("No existe ningun inquilino con ID " + idInquilino);
        }
        return i;
    }


    public int añadirInquilino(String nombre, String apellidos, String dni,
                                String telefono, String email) throws SQLException {
        validarCamposObligatorios(nombre, apellidos, dni);

        String dniNormalizado = dni.toUpperCase().trim();

        if (inquilinoDAO.buscarPorDni(dniNormalizado) != null) {
            throw new IllegalArgumentException("Ya existe un inquilino con el DNI " + dniNormalizado);
        }

        Inquilino nuevo = new Inquilino(0, nombre.trim(), apellidos.trim(),
                                        dniNormalizado, telefono, email);
        return inquilinoDAO.insertar(nuevo);
    }



    private void validarCamposObligatorios(String nombre, String apellidos, String dni) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del inquilino es obligatorio.");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Los apellidos del inquilino son obligatorios.");
        }
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI del inquilino es obligatorio.");
        }
    }
}
