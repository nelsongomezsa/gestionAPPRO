package com.gestionap.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Formato legacy (SHA-256 sin sal). Se mantiene solo para verificar
     * hashes ya existentes en la BD durante la migración a bcrypt — no usar
     * para contraseñas nuevas.
     */
    public static String sha256(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /** Hash para contraseñas nuevas: bcrypt con salt aleatorio. */
    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    /** True si el hash almacenado ya tiene formato bcrypt ($2a$/$2b$/$2y$...). */
    public static boolean esBcrypt(String storedHash) {
        return storedHash != null && storedHash.startsWith("$2");
    }

    /**
     * Verifica plainText contra storedHash soportando ambos formatos: bcrypt
     * (actual) y SHA-256 sin sal (legacy, en migración). El llamador decide
     * si, tras una verificación legacy exitosa, conviene re-hashear a bcrypt.
     */
    public static boolean verificar(String plainText, String storedHash) {
        if (storedHash == null) return false;
        if (esBcrypt(storedHash)) {
            return BCrypt.checkpw(plainText, storedHash);
        }
        return sha256(plainText).equals(storedHash);
    }
}
