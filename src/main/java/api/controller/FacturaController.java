package api.controller;

import application.dto.EnvioResponse;
import application.dto.FacturaRequest;
import application.dto.FacturaResponse;
import application.dto.FirmaHsmRequest;
import application.dto.PagoBankyRequest;
import application.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturaController {
    private final FacturaService facturaService;
    public FacturaController(FacturaService facturaService) { this.facturaService = facturaService; }

    @PostMapping("/procesar")
    public ResponseEntity<FacturaResponse> procesarFactura(@Valid @RequestBody FacturaRequest request, Authentication auth) {
        return ResponseEntity.ok(facturaService.procesarFacturaElectronica(request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponse>> obtenerMisFacturas(Authentication auth) {
        return ResponseEntity.ok(facturaService.obtenerDelCliente(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtenerFactura(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(facturaService.obtenerPorId(id, auth.getName()));
    }

    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> xml(@PathVariable String id, Authentication auth) {
        byte[] body = facturaService.obtenerXml(id, auth.getName()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + id + ".xml\"")
                .body(body);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".pdf\"")
                .body(facturaService.obtenerPdf(id, auth.getName()));
    }

    @GetMapping(value = "/{id}/pdf-bonita", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfBonita(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "-factura-bonita.pdf\"")
                .body(facturaService.obtenerPdfBonito(id, auth.getName()));
    }

    @GetMapping(value = "/{id}/html-bonita", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> htmlBonita(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(facturaService.obtenerHtmlBonito(id, auth.getName()));
    }


    @PostMapping("/{id}/firma-hsm")
    public ResponseEntity<FacturaResponse> registrarFirmaHsm(
            @PathVariable String id,
            @Valid @RequestBody FirmaHsmRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                facturaService.registrarFirmaHsm(id, request, auth.getName())
        );
    }

    @PostMapping("/{id}/pago-banky")
    public ResponseEntity<FacturaResponse> registrarPagoBanky(
            @PathVariable String id,
            @Valid @RequestBody PagoBankyRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(facturaService.registrarPagoBanky(id, request, auth.getName()));
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<EnvioResponse> enviar(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(facturaService.enviarADgtd(id, auth.getName()));
    }

    @GetMapping("/{id}/envios")
    public ResponseEntity<List<EnvioResponse>> envios(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(facturaService.obtenerEnvios(id, auth.getName()));
    }
}
