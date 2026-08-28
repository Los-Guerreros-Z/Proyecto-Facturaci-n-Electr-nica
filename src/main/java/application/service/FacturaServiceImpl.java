package application.service;

import application.dto.EnvioResponse;
import application.dto.FacturaRequest;
import application.dto.FacturaResponse;
import application.dto.FirmaHsmRequest;
import application.dto.PagoBankyRequest;
import application.mapper.FacturaMapper;
import domain.entity.Cliente;
import domain.entity.EnvioFactura;
import domain.entity.Factura;
import domain.enums.EstadoEnvio;
import domain.enums.EstadoFactura;
import domain.enums.EstadoPago;
import domain.repository.EnvioFacturaRepository;
import domain.repository.FacturaRepository;
import infraestructure.integration.DgtdClient;
import infraestructure.integration.HsmSignClient;
import infraestructure.integration.FacturaBonitaClient;
import infraestructure.pdf.FacturaPdfGenerator;
import infraestructure.xml.generator.FacturaXmlGenerator;
import infraestructure.xml.validator.XmlValidator;
import infraestructure.xml.validator.SignedXmlMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final EnvioFacturaRepository envioRepository;
    private final FacturaMapper facturaMapper;
    private final ClienteService clienteService;
    private final FacturaXmlGenerator xmlGenerator;
    private final XmlValidator xmlValidator;
    private final SignedXmlMatcher signedXmlMatcher;
    private final HsmSignClient hsmSignClient;
    private final DgtdClient dgtdClient;
    private final FacturaPdfGenerator pdfGenerator;
    private final FacturaBonitaClient facturaBonitaClient;

    public FacturaServiceImpl(
            FacturaRepository facturaRepository,
            EnvioFacturaRepository envioRepository,
            FacturaMapper facturaMapper,
            ClienteService clienteService,
            FacturaXmlGenerator xmlGenerator,
            XmlValidator xmlValidator,
            SignedXmlMatcher signedXmlMatcher,
            HsmSignClient hsmSignClient,
            DgtdClient dgtdClient,
            FacturaPdfGenerator pdfGenerator,
            FacturaBonitaClient facturaBonitaClient
    ) {
        this.facturaRepository = facturaRepository;
        this.envioRepository = envioRepository;
        this.facturaMapper = facturaMapper;
        this.clienteService = clienteService;
        this.xmlGenerator = xmlGenerator;
        this.xmlValidator = xmlValidator;
        this.signedXmlMatcher = signedXmlMatcher;
        this.hsmSignClient = hsmSignClient;
        this.dgtdClient = dgtdClient;
        this.pdfGenerator = pdfGenerator;
        this.facturaBonitaClient = facturaBonitaClient;
    }

    @Override
    @Transactional
    public FacturaResponse procesarFacturaElectronica(
            FacturaRequest request,
            String correoCliente
    ) {

        if (facturaRepository.existsById(request.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una factura con id " + request.getId()
            );
        }

        Cliente cliente =
                clienteService.obtenerEntidadPorCorreo(correoCliente);

        if (!cliente.getNumeroIdentificacion()
                .equals(request.getEmisor()
                        .getIdentificacion()
                        .getNumero())) {

            throw new IllegalArgumentException(
                    "La identificación del emisor debe corresponder al cliente autenticado"
            );
        }

        validarCoherenciaTotales(request);

        Factura factura = facturaMapper.toEntity(request);

        factura.setCliente(cliente);
        factura.setEstado(EstadoFactura.GENERADA);

        // Generar XML
        String xml = xmlGenerator.generarXml(factura);

        // Validar XML contra el XSD
        boolean valido = xmlValidator.validarXml(xml);

        if (!valido) {
            throw new IllegalArgumentException(
                    "El XML generado no cumple el esquema XSD configurado"
            );
        }

        factura.setXmlGenerado(xml);
        factura.setXmlValido(true);

        // El XML válido queda listo para que el usuario lo firme en HSM Sign CR.
        // El PIN nunca pasa por FacturaSmart.
        factura.setEstado(EstadoFactura.VALIDADA);

        return FacturaResponse.from(
                facturaRepository.save(factura)
        );
    }

    @Override
    public List<FacturaResponse> obtenerDelCliente(
            String correoCliente
    ) {

        Cliente cliente =
                clienteService.obtenerEntidadPorCorreo(correoCliente);

        return facturaRepository
                .findByClienteIdOrderByFechaRegistroDesc(cliente.getId())
                .stream()
                .map(FacturaResponse::from)
                .toList();
    }

    @Override
    public FacturaResponse obtenerPorId(
            String id,
            String correoCliente
    ) {

        return FacturaResponse.from(
                obtenerFacturaPropia(id, correoCliente)
        );
    }

    @Override
    public String obtenerXml(
            String id,
            String correoCliente
    ) {

        Factura factura =
                obtenerFacturaPropia(id, correoCliente);

        // Si existe una firma HSM válida, este endpoint devuelve el XML final firmado.
        // El XML original se conserva en xmlGenerado para trazabilidad y validación XSD.
        if (factura.isFirmaValida()
                && factura.getXmlFirmado() != null
                && !factura.getXmlFirmado().isBlank()) {
            return factura.getXmlFirmado();
        }

        String xml = factura.getXmlGenerado();

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "La factura no tiene un XML generado"
            );
        }

        boolean valido =
                xmlValidator.validarXml(xml);

        if (!valido) {
            throw new IllegalArgumentException(
                    "El XML almacenado no cumple el esquema XSD configurado"
            );
        }

        return xml;
    }

    @Override
    public byte[] obtenerPdf(
            String id,
            String correoCliente
    ) {

        Factura factura =
                obtenerFacturaPropia(id, correoCliente);

        return pdfGenerator.generar(factura);
    }

    @Override
    public byte[] obtenerPdfBonito(
            String id,
            String correoCliente
    ) {
        Factura factura = obtenerFacturaPropia(id, correoCliente);
        return facturaBonitaClient.generarPdf(factura);
    }

    @Override
    public byte[] obtenerHtmlBonito(
            String id,
            String correoCliente
    ) {
        Factura factura = obtenerFacturaPropia(id, correoCliente);
        return facturaBonitaClient.generarHtml(factura);
    }

    @Override
    @Transactional
    public FacturaResponse registrarFirmaHsm(
            String id,
            FirmaHsmRequest request,
            String correoCliente
    ) {
        Factura factura = obtenerFacturaPropia(id, correoCliente);

        if (!factura.isXmlValido()
                || factura.getXmlGenerado() == null
                || factura.getXmlGenerado().isBlank()) {
            throw new IllegalArgumentException(
                    "La factura debe tener un XML válido antes de firmarse"
            );
        }

        if (factura.isFirmaValida()) {
            throw new IllegalArgumentException(
                    "La factura ya posee una firma digital validada"
            );
        }

        if (!signedXmlMatcher.correspondeAlOriginal(
                factura.getXmlGenerado(),
                request.xmlFirmado()
        )) {
            throw new IllegalArgumentException(
                    "El XML firmado no corresponde al comprobante original generado por FacturaSmart"
            );
        }

        HsmSignClient.ValidacionFirmaResult validacion =
                hsmSignClient.validarFirma(request.xmlFirmado());

        if (!validacion.esValida()) {
            throw new IllegalArgumentException(
                    validacion.motivo() != null
                            ? validacion.motivo()
                            : "HSM Sign CR indicó que la firma digital no es válida"
            );
        }

        String esperado = normalizarIdentificacion(
                factura.getEmisor() != null
                        ? factura.getEmisor().getNumeroIdentificacion()
                        : null
        );
        String firmadoPor = normalizarIdentificacion(validacion.signerId());

        if (!esperado.isBlank()
                && !firmadoPor.isBlank()
                && !esperado.equals(firmadoPor)) {
            throw new IllegalArgumentException(
                    "La firma digital pertenece a una identificación distinta del emisor"
            );
        }

        factura.setXmlFirmado(request.xmlFirmado());
        factura.setFirmaValida(true);
        factura.setHashDocumento(limpiarDatoFirma(request.hashDocumento(), 180));
        factura.setSerialCertificado(limpiarDatoFirma(request.serialCertificado(), 180));
        factura.setFirmanteIdentificacion(limpiarDatoFirma(validacion.signerId(), 80));
        factura.setFirmanteNombre(limpiarDatoFirma(validacion.signerName(), 200));
        factura.setAlgoritmoFirma(limpiarDatoFirma(validacion.algorithm(), 120));
        factura.setEstadoCertificado(limpiarDatoFirma(validacion.certificateStatus(), 80));
        factura.setFechaFirmaHsm(limpiarDatoFirma(validacion.signatureDate(), 120));
        factura.setEstado(EstadoFactura.FIRMADA);

        return FacturaResponse.from(facturaRepository.save(factura));
    }

    @Override
    @Transactional
    public EnvioResponse enviarADgtd(
            String id,
            String correoCliente
    ) {

        Factura factura =
                obtenerFacturaPropia(id, correoCliente);

        String xmlOriginal = factura.getXmlGenerado();

        if (xmlOriginal == null || xmlOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "La factura no tiene un XML generado"
            );
        }

        // El XSD se valida contra el comprobante original. Si la factura ya fue
        // firmada y validada por HSM, Mini Tributación recibe el XML firmado.
        if (!xmlValidator.validarXml(xmlOriginal)) {
            throw new IllegalArgumentException(
                    "El XML de la factura no cumple el esquema XSD y no puede enviarse"
            );
        }

        String xmlParaEnvio = factura.isFirmaValida()
                && factura.getXmlFirmado() != null
                && !factura.getXmlFirmado().isBlank()
                ? factura.getXmlFirmado()
                : xmlOriginal;

        DgtdClient.DgtdResult result =
                dgtdClient.enviar(factura, xmlParaEnvio);

        EnvioFactura envio = new EnvioFactura();

        envio.setFactura(factura);
        envio.setFechaEnvio(LocalDateTime.now());
        envio.setEstado(result.estado());
        envio.setCodigoRespuesta(result.codigo());
        envio.setMensajeRespuesta(result.mensaje());
        envio.setNumeroAcuse(result.numeroAcuse());
        envio.setModoIntegracion(result.modo());

        envio = envioRepository.save(envio);

        if (result.estado() == EstadoEnvio.ACEPTADO) {

            factura.setEstado(EstadoFactura.ACEPTADA);

        } else if (result.estado() == EstadoEnvio.RECHAZADO) {

            factura.setEstado(EstadoFactura.RECHAZADA);

        } else if (
                result.estado()
                        == EstadoEnvio.PENDIENTE_INTEGRACION
        ) {

            factura.setEstado(
                    EstadoFactura.PENDIENTE_INTEGRACION
            );

        } else {

            factura.setEstado(EstadoFactura.ENVIADA);
        }

        facturaRepository.save(factura);

        return EnvioResponse.from(envio);
    }

    @Override
    public List<EnvioResponse> obtenerEnvios(
            String id,
            String correoCliente
    ) {

        obtenerFacturaPropia(id, correoCliente);

        return envioRepository
                .findByFacturaIdOrderByFechaEnvioDesc(id)
                .stream()
                .map(EnvioResponse::from)
                .toList();
    }


    @Override
    @Transactional
    public FacturaResponse registrarPagoBanky(
            String id,
            PagoBankyRequest request,
            String correoCliente
    ) {
        Factura factura = obtenerFacturaPropia(id, correoCliente);

        if (factura.getEstadoPago() == EstadoPago.PAGADA) {
            throw new IllegalArgumentException("Esta factura ya se encuentra pagada");
        }

        if (!"completed".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("BankyFinanzas no confirmó un pago completado");
        }

        factura.setEstadoPago(EstadoPago.PAGADA);
        factura.setFechaPago(LocalDateTime.now());
        factura.setCodigoTransaccionBanky(request.getTransactionCode());
        factura.setMarcaTarjeta(limpiarDatoPago(request.getCardBrand(), 40));
        factura.setUltimosDigitosTarjeta(limpiarUltimosDigitos(request.getCardLastFourDigits()));
        factura.setTitularTarjeta(limpiarDatoPago(request.getCardholderName(), 160));
        factura.setMontoPagado(factura.getTotalComprobante());

        return FacturaResponse.from(facturaRepository.save(factura));
    }

    private String limpiarDatoPago(String valor, int max) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        return limpio.length() <= max ? limpio : limpio.substring(0, max);
    }

    private String limpiarUltimosDigitos(String valor) {
        if (valor == null) return null;
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() > 4) digitos = digitos.substring(digitos.length() - 4);
        return digitos.isBlank() ? null : digitos;
    }

    private String normalizarIdentificacion(String identificacion) {
        if (identificacion == null) return "";
        return identificacion.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String limpiarDatoFirma(String valor, int max) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        return limpio.length() <= max ? limpio : limpio.substring(0, max);
    }

    private void validarCoherenciaTotales(FacturaRequest request) {
        double totalLineas = request.getItems().stream()
                .mapToDouble(item -> item.getMontoTotalLinea() == null ? 0.0 : item.getMontoTotalLinea())
                .sum();

        double totalComprobante = request.getTotales().getTotalComprobante();
        if (Math.abs(totalLineas - totalComprobante) > 0.01) {
            throw new IllegalArgumentException(
                    "El total del comprobante no coincide con la suma de las líneas de detalle"
            );
        }

        double totalCalculado = request.getTotales().getTotalGravado()
                + request.getTotales().getTotalExento()
                - request.getTotales().getTotalDescuentos()
                + request.getTotales().getTotalImpuesto();

        if (Math.abs(totalCalculado - totalComprobante) > 0.01) {
            throw new IllegalArgumentException(
                    "Los totales de la factura no son consistentes con el total del comprobante"
            );
        }
    }

    private Factura obtenerFacturaPropia(
            String id,
            String correoCliente
    ) {

        Cliente cliente =
                clienteService.obtenerEntidadPorCorreo(correoCliente);

        Factura factura =
                facturaRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Factura no encontrada"
                                )
                        );

        if (!factura.getCliente()
                .getId()
                .equals(cliente.getId())) {

            throw new SecurityException(
                    "No tiene permiso para consultar esta factura"
            );
        }

        return factura;
    }
}