package com.gestionap.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto de acceso a la base de datos, respaldado por un pool de conexiones
 * (HikariCP) en vez de una única Connection compartida por toda la app.
 *
 * <p><b>Uso normal (fuera de una transacción):</b> cada DAO pide su propia
 * conexión con {@code getInstance().getConexion()} y la cierra con
 * try-with-resources — eso la devuelve al pool, no la destruye:
 * <pre>{@code
 * try (Connection con = DatabaseConnection.getInstance().getConexion();
 *      PreparedStatement ps = con.prepareStatement(sql)) {
 *     ...
 * }
 * }</pre>
 *
 * <p><b>Dentro de una transacción</b> ({@link #ejecutarEnTransaccion}): se
 * pide una única conexión del pool para toda la operación y se fija en un
 * {@link ThreadLocal}. Mientras esa transacción esté activa en este hilo,
 * {@code getConexion()} devuelve esa MISMA conexión (necesario para que
 * commit()/rollback() apliquen a todo lo que se hizo dentro, incluidas
 * llamadas a otros DAO anidados) envuelta en un proxy cuyo {@code close()}
 * no hace nada — la transacción es la única dueña de cuándo se cierra de
 * verdad y se devuelve al pool.
 */
public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "database.properties";

    private static DatabaseConnection instancia;

    private static volatile HikariDataSource dataSource;

    /** Solo para tests: si no es null, TODOS los hilos usan esta conexión en vez del pool. */
    private static volatile Connection conexionDeTest;

    /** La conexión de la transacción activa en este hilo, si la hay. */
    private static final ThreadLocal<Connection> conexionTransaccional = new ThreadLocal<>();

    private DatabaseConnection() {}

    public static synchronized DatabaseConnection getInstance() {
        if (instancia == null) instancia = new DatabaseConnection();
        return instancia;
    }

    /**
     * Solo para tests: todas las llamadas a getConexion() (de cualquier
     * hilo) devuelven esta conexión ya abierta (p. ej. H2 en memoria) en
     * vez de pedir una al pool. Los DAO no cambian ni se enteran.
     */
    public static void usarConexionDeTest(Connection conexion) {
        conexionDeTest = conexion;
    }

    /**
     * Conexión para una única operación. Si este hilo está dentro de
     * {@link #ejecutarEnTransaccion}, devuelve la conexión de esa
     * transacción (con close() en no-op). Si no, pide una conexión nueva
     * del pool — el llamador es responsable de cerrarla (try-with-resources)
     * para devolverla.
     */
    public Connection getConexion() throws SQLException {
        Connection fijaDeTest = conexionDeTest;
        if (fijaDeTest != null) return envolverSinCerrar(fijaDeTest);

        Connection enTransaccion = conexionTransaccional.get();
        if (enTransaccion != null) return envolverSinCerrar(enTransaccion);

        return obtenerDataSource().getConnection();
    }

    @FunctionalInterface
    public interface Operacion<T> {
        T ejecutar() throws SQLException;
    }

    /**
     * Ejecuta {@code operacion} en una transacción con una única conexión
     * dedicada de principio a fin: se pide una vez (del pool, o la fija de
     * test), autocommit desactivado, commit si todo va bien, rollback si
     * algo lanza SQLException. Todas las llamadas a getConexion() que
     * ocurran dentro de {@code operacion} en este mismo hilo — incluidas
     * las de otros DAO invocados anidados — reutilizan esta misma conexión.
     *
     * <p>Si ya hay una transacción activa en este hilo (llamada anidada,
     * p. ej. un DAO que a su vez llama a ejecutarEnTransaccion), no abre
     * una segunda conexión: participa de la que ya está abierta y deja que
     * el commit/rollback lo decida la transacción externa.
     */
    public <T> T ejecutarEnTransaccion(Operacion<T> operacion) throws SQLException {
        if (conexionTransaccional.get() != null) {
            return operacion.ejecutar();
        }

        Connection fijaDeTest = conexionDeTest;
        boolean esDelPool = (fijaDeTest == null);
        Connection con = esDelPool ? obtenerDataSource().getConnection() : fijaDeTest;

        conexionTransaccional.set(con);
        try {
            con.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar();
                con.commit();
                return resultado;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } finally {
            conexionTransaccional.remove();
            if (esDelPool) con.close(); // devuelve la conexión al pool; la fija de test no se cierra nunca aquí
        }
    }

    /** Cierra el pool. Se llama al salir de la app (ver Launcher/shutdown hook). */
    public void cerrarConexion() {
        HikariDataSource ds = dataSource;
        if (ds != null && !ds.isClosed()) {
            ds.close();
            System.out.println("Pool de conexiones cerrado correctamente.");
        }
    }

    private static HikariDataSource obtenerDataSource() throws SQLException {
        HikariDataSource actual = dataSource;
        if (actual != null) return actual;
        synchronized (DatabaseConnection.class) {
            if (dataSource == null) {
                dataSource = crearDataSource();
            }
            return dataSource;
        }
    }

    private static HikariDataSource crearDataSource() throws SQLException {
        Properties props = cargarPropiedades();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        config.setPoolName("gestionap-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);  // ms de espera antes de lanzar SQLTransientConnectionException
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(1_800_000);
        try {
            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new SQLException("No se pudo inicializar el pool de conexiones: " + e.getMessage(), e);
        }
    }

    /**
     * Envuelve {@code real} en un proxy de Connection cuyo close() no hace
     * nada — para el código dentro de una transacción (o de un test), que
     * no debe cerrar la conexión compartida, solo quien la abrió.
     */
    private static Connection envolverSinCerrar(Connection real) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName())) return null;
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(
                DatabaseConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    private static Properties cargarPropiedades() throws SQLException {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                throw new SQLException(
                    "No se encontro '" + PROPERTIES_FILE + "' en el classpath.\n" +
                    "Copia 'database.properties.example' como 'database.properties' " +
                    "en src/main/resources/ y rellena tus credenciales."
                );
            }
            props.load(is);
        } catch (IOException e) {
            throw new SQLException("Error leyendo " + PROPERTIES_FILE + ": " + e.getMessage(), e);
        }
        return props;
    }
}
