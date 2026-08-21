package infraestructure.xml.validator;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.StringReader;

public class XmlValidator {

    public boolean validarXml(String xmlContent) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            // Leemos el XSD directamente desde la carpeta del proyecto
            File schemaFile = new File("infraestructure/xml/schemas/factura.xsd");
            
            if (!schemaFile.exists()) {
                throw new RuntimeException("No se encontró el archivo factura.xsd en la ruta especificada.");
            }
            
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            return true;
        } catch (Exception e) {
            System.err.println("Error de validación XML: " + e.getMessage());
            return false;
        }
    }
}