package com.gestionap;

import com.gestionap.dao.IncidenciaDAO;
import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.*;
import com.gestionap.model.Habitacion.Estado;
import com.gestionap.model.Pago.MetodoPago;
import com.gestionap.service.ContratoService;
import com.gestionap.service.HabitacionService;
import com.gestionap.service.InquilinoService;
import com.gestionap.utils.XmlExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada de GestionAp.
 * Presenta un menu de consola interactivo para gestionar el alquiler de habitaciones.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final HabitacionService habitacionService = new HabitacionService();
    private static final InquilinoService  inquilinoService  = new InquilinoService();
    private static final ContratoService   contratoService   = new ContratoService();
    private static final IncidenciaDAO     incidenciaDAO     = new IncidenciaDAO();
    private static final XmlExporter       xmlExporter       = new XmlExporter();

    // =========================================================================
    // METODO PRINCIPAL
    // =========================================================================

    public static void main(String[] args) {
        mostrarBienvenida();
        try {
            // Verificar conexion a la base de datos al arrancar
            DatabaseConnection.getInstance();
            System.out.println("Conexion a la base de datos establecida correctamente.\n");
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo conectar a la base de datos.");
            System.err.println(e.getMessage());
            System.err.println("Verifica que MySQL este activo y que database.properties este configurado.");
            return;
        }

        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Selecciona una opcion: ");
            System.out.println();

            try {
                switch (opcion) {
                    case 1 -> menuHabitaciones();
                    case 2 -> menuInquilinos();
                    case 3 -> menuContratos();
                    case 4 -> menuPagos();
                    case 5 -> menuIncidencias();
                    case 6 -> exportarContratosXml();
                    case 0 -> ejecutando = false;
                    default -> System.out.println("Opcion no valida. Por favor elige entre 0 y 6.\n");
                }
            } catch (SQLException e) {
                System.err.println("Error de base de datos: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Error de validacion: " + e.getMessage());
            }
        }

        System.out.println("\nCerrando GestionAp...");
        try { DatabaseConnection.getInstance().cerrarConexion(); } catch (SQLException ignored) {}
        System.out.println("Hasta luego.");
    }

    // =========================================================================
    // MENU 1 - HABITACIONES
    // =========================================================================

    private static void menuHabitaciones() throws SQLException {
        boolean volver = false;
        while (!volver) {
            System.out.println("=== GESTIONAR HABITACIONES ===");
            System.out.println("  1. Listar todas las habitaciones");
            System.out.println("  2. Listar habitaciones disponibles");
            System.out.println("  3. Cambiar estado de una habitacion");
            System.out.println("  0. Volver al menu principal");
            int op = leerEntero("Opcion: ");
            System.out.println();

            switch (op) {
                case 1 -> listarTodasHabitaciones();
                case 2 -> listarHabitacionesDisponibles();
                case 3 -> cambiarEstadoHabitacion();
                case 0 -> volver = true;
                default -> System.out.println("Opcion no valida.\n");
            }
        }
    }

    private static void listarTodasHabitaciones() throws SQLException {
        List<Habitacion> lista = habitacionService.listarTodas();
        if (lista.isEmpty()) {
            System.out.println("No hay habitaciones registradas.\n");
            return;
        }
        System.out.println("--- Todas las habitaciones (" + lista.size() + ") ---");
        System.out.printf("%-5s %-6s %-12s %-15s %-30s %-15s%n",
                "ID", "Num.", "Precio (€)", "Estado", "Direccion piso", "Ciudad");
        System.out.println("-".repeat(90));
        for (Habitacion h : lista) {
            System.out.printf("%-5d %-6d %-12.2f %-15s %-30s %-15s%n",
                    h.getIdHabitacion(), h.getNumero(), h.getPrecio(),
                    h.getEstado(), h.getDireccionPiso(), h.getNombreCiudad());
        }
        System.out.println();
    }

    private static void listarHabitacionesDisponibles() throws SQLException {
        List<Habitacion> lista = habitacionService.listarDisponibles();
        if (lista.isEmpty()) {
            System.out.println("No hay habitaciones disponibles en este momento.\n");
            return;
        }
        System.out.println("--- Habitaciones disponibles (" + lista.size() + ") ---");
        System.out.printf("%-5s %-6s %-12s %-30s %-15s%n",
                "ID", "Num.", "Precio (€)", "Direccion piso", "Ciudad");
        System.out.println("-".repeat(75));
        for (Habitacion h : lista) {
            System.out.printf("%-5d %-6d %-12.2f %-30s %-15s%n",
                    h.getIdHabitacion(), h.getNumero(), h.getPrecio(),
                    h.getDireccionPiso(), h.getNombreCiudad());
        }
        System.out.println();
    }

    private static void cambiarEstadoHabitacion() throws SQLException {
        int id = leerEntero("ID de la habitacion: ");
        System.out.println("Nuevo estado:");
        System.out.println("  1. disponible");
        System.out.println("  2. alquilada");
        System.out.println("  3. mantenimiento");
        int opEstado = leerEntero("Estado: ");
        Estado nuevo = switch (opEstado) {
            case 1 -> Estado.disponible;
            case 2 -> Estado.alquilada;
            case 3 -> Estado.mantenimiento;
            default -> throw new IllegalArgumentException("Estado no reconocido.");
        };
        habitacionService.cambiarEstado(id, nuevo);
        System.out.println("Estado actualizado correctamente a '" + nuevo + "'.\n");
    }

    // =========================================================================
    // MENU 2 - INQUILINOS
    // =========================================================================

    private static void menuInquilinos() throws SQLException {
        boolean volver = false;
        while (!volver) {
            System.out.println("=== GESTIONAR INQUILINOS ===");
            System.out.println("  1. Listar todos los inquilinos");
            System.out.println("  2. Añadir nuevo inquilino");
            System.out.println("  3. Buscar inquilino por DNI");
            System.out.println("  0. Volver al menu principal");
            int op = leerEntero("Opcion: ");
            System.out.println();

            switch (op) {
                case 1 -> listarTodosInquilinos();
                case 2 -> añadirInquilino();
                case 3 -> buscarInquilinoPorDni();
                case 0 -> volver = true;
                default -> System.out.println("Opcion no valida.\n");
            }
        }
    }

    private static void listarTodosInquilinos() throws SQLException {
        List<Inquilino> lista = inquilinoService.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay inquilinos registrados.\n");
            return;
        }
        System.out.println("--- Inquilinos registrados (" + lista.size() + ") ---");
        System.out.printf("%-5s %-25s %-12s %-15s %-30s%n",
                "ID", "Nombre completo", "DNI", "Telefono", "Email");
        System.out.println("-".repeat(92));
        for (Inquilino i : lista) {
            System.out.printf("%-5d %-25s %-12s %-15s %-30s%n",
                    i.getIdInquilino(), i.getNombreCompleto(),
                    i.getDni(), i.getTelefono(), i.getEmail());
        }
        System.out.println();
    }

    private static void añadirInquilino() throws SQLException {
        System.out.println("--- Nuevo inquilino ---");
        String nombre    = leerTexto("Nombre: ");
        String apellidos = leerTexto("Apellidos: ");
        String dni       = leerTexto("DNI: ");
        String telefono  = leerTexto("Telefono (opcional, Enter para saltar): ");
        String email     = leerTexto("Email (opcional, Enter para saltar): ");

        int id = inquilinoService.añadirInquilino(
                nombre, apellidos, dni,
                telefono.isBlank() ? null : telefono,
                email.isBlank()    ? null : email);
        System.out.println("Inquilino registrado con ID " + id + ".\n");
    }

    private static void buscarInquilinoPorDni() throws SQLException {
        String dni = leerTexto("DNI del inquilino: ");
        Inquilino i = inquilinoService.buscarPorDni(dni);
        System.out.println("--- Resultado ---");
        System.out.println("ID       : " + i.getIdInquilino());
        System.out.println("Nombre   : " + i.getNombreCompleto());
        System.out.println("DNI      : " + i.getDni());
        System.out.println("Telefono : " + i.getTelefono());
        System.out.println("Email    : " + i.getEmail());
        System.out.println();
    }

    // =========================================================================
    // MENU 3 - CONTRATOS
    // =========================================================================

    private static void menuContratos() throws SQLException {
        boolean volver = false;
        while (!volver) {
            System.out.println("=== GESTIONAR CONTRATOS ===");
            System.out.println("  1. Crear nuevo contrato");
            System.out.println("  2. Listar contratos activos");
            System.out.println("  3. Listar todos los contratos");
            System.out.println("  0. Volver al menu principal");
            int op = leerEntero("Opcion: ");
            System.out.println();

            switch (op) {
                case 1 -> crearContrato();
                case 2 -> listarContratosActivos();
                case 3 -> listarTodosContratos();
                case 0 -> volver = true;
                default -> System.out.println("Opcion no valida.\n");
            }
        }
    }

    private static void crearContrato() throws SQLException {
        System.out.println("--- Nuevo contrato ---");
        int idHabitacion  = leerEntero("ID de la habitacion: ");
        int idInquilino   = leerEntero("ID del inquilino: ");
        LocalDate inicio  = leerFecha("Fecha de inicio (YYYY-MM-DD): ");
        LocalDate fin     = leerFecha("Fecha de fin    (YYYY-MM-DD): ");
        BigDecimal precio = leerDecimal("Precio mensual (€): ");

        int id = contratoService.crearContrato(idHabitacion, idInquilino, inicio, fin, precio);
        System.out.println("Contrato creado con ID " + id +
                           ". La habitacion ha pasado a estado 'alquilada'.\n");
    }

    private static void listarContratosActivos() throws SQLException {
        List<Contrato> lista = contratoService.listarActivos();
        imprimirTablaContratos(lista, "Contratos activos");
    }

    private static void listarTodosContratos() throws SQLException {
        List<Contrato> lista = contratoService.listarTodos();
        imprimirTablaContratos(lista, "Todos los contratos");
    }

    private static void imprimirTablaContratos(List<Contrato> lista, String titulo) {
        if (lista.isEmpty()) {
            System.out.println("No hay contratos que mostrar.\n");
            return;
        }
        System.out.println("--- " + titulo + " (" + lista.size() + ") ---");
        System.out.printf("%-5s %-5s %-25s %-12s %-12s %-12s %-7s%n",
                "ID", "Hab.", "Inquilino", "Inicio", "Fin", "Precio/mes", "Activo");
        System.out.println("-".repeat(85));
        for (Contrato c : lista) {
            System.out.printf("%-5d %-5d %-25s %-12s %-12s %-12.2f %-7s%n",
                    c.getIdContrato(), c.getNumeroHabitacion(),
                    c.getNombreInquilino(), c.getFechaInicio(),
                    c.getFechaFin(), c.getPrecioMensual(),
                    c.isActivo() ? "Si" : "No");
        }
        System.out.println();
    }

    // =========================================================================
    // MENU 4 - PAGOS
    // =========================================================================

    private static void menuPagos() throws SQLException {
        boolean volver = false;
        while (!volver) {
            System.out.println("=== GESTIONAR PAGOS ===");
            System.out.println("  1. Registrar nuevo pago");
            System.out.println("  2. Listar pagos de un contrato");
            System.out.println("  0. Volver al menu principal");
            int op = leerEntero("Opcion: ");
            System.out.println();

            switch (op) {
                case 1 -> registrarPago();
                case 2 -> listarPagosPorContrato();
                case 0 -> volver = true;
                default -> System.out.println("Opcion no valida.\n");
            }
        }
    }

    private static void registrarPago() throws SQLException {
        System.out.println("--- Registrar pago ---");
        int idContrato    = leerEntero("ID del contrato: ");
        BigDecimal importe = leerDecimal("Importe (€): ");
        System.out.println("Metodo de pago:");
        System.out.println("  1. Transferencia");
        System.out.println("  2. Efectivo");
        System.out.println("  3. Domiciliacion");
        int opMetodo = leerEntero("Metodo: ");
        MetodoPago metodo = switch (opMetodo) {
            case 1 -> MetodoPago.transferencia;
            case 2 -> MetodoPago.efectivo;
            case 3 -> MetodoPago.domiciliacion;
            default -> throw new IllegalArgumentException("Metodo de pago no reconocido.");
        };
        String fechaStr = leerTexto("Fecha del pago (YYYY-MM-DD) [Enter = hoy]: ");
        LocalDate fecha = fechaStr.isBlank() ? LocalDate.now() : LocalDate.parse(fechaStr);

        int id = contratoService.registrarPago(idContrato, importe, metodo, fecha);
        System.out.println("Pago registrado con ID " + id + ".\n");
    }

    private static void listarPagosPorContrato() throws SQLException {
        int idContrato = leerEntero("ID del contrato: ");
        List<Pago> lista = contratoService.listarPagosPorContrato(idContrato);
        if (lista.isEmpty()) {
            System.out.println("No hay pagos registrados para el contrato " + idContrato + ".\n");
            return;
        }
        System.out.println("--- Pagos del contrato " + idContrato + " (" + lista.size() + ") ---");
        System.out.printf("%-6s %-12s %-15s %-20s%n",
                "ID", "Importe (€)", "Metodo", "Fecha");
        System.out.println("-".repeat(58));
        for (Pago p : lista) {
            System.out.printf("%-6d %-12.2f %-15s %-20s%n",
                    p.getIdPago(), p.getCantidad(), p.getMetodoPago(), p.getFechaPago());
        }
        System.out.println();
    }

    // =========================================================================
    // MENU 5 - INCIDENCIAS
    // =========================================================================

    private static void menuIncidencias() throws SQLException {
        boolean volver = false;
        while (!volver) {
            System.out.println("=== GESTIONAR INCIDENCIAS ===");
            System.out.println("  1. Listar incidencias pendientes");
            System.out.println("  2. Crear nueva incidencia");
            System.out.println("  3. Listar todas las incidencias");
            System.out.println("  0. Volver al menu principal");
            int op = leerEntero("Opcion: ");
            System.out.println();

            switch (op) {
                case 1 -> listarIncidenciasPendientes();
                case 2 -> crearIncidencia();
                case 3 -> listarTodasIncidencias();
                case 0 -> volver = true;
                default -> System.out.println("Opcion no valida.\n");
            }
        }
    }

    private static void listarIncidenciasPendientes() throws SQLException {
        List<Incidencia> lista = incidenciaDAO.listarPendientes();
        imprimirTablaIncidencias(lista, "Incidencias pendientes");
    }

    private static void listarTodasIncidencias() throws SQLException {
        List<Incidencia> lista = incidenciaDAO.listarTodas();
        imprimirTablaIncidencias(lista, "Todas las incidencias");
    }

    private static void imprimirTablaIncidencias(List<Incidencia> lista, String titulo) {
        if (lista.isEmpty()) {
            System.out.println("No hay incidencias que mostrar.\n");
            return;
        }
        System.out.println("--- " + titulo + " (" + lista.size() + ") ---");
        System.out.printf("%-5s %-5s %-25s %-12s %-12s %-40s%n",
                "ID", "Hab.", "Inquilino", "Estado", "Fecha", "Descripcion");
        System.out.println("-".repeat(100));
        for (Incidencia i : lista) {
            String desc = i.getDescripcion();
            if (desc.length() > 38) desc = desc.substring(0, 35) + "...";
            System.out.printf("%-5d %-5d %-25s %-12s %-12s %-40s%n",
                    i.getIdIncidencia(), i.getNumeroHabitacion(),
                    i.getNombreInquilino(), i.getEstado(), i.getFecha(), desc);
        }
        System.out.println();
    }

    private static void crearIncidencia() throws SQLException {
        System.out.println("--- Nueva incidencia ---");
        int idHabitacion = leerEntero("ID de la habitacion: ");
        int idInquilino  = leerEntero("ID del inquilino que la reporta: ");
        String desc      = leerTexto("Descripcion: ");
        String fechaStr  = leerTexto("Fecha (YYYY-MM-DD) [Enter = hoy]: ");
        LocalDate fecha  = fechaStr.isBlank() ? LocalDate.now() : LocalDate.parse(fechaStr);

        Incidencia inc = new Incidencia(0, idHabitacion, idInquilino,
                                        desc, Incidencia.Estado.pendiente, fecha);
        int id = incidenciaDAO.insertar(inc);
        System.out.println("Incidencia registrada con ID " + id + " (estado: pendiente).\n");
    }

    // =========================================================================
    // MENU 6 - EXPORTAR XML
    // =========================================================================

    private static void exportarContratosXml() throws SQLException {
        System.out.println("=== EXPORTAR CONTRATOS A XML ===");
        System.out.println("  1. Exportar solo contratos activos");
        System.out.println("  2. Exportar todos los contratos");
        int op = leerEntero("Opcion: ");

        List<Contrato> lista = (op == 1)
                ? contratoService.listarActivos()
                : contratoService.listarTodos();

        if (lista.isEmpty()) {
            System.out.println("No hay contratos para exportar.\n");
            return;
        }

        String ruta = leerTexto("Ruta del archivo XML de destino [Enter = contratos_export.xml]: ");
        if (ruta.isBlank()) ruta = "contratos_export.xml";

        try {
            xmlExporter.exportarContratos(lista, ruta);
            System.out.println("Exportados " + lista.size() + " contratos a: " + ruta + "\n");
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo XML: " + e.getMessage());
        }
    }

    // =========================================================================
    // METODOS AUXILIARES DE CONSOLA
    // =========================================================================

    private static void mostrarBienvenida() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║       GestionAp - Alquiler de Habitaciones   ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                MENU PRINCIPAL                ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  1. Gestionar habitaciones                   ║");
        System.out.println("║  2. Gestionar inquilinos                     ║");
        System.out.println("║  3. Gestionar contratos                      ║");
        System.out.println("║  4. Gestionar pagos                          ║");
        System.out.println("║  5. Ver incidencias                          ║");
        System.out.println("║  6. Exportar contratos a XML                 ║");
        System.out.println("║  0. Salir                                    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    /** Lee un entero de consola; devuelve -1 si la entrada no es numerica. */
    private static int leerEntero(String prompt) {
        System.out.print(prompt);
        String linea = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Lee una linea de texto de consola (nunca null). */
    private static String leerTexto(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /** Lee un BigDecimal de consola; lanza IllegalArgumentException si es invalido. */
    private static BigDecimal leerDecimal(String prompt) {
        System.out.print(prompt);
        String linea = scanner.nextLine().trim().replace(",", ".");
        try {
            return new BigDecimal(linea);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numerico no valido: " + linea);
        }
    }

    /** Lee una fecha en formato YYYY-MM-DD; lanza IllegalArgumentException si el formato es incorrecto. */
    private static LocalDate leerFecha(String prompt) {
        System.out.print(prompt);
        String linea = scanner.nextLine().trim();
        try {
            return LocalDate.parse(linea);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha incorrecto. Usa YYYY-MM-DD. Recibido: " + linea);
        }
    }
}
