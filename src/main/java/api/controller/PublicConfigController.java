package api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class PublicConfigController {

    private final String integracionesModo;
    private final String bankyUrl;
    private final String bankyMerchantId;
    private final String bankyCurrency;
    private final String facturaBonitaUrl;

    public PublicConfigController(
            @Value("${integraciones.modo:SIMULADO}") String integracionesModo,
            @Value("${integraciones.banky-url:https://bankyfinanzas.netlify.app}") String bankyUrl,
            @Value("${integraciones.banky-merchant-id:}") String bankyMerchantId,
            @Value("${integraciones.banky-currency:CRC}") String bankyCurrency,
            @Value("${integraciones.factura-bonita-url:https://proyecto-kn7p.onrender.com}") String facturaBonitaUrl
    ) {
        this.integracionesModo = integracionesModo;
        this.bankyUrl = bankyUrl;
        this.bankyMerchantId = bankyMerchantId;
        this.bankyCurrency = bankyCurrency;
        this.facturaBonitaUrl = facturaBonitaUrl;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicConfig() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("integracionesModo", integracionesModo);
        body.put("bankyUrl", bankyUrl);
        body.put("bankyMerchantId", bankyMerchantId);
        body.put("bankyCurrency", bankyCurrency);
        body.put("bankyConfigurado", bankyMerchantId != null && !bankyMerchantId.isBlank());
        body.put("facturaBonitaConfigurada", facturaBonitaUrl != null && !facturaBonitaUrl.isBlank());
        return ResponseEntity.ok(body);
    }
}
