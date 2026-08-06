package com.gestionap.utils;

import com.github.javakeyring.Keyring;

import java.util.Optional;

/**
 * Almacén de secretos multiplataforma vía java-keyring: Keychain en macOS,
 * Credential Manager en Windows, Secret Service/KWallet en Linux — misma
 * API en los tres sistemas, sin invocar binarios externos por proceso
 * (a diferencia de shellear `security` en macOS, evita exponer el secreto
 * en la lista de procesos).
 *
 * Cualquier fallo (backend no soportado, keyring bloqueado, acceso
 * denegado) se trata como "secreto no disponible" — el llamador decide
 * si vuelve a pedir la credencial en vez de fallar en silencio.
 */
public class KeyringUtil {

    private KeyringUtil() {}

    /** Guarda (o actualiza si ya existe) un secreto en el almacén del sistema. */
    public static boolean guardar(String dominio, String cuenta, String secreto) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(dominio, cuenta, secreto);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Lee un secreto del almacén del sistema. Vacío si no existe o no es accesible. */
    public static Optional<String> leer(String dominio, String cuenta) {
        try (Keyring keyring = Keyring.create()) {
            String valor = keyring.getPassword(dominio, cuenta);
            return (valor == null || valor.isBlank()) ? Optional.empty() : Optional.of(valor);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Elimina un secreto del almacén del sistema. */
    public static void eliminar(String dominio, String cuenta) {
        try (Keyring keyring = Keyring.create()) {
            keyring.deletePassword(dominio, cuenta);
        } catch (Exception ignored) {}
    }
}
