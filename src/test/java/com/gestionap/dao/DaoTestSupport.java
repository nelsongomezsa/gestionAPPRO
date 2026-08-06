package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Bootstrap de una base H2 en memoria (modo MySQL) para tests de DAO, para
 * no depender de un MySQL real corriendo en la máquina que ejecuta los
 * tests. Cada llamada crea una base nueva y aislada (nombre aleatorio) y
 * apunta el singleton DatabaseConnection a ella — los DAO no cambian ni se
 * enteran de que están hablando con H2 en vez de MySQL.
 */
public final class DaoTestSupport {

    private DaoTestSupport() {}

    /**
     * Crea una base H2 en memoria con el esquema de schema-test.sql cargado
     * y apunta el singleton DatabaseConnection a ella. Cerrar la conexión
     * devuelta (p. ej. en @AfterEach) destruye la base en memoria.
     */
    public static Connection nuevaBaseDeTest() throws SQLException, IOException {
        String nombre = "test_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + nombre + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";
        Connection conexion = DriverManager.getConnection(url, "sa", "");
        ejecutarScript(conexion, "/schema-test.sql");
        DatabaseConnection.usarConexionDeTest(conexion);
        return conexion;
    }

    /** Ejecuta un INSERT de fixture y devuelve la clave autogenerada — para preparar datos en @BeforeEach. */
    public static int insertarYObtenerId(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (var ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se generó ID para: " + sql);
    }

    private static void ejecutarScript(Connection conexion, String recursoClasspath) throws SQLException, IOException {
        String sql;
        try (InputStream is = DaoTestSupport.class.getResourceAsStream(recursoClasspath)) {
            if (is == null) {
                throw new IOException("No se encontró " + recursoClasspath + " en el classpath de test.");
            }
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement st = conexion.createStatement()) {
            for (String sentencia : sql.split(";")) {
                String s = sentencia.strip();
                if (!s.isEmpty()) st.execute(s);
            }
        }
    }
}
