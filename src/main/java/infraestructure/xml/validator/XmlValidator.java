package infraestructure.xml.validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;

@Component
public class XmlValidator {

    private final Schema schema;

    public XmlValidator() throws Exception {

        SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        this.schema = factory.newSchema(
                new ClassPathResource(
                        "xsd/factura-electronica-4.3-base.xsd"
                ).getURL()
        );
    }

    public boolean validarXml(String xmlContent) {

        try {

            Validator validator = schema.newValidator();

            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            validator.validate(
                    new StreamSource(
                            new StringReader(xmlContent)
                    )
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}