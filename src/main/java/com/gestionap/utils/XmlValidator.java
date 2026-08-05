package com.gestionap.utils;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Valida contratos.xml contra esquema.xsd usando la API estándar javax.xml.validation.
 *
 * Uso:
 *   java com.gestionap.utils.XmlValidator
 *   java com.gestionap.utils.XmlValidator xml/contratos.xml xml/esquema.xsd
 *   java com.gestionap.utils.XmlValidator xml/contrato_invalido.xml xml/esquema.xsd
 */
public class XmlValidator {

    public static void main(String[] args) {
        Path xmlPath;
        Path xsdPath;

        if (args.length >= 2) {
            xmlPath = Paths.get(args[0]);
            xsdPath = Paths.get(args[1]);
        } else {
            // Rutas por defecto relativas al directorio de ejecución del proyecto
            xmlPath = Paths.get("xml", "contratos.xml");
            xsdPath = Paths.get("xml", "esquema.xsd");
        }

        boolean valido = validar(xmlPath.toFile(), xsdPath.toFile());
        System.exit(valido ? 0 : 1);
    }

    /**
     * Valida {@code xmlFile} contra {@code xsdFile}.
     *
     * @return true si el XML es válido, false en caso contrario.
     */
    public static boolean validar(File xmlFile, File xsdFile) {
        System.out.println("=== Validador XML — GestionAp ===");
        System.out.println("XML : " + xmlFile.getAbsolutePath());
        System.out.println("XSD : " + xsdFile.getAbsolutePath());
        System.out.println();

        if (!xmlFile.exists()) {
            System.err.println("ERROR: No se encontró el archivo XML: " + xmlFile.getAbsolutePath());
            return false;
        }
        if (!xsdFile.exists()) {
            System.err.println("ERROR: No se encontró el esquema XSD: " + xsdFile.getAbsolutePath());
            return false;
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdFile);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));

            System.out.println("RESULTADO: El XML es VÁLIDO. Cumple todas las restricciones del esquema.");
            return true;

        } catch (org.xml.sax.SAXException e) {
            System.out.println("RESULTADO: El XML es INVÁLIDO.");
            System.out.println("Causa     : " + e.getMessage());
            return false;
        } catch (java.io.IOException e) {
            System.err.println("ERROR al leer los archivos: " + e.getMessage());
            return false;
        }
    }
}
