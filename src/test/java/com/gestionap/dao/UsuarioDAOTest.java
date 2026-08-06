package com.gestionap.dao;

import com.gestionap.model.Usuario;
import com.gestionap.model.Usuario.Rol;
import com.gestionap.utils.PasswordUtil;
import com.gestionap.utils.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cubre autenticar() — incluida la migración perezosa de hash SHA-256
 * (legacy) a bcrypt del commit ac1797c — y requireAdmin(), el control de
 * rol dentro del DAO del commit a64d8b0 (antes solo se ocultaba el botón en
 * la UI; ahora el propio DAO lo exige).
 */
class UsuarioDAOTest {

    private Connection conexion;
    private UsuarioDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        conexion = DaoTestSupport.nuevaBaseDeTest();
        dao = new UsuarioDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        Session.getInstance().cerrarSesion();
        conexion.close();
    }

    private int crearUsuario(String email, String passwordHash, Rol rol, boolean activo) throws SQLException {
        return DaoTestSupport.insertarYObtenerId(conexion,
                "INSERT INTO Usuarios (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, ?, ?)",
                "Test User", email, passwordHash, rol.name(), activo ? 1 : 0);
    }

    private String leerHashActual(String email) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT password_hash FROM Usuarios WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    private void loguearComo(Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setNombre("Sesión de test");
        u.setRol(rol);
        Session.getInstance().iniciarSesion(u);
    }

    // ── autenticar() ────────────────────────────────────────────

    @Test
    void autenticar_hashBcryptYPasswordCorrecta_devuelveUsuario() throws SQLException {
        String hash = PasswordUtil.hash("miPassword123");
        crearUsuario("ana@example.com", hash, Rol.usuario, true);

        Usuario u = dao.autenticar("ana@example.com", "miPassword123");

        assertNotNull(u);
        assertEquals("ana@example.com", u.getEmail());
        assertEquals(hash, leerHashActual("ana@example.com"), "Un hash ya bcrypt no debe tocarse en el login");
    }

    @Test
    void autenticar_passwordIncorrecta_devuelveNull() throws SQLException {
        crearUsuario("ana@example.com", PasswordUtil.hash("correcta"), Rol.usuario, true);

        assertNull(dao.autenticar("ana@example.com", "incorrecta"));
    }

    @Test
    void autenticar_emailInexistente_devuelveNull() throws SQLException {
        assertNull(dao.autenticar("no-existe@example.com", "cualquiera"));
    }

    @Test
    void autenticar_usuarioInactivo_devuelveNullAunqueLaPasswordSeaCorrecta() throws SQLException {
        crearUsuario("ana@example.com", PasswordUtil.hash("correcta"), Rol.usuario, false);

        assertNull(dao.autenticar("ana@example.com", "correcta"));
    }

    @Test
    void autenticar_hashLegacySha256YPasswordCorrecta_autenticaYMigraABcrypt() throws SQLException {
        String hashLegacy = PasswordUtil.sha256("admin123");
        crearUsuario("admin@example.com", hashLegacy, Rol.admin, true);

        Usuario u = dao.autenticar("admin@example.com", "admin123");

        assertNotNull(u, "Debe autenticar con el hash legacy todavía");
        String hashTrasLogin = leerHashActual("admin@example.com");
        assertNotEquals(hashLegacy, hashTrasLogin, "El hash debe haberse migrado");
        assertTrue(PasswordUtil.esBcrypt(hashTrasLogin), "El nuevo hash debe ser bcrypt");
        assertTrue(PasswordUtil.verificar("admin123", hashTrasLogin), "La misma password debe seguir verificando contra el hash nuevo");
    }

    @Test
    void autenticar_hashLegacySha256YPasswordIncorrecta_noAutenticaNiMigra() throws SQLException {
        String hashLegacy = PasswordUtil.sha256("admin123");
        crearUsuario("admin@example.com", hashLegacy, Rol.admin, true);

        assertNull(dao.autenticar("admin@example.com", "incorrecta"));
        assertEquals(hashLegacy, leerHashActual("admin@example.com"), "No debe migrar si la password no coincide");
    }

    // ── requireAdmin() ──────────────────────────────────────────

    @Test
    void listarTodos_sinSesion_lanzaSecurityException() {
        assertThrows(SecurityException.class, () -> dao.listarTodos());
    }

    @Test
    void listarTodos_sesionUsuarioNoAdmin_lanzaSecurityException() {
        loguearComo(Rol.usuario);
        assertThrows(SecurityException.class, () -> dao.listarTodos());
    }

    @Test
    void listarTodos_sesionAdmin_funciona() throws SQLException {
        crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);
        loguearComo(Rol.admin);

        List<Usuario> lista = dao.listarTodos();

        assertEquals(1, lista.size());
    }

    @Test
    void insertar_sinSesionAdmin_lanzaSecurityExceptionYNoInserta() throws SQLException {
        Usuario nuevo = new Usuario();
        nuevo.setNombre("Nuevo");
        nuevo.setEmail("nuevo@example.com");
        nuevo.setRol(Rol.usuario);

        assertThrows(SecurityException.class, () -> dao.insertar(nuevo, PasswordUtil.hash("x")));

        try (PreparedStatement ps = conexion.prepareStatement("SELECT COUNT(*) FROM Usuarios");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void insertar_conSesionAdmin_creaElUsuario() throws SQLException {
        loguearComo(Rol.admin);
        Usuario nuevo = new Usuario();
        nuevo.setNombre("Nuevo");
        nuevo.setEmail("Nuevo@Example.com");
        nuevo.setRol(Rol.usuario);

        int id = dao.insertar(nuevo, PasswordUtil.hash("x"));

        assertTrue(id > 0);
        assertEquals("nuevo@example.com", leerEmail(id), "El email debe normalizarse a minúsculas");
    }

    private String leerEmail(int idUsuario) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT email FROM Usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    @Test
    void activarDesactivar_sinSesionAdmin_lanzaSecurityException() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);
        assertThrows(SecurityException.class, () -> dao.activarDesactivar(id, false));
    }

    @Test
    void activarDesactivar_conSesionAdmin_cambiaElEstado() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);
        loguearComo(Rol.admin);

        assertTrue(dao.activarDesactivar(id, false));

        try (PreparedStatement ps = conexion.prepareStatement("SELECT activo FROM Usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void actualizarRol_sinSesionAdmin_lanzaSecurityException() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);
        assertThrows(SecurityException.class, () -> dao.actualizarRol(id, Rol.admin));
    }

    @Test
    void actualizarRol_conSesionAdmin_cambiaElRol() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);
        loguearComo(Rol.admin);

        assertTrue(dao.actualizarRol(id, Rol.admin));

        Usuario u = dao.buscarPorEmail("a@example.com");
        assertEquals(Rol.admin, u.getRol());
    }

    // ── autoservicio: NO requiere admin ──────────────────────────

    @Test
    void actualizarPerfil_sinSesion_funcionaIgual() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("x"), Rol.usuario, true);

        assertTrue(dao.actualizarPerfil(id, "Nombre Nuevo", "nuevo-email@example.com"));

        Usuario u = dao.buscarPorEmail("nuevo-email@example.com");
        assertNotNull(u);
        assertEquals("Nombre Nuevo", u.getNombre());
    }

    @Test
    void actualizarPassword_sinSesion_funcionaIgual() throws SQLException {
        int id = crearUsuario("a@example.com", PasswordUtil.hash("vieja"), Rol.usuario, true);

        assertTrue(dao.actualizarPassword(id, PasswordUtil.hash("nueva")));

        assertNotNull(dao.autenticar("a@example.com", "nueva"));
    }
}
