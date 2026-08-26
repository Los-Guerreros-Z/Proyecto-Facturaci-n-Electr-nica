package infraestructure.xml.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlValidatorTest {

    @Test
    void debeAceptarXmlValido() throws Exception {

        XmlValidator validator = new XmlValidator();

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <FacturaElectronica xmlns="https://cdn.comprobanteselectronicos.go.cr/xml-properties/v4.3">

                    <Clave>PRUEBA-001</Clave>
                    <NumeroConsecutivo>PRUEBA-001</NumeroConsecutivo>
                    <FechaEmision>2026-08-25T15:00:00</FechaEmision>
                    <CondicionVenta>CONTADO</CondicionVenta>
                    <MedioPago>SINPE</MedioPago>

                    <Emisor>
                        <Nombre>Mario Espinoza</Nombre>
                        <Identificacion>
                            <Tipo>CEDULA_FISICA</Tipo>
                            <Numero>116780456</Numero>
                        </Identificacion>
                        <CorreoElectronico>mario.prueba@gmail.com</CorreoElectronico>
                    </Emisor>

                    <Receptor>
                        <Nombre>Cliente Prueba</Nombre>
                        <Identificacion>
                            <Tipo>CEDULA_FISICA</Tipo>
                            <Numero>205550555</Numero>
                        </Identificacion>
                        <CorreoElectronico>cliente@gmail.com</CorreoElectronico>
                    </Receptor>

                    <DetalleServicio>
                        <LineaDetalle>
                            <NumeroLinea>1</NumeroLinea>
                            <Detalle>Servicio de parqueo</Detalle>
                            <Cantidad>1</Cantidad>
                            <PrecioUnitario>1000</PrecioUnitario>
                            <Descuento>0</Descuento>
                            <TarifaImpuesto>13</TarifaImpuesto>
                            <SubTotal>1000</SubTotal>
                            <MontoTotalLinea>1130</MontoTotalLinea>
                        </LineaDetalle>
                    </DetalleServicio>

                    <ResumenFactura>
                        <Moneda>CRC</Moneda>
                        <TotalGravado>1000</TotalGravado>
                        <TotalExento>0</TotalExento>
                        <TotalDescuentos>0</TotalDescuentos>
                        <TotalImpuesto>130</TotalImpuesto>
                        <TotalComprobante>1130</TotalComprobante>
                    </ResumenFactura>

                </FacturaElectronica>
                """;

        assertTrue(validator.validarXml(xml));
    }


    @Test
    void debeRechazarTotalComprobanteNegativo() throws Exception {

        XmlValidator validator = new XmlValidator();

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <FacturaElectronica xmlns="https://cdn.comprobanteselectronicos.go.cr/xml-properties/v4.3">

                    <Clave>PRUEBA-INVALIDA-001</Clave>
                    <NumeroConsecutivo>PRUEBA-INVALIDA-001</NumeroConsecutivo>
                    <FechaEmision>2026-08-25T15:00:00</FechaEmision>
                    <CondicionVenta>CONTADO</CondicionVenta>
                    <MedioPago>SINPE</MedioPago>

                    <Emisor>
                        <Nombre>Mario Espinoza</Nombre>
                        <Identificacion>
                            <Tipo>CEDULA_FISICA</Tipo>
                            <Numero>116780456</Numero>
                        </Identificacion>
                        <CorreoElectronico>mario.prueba@gmail.com</CorreoElectronico>
                    </Emisor>

                    <Receptor>
                        <Nombre>Cliente Prueba</Nombre>
                        <Identificacion>
                            <Tipo>CEDULA_FISICA</Tipo>
                            <Numero>205550555</Numero>
                        </Identificacion>
                    </Receptor>

                    <DetalleServicio>
                        <LineaDetalle>
                            <NumeroLinea>1</NumeroLinea>
                            <Detalle>Servicio de parqueo</Detalle>
                            <Cantidad>1</Cantidad>
                            <PrecioUnitario>1000</PrecioUnitario>
                            <Descuento>0</Descuento>
                            <TarifaImpuesto>13</TarifaImpuesto>
                            <SubTotal>1000</SubTotal>
                            <MontoTotalLinea>1130</MontoTotalLinea>
                        </LineaDetalle>
                    </DetalleServicio>

                    <ResumenFactura>
                        <Moneda>CRC</Moneda>
                        <TotalGravado>1000</TotalGravado>
                        <TotalExento>0</TotalExento>
                        <TotalDescuentos>0</TotalDescuentos>
                        <TotalImpuesto>130</TotalImpuesto>
                        <TotalComprobante>-100</TotalComprobante>
                    </ResumenFactura>

                </FacturaElectronica>
                """;

        assertFalse(validator.validarXml(xml));
    }
}