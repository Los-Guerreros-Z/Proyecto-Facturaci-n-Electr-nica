package infraestructure.integration;

import domain.entity.Factura;
import domain.enums.EstadoEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DgtdClient {

    private final String modo;
    private final String dgtdUrl;
    private final RestClient restClient;

    public DgtdClient(
            @Value("${integraciones.modo:SIMULADO}") String modo,
            @Value("${integraciones.dgtd-url:}") String dgtdUrl
    ) {
        this.modo = modo;
        this.dgtdUrl = dgtdUrl;
        this.restClient = RestClient.create();
    }

    public DgtdResult enviar(Factura factura, String xml) {

        /*
         * ============================================================
         * MODO SIMULADO
         * ============================================================
         */
        if ("SIMULADO".equalsIgnoreCase(modo)) {

            // Si el ID empieza con RECHAZO-, simula rechazo
            if (factura.getId() != null &&
                    factura.getId().toUpperCase().startsWith("RECHAZO-")) {

                return new DgtdResult(
                        EstadoEnvio.RECHAZADO,
                        "400",
                        "Factura rechazada por DGTD simulado",
                        null,
                        "SIMULADO"
                );
            }

            // En cualquier otro caso, simula aceptación
            String acuse = "SIM-" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            return new DgtdResult(
                    EstadoEnvio.ACEPTADO,
                    "202",
                    "Factura recibida por DGTD simulado",
                    acuse,
                    "SIMULADO"
            );
        }

        /*
         * ============================================================
         * MODO REAL
         * ============================================================
         */
        if (dgtdUrl == null || dgtdUrl.isBlank()) {

            return new DgtdResult(
                    EstadoEnvio.PENDIENTE_INTEGRACION,
                    "PENDIENTE",
                    "Mini Tributación no está configurada",
                    null,
                    "REAL"
            );
        }

        try {

            Map<String, Object> payload = construirPayload(factura, xml);

            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = restClient
                    .post()
                    .uri(dgtdUrl + "/api/facturas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null) {

                return new DgtdResult(
                        EstadoEnvio.PENDIENTE_INTEGRACION,
                        "SIN_RESPUESTA",
                        "Mini Tributación no devolvió una respuesta",
                        null,
                        "REAL"
                );
            }

            String estado = valorTexto(respuesta.get("estado"));
            String mensaje = valorTexto(respuesta.get("mensaje"));
            String numeroAcuse = valorTexto(respuesta.get("numeroAcuse"));
            String motivo = valorTexto(respuesta.get("motivo"));

            /*
             * Factura aceptada
             */
            if ("Aceptada".equalsIgnoreCase(estado)) {

                return new DgtdResult(
                        EstadoEnvio.ACEPTADO,
                        "201",
                        mensaje != null
                                ? mensaje
                                : "Factura aceptada por Mini Tributación",
                        numeroAcuse,
                        "REAL"
                );
            }

            /*
             * Factura rechazada
             */
            String mensajeRechazo = mensaje;

            if (motivo != null && !motivo.isBlank()) {

                if (mensajeRechazo == null || mensajeRechazo.isBlank()) {
                    mensajeRechazo = motivo;
                } else {
                    mensajeRechazo =
                            mensajeRechazo + " - " + motivo;
                }
            }

            return new DgtdResult(
                    EstadoEnvio.RECHAZADO,
                    "400",
                    mensajeRechazo != null
                            ? mensajeRechazo
                            : "Factura rechazada por Mini Tributación",
                    numeroAcuse,
                    "REAL"
            );

            /*
             * ============================================================
             * ERRORES HTTP 4xx
             * ============================================================
             */
        } catch (HttpClientErrorException ex) {

            String mensaje =
                    "Mini Tributación rechazó la factura";

            try {

                @SuppressWarnings("unchecked")
                Map<String, Object> error =
                        ex.getResponseBodyAs(Map.class);

                if (error != null) {

                    String respuestaMensaje =
                            valorTexto(error.get("mensaje"));

                    String motivo =
                            valorTexto(error.get("motivo"));

                    if (respuestaMensaje != null &&
                            !respuestaMensaje.isBlank()) {

                        mensaje = respuestaMensaje;
                    }

                    if (motivo != null &&
                            !motivo.isBlank()) {

                        mensaje = mensaje + " - " + motivo;
                    }
                }

            } catch (Exception ignored) {
                // Si Mini Tributación devuelve algo diferente a JSON,
                // se conserva el mensaje general.
            }

            return new DgtdResult(
                    EstadoEnvio.RECHAZADO,
                    String.valueOf(ex.getStatusCode().value()),
                    mensaje,
                    null,
                    "REAL"
            );

            /*
             * ============================================================
             * ERROR DE CONEXIÓN
             * ============================================================
             */
        } catch (RestClientException ex) {

            return new DgtdResult(
                    EstadoEnvio.PENDIENTE_INTEGRACION,
                    "ERROR_CONEXION",
                    "No fue posible comunicarse con Mini Tributación: "
                            + ex.getMessage(),
                    null,
                    "REAL"
            );
        }
    }

    /*
     * ================================================================
     * CONSTRUIR JSON QUE ESPERA MINI TRIBUTACIÓN
     * ================================================================
     */
    private Map<String, Object> construirPayload(
            Factura factura,
            String xml
    ) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put("id", factura.getId());
        payload.put("fecha", factura.getFecha());
        payload.put("moneda", factura.getMoneda());
        payload.put(
                "condicionVenta",
                factura.getCondicionVenta()
        );
        payload.put(
                "medioPago",
                factura.getMedioPago()
        );

        payload.put(
                "tipoDocumento",
                "Factura electrónica"
        );

        /*
         * ============================================================
         * EMISOR
         * ============================================================
         */
        Map<String, Object> emisor =
                new LinkedHashMap<>();

        emisor.put(
                "nombre",
                factura.getEmisor().getNombre()
        );

        Map<String, Object> identificacionEmisor =
                new LinkedHashMap<>();

        identificacionEmisor.put(
                "tipo",
                factura.getEmisor()
                        .getTipoIdentificacion()
                        .name()
        );

        identificacionEmisor.put(
                "numero",
                factura.getEmisor()
                        .getNumeroIdentificacion()
        );

        emisor.put(
                "identificacion",
                identificacionEmisor
        );

        emisor.put(
                "correo",
                factura.getEmisor().getCorreo()
        );

        payload.put(
                "emisor",
                emisor
        );

        /*
         * ============================================================
         * RECEPTOR
         * ============================================================
         */
        Map<String, Object> receptor =
                new LinkedHashMap<>();

        receptor.put(
                "nombre",
                factura.getReceptor().getNombre()
        );

        Map<String, Object> identificacionReceptor =
                new LinkedHashMap<>();

        identificacionReceptor.put(
                "tipo",
                factura.getReceptor()
                        .getTipoIdentificacion()
                        .name()
        );

        identificacionReceptor.put(
                "numero",
                factura.getReceptor()
                        .getNumeroIdentificacion()
        );

        receptor.put(
                "identificacion",
                identificacionReceptor
        );

        receptor.put(
                "correo",
                factura.getReceptor().getCorreo()
        );

        payload.put(
                "receptor",
                receptor
        );

        /*
         * ============================================================
         * DETALLES
         * ============================================================
         */
        payload.put(
                "items",
                factura.getItems()
                        .stream()
                        .map(item -> {

                            Map<String, Object> linea =
                                    new LinkedHashMap<>();

                            linea.put(
                                    "detalle",
                                    item.getDetalle()
                            );

                            linea.put(
                                    "cantidad",
                                    item.getCantidad()
                            );

                            linea.put(
                                    "precioUnitario",
                                    item.getPrecioUnitario()
                            );

                            linea.put(
                                    "montoTotalLinea",
                                    item.getMontoTotalLinea()
                            );

                            return linea;

                        })
                        .toList()
        );

        /*
         * ============================================================
         * TOTALES
         * ============================================================
         */
        Map<String, Object> totales =
                new LinkedHashMap<>();

        totales.put(
                "totalGravado",
                factura.getTotalGravado()
        );

        totales.put(
                "totalExento",
                factura.getTotalExento()
        );

        totales.put(
                "totalDescuentos",
                factura.getTotalDescuentos()
        );

        totales.put(
                "totalImpuesto",
                factura.getTotalImpuesto()
        );

        totales.put(
                "totalComprobante",
                factura.getTotalComprobante()
        );

        payload.put(
                "totales",
                totales
        );

        /*
         * XML generado por FacturaSmart
         */
        payload.put(
                "archivoXML",
                xml
        );

        return payload;
    }

    /*
     * ================================================================
     * UTILIDAD
     * ================================================================
     */
    private String valorTexto(Object valor) {

        if (valor == null) {
            return null;
        }

        return String.valueOf(valor);
    }

    /*
     * ================================================================
     * RESULTADO
     * ================================================================
     */
    public record DgtdResult(
            EstadoEnvio estado,
            String codigo,
            String mensaje,
            String numeroAcuse,
            String modo
    ) {
    }
}