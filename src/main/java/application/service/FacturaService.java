package application.service;

import application.dto.EnvioResponse;
import application.dto.FacturaRequest;
import application.dto.FacturaResponse;
import application.dto.FirmaHsmRequest;
import application.dto.PagoBankyRequest;

import java.util.List;

public interface FacturaService {
    FacturaResponse procesarFacturaElectronica(FacturaRequest request, String correoCliente);
    List<FacturaResponse> obtenerDelCliente(String correoCliente);
    FacturaResponse obtenerPorId(String id, String correoCliente);
    String obtenerXml(String id, String correoCliente);
    byte[] obtenerPdf(String id, String correoCliente);
    byte[] obtenerPdfBonito(String id, String correoCliente);
    byte[] obtenerHtmlBonito(String id, String correoCliente);
    EnvioResponse enviarADgtd(String id, String correoCliente);
    List<EnvioResponse> obtenerEnvios(String id, String correoCliente);
    FacturaResponse registrarPagoBanky(String id, PagoBankyRequest request, String correoCliente);
    FacturaResponse registrarFirmaHsm(String id, FirmaHsmRequest request, String correoCliente);
}
