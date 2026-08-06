package com.gestionap.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Acceso al Keychain de inicio de sesión de macOS vía el comando `security`,
 * para no guardar secretos (p. ej. la contraseña SMTP) en texto plano en
 * disco. Solo funciona en macOS — esta app ya está limitada a esa
 * plataforma (ver classifier mac-aarch64 en pom.xml).
 *
 * Limitación conocida: `add-generic-password -w` pasa el secreto como
 * argumento del proceso `security`, así que queda expuesto brevemente en la
 * lista de procesos (`ps`) mientras el comando corre. Es una limitación del
 * propio `security` de Apple, no de esta clase — sigue siendo muchísimo
 * mejor que un fichero en texto plano permanente en disco.
 */
public class KeychainUtil {

    private KeychainUtil() {}

    /** Guarda (o actualiza si ya existe) un secreto en el Keychain. */
    public static boolean guardar(String servicio, String cuenta, String secreto) {
        return ejecutar(List.of("security", "add-generic-password",
                "-a", cuenta, "-s", servicio, "-w", secreto, "-U")) == 0;
    }

    /** Lee un secreto del Keychain. Vacío si no existe o el usuario deniega el acceso. */
    public static Optional<String> leer(String servicio, String cuenta) {
        ProcessBuilder pb = new ProcessBuilder("security", "find-generic-password",
                "-a", cuenta, "-s", servicio, "-w");
        try {
            Process proc = pb.start();
            String salida;
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                salida = r.readLine();
            }
            int exit = proc.waitFor();
            if (exit != 0 || salida == null || salida.isBlank()) return Optional.empty();
            return Optional.of(salida);
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** Elimina un secreto del Keychain. */
    public static void eliminar(String servicio, String cuenta) {
        ejecutar(List.of("security", "delete-generic-password", "-a", cuenta, "-s", servicio));
    }

    private static int ejecutar(List<String> comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Process proc = pb.start();
            return proc.waitFor();
        } catch (IOException e) {
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}
