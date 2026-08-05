package com.gestionap.service;

import com.gestionap.dao.ContratoDAO;
import com.gestionap.dao.PagoDAO;
import com.gestionap.model.Contrato;
import com.gestionap.model.Habitacion;
import com.gestionap.model.Pago;
import com.gestionap.model.Pago.MetodoPago;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ContratoService {

    private final ContratoDAO       contratoDAO       = new ContratoDAO();
    private final PagoDAO           pagoDAO           = new PagoDAO();
    private final HabitacionService habitacionService = new HabitacionService();


    public List<Contrato> listarTodos() throws SQLException {
        return contratoDAO.listarTodos();
    }


    public List<Contrato> listarActivos() throws SQLException {
        return contratoDAO.listarActivos();
    }


    public Contrato buscarPorId(int idContrato) throws SQLException {
        Contrato c = contratoDAO.buscarPorId(idContrato);
        if (c == null) {
            throw new IllegalArgumentException("No existe un contrato con ID " + idContrato);
        }
        return c;
    }


    public int crearContrato(int idHabitacion, int idInquilino,
                              LocalDate fechaInicio, LocalDate fechaFin,
                              BigDecimal precioMensual) throws SQLException {

        // Validacion del negocio
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new IllegalArgumentException(
                "La fecha de fin debe ser posterior a la fecha de inicio.");
        }
        if (precioMensual == null || precioMensual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio mensual debe ser mayor que cero.");
        }

        Habitacion h = habitacionService.buscarPorId(idHabitacion);
        if (h.getEstado() != Habitacion.Estado.disponible) {
            throw new IllegalStateException(
                "La habitacion no esta disponible. Estado actual: " + h.getEstado());
        }

        Contrato nuevo = new Contrato(0, idHabitacion, idInquilino,
                                      fechaInicio, fechaFin, precioMensual);
        int idContrato = contratoDAO.insertar(nuevo);

        // Marcar la habitacion como alquilada
        habitacionService.cambiarEstado(idHabitacion, Habitacion.Estado.alquilada);

        return idContrato;
    }


    public List<Pago> listarPagosPorContrato(int idContrato) throws SQLException {
        // Verificar que el contrato existe antes de listar pagos
        buscarPorId(idContrato);
        return pagoDAO.listarPorContrato(idContrato);
    }


    public int registrarPago(int idContrato, BigDecimal cantidad,
                              MetodoPago metodoPago, LocalDate fechaPago) throws SQLException {

        buscarPorId(idContrato); // lanza excepcion si no existe

        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad del pago debe ser mayor que cero.");
        }
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }

        Pago pago = new Pago(0, idContrato, cantidad, metodoPago, fechaPago);
        return pagoDAO.insertar(pago);
    }
}
