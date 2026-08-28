package infraestructure.xml.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignedXmlMatcherTest {

    private final SignedXmlMatcher matcher = new SignedXmlMatcher();

    @Test
    void debeAceptarMismoXmlConFirmaEnveloped() {
        String original = """
                <FacturaElectronica xmlns="urn:factura">
                  <Clave>FAC-001</Clave>
                  <ResumenFactura><TotalComprobante>1130</TotalComprobante></ResumenFactura>
                </FacturaElectronica>
                """;

        String firmado = """
                <FacturaElectronica xmlns="urn:factura" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                  <Clave>FAC-001</Clave>
                  <ResumenFactura><TotalComprobante>1130</TotalComprobante></ResumenFactura>
                  <ds:Signature><ds:SignedInfo/></ds:Signature>
                </FacturaElectronica>
                """;

        assertTrue(matcher.correspondeAlOriginal(original, firmado));
    }

    @Test
    void debeRechazarXmlFirmadoConContenidoAlterado() {
        String original = """
                <FacturaElectronica xmlns="urn:factura">
                  <Clave>FAC-001</Clave>
                  <Total>1130</Total>
                </FacturaElectronica>
                """;

        String firmado = """
                <FacturaElectronica xmlns="urn:factura" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                  <Clave>FAC-001</Clave>
                  <Total>999999</Total>
                  <ds:Signature><ds:SignedInfo/></ds:Signature>
                </FacturaElectronica>
                """;

        assertFalse(matcher.correspondeAlOriginal(original, firmado));
    }
}
