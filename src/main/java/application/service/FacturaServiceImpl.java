package application.service;

import application.dto.FacturaRequest;
import application.mapper.FacturaMapper;
import domain.entity.Factura;
import domain.repository.FacturaRepository;
import infraestructure.xml.generator.FacturaXmlGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    private final FacturaXmlGenerator xmlGenerator;

    public FacturaServiceImpl(FacturaRepository facturaRepository, FacturaMapper facturaMapper, FacturaXmlGenerator xmlGenerator) {
        this.facturaRepository = facturaRepository;
        this.facturaMapper = facturaMapper;
        this.xmlGenerator = xmlGenerator;
    }

    @Override
    public String procesarFacturaElectronica(FacturaRequest request) {
        Factura nuevaFactura = facturaMapper.toEntity(request);
        facturaRepository.save(nuevaFactura);
        String xmlGenerado = xmlGenerator.generarXml(nuevaFactura);
        
        System.out.println("================ XML GENERADO ================");
        System.out.println(xmlGenerado);
        System.out.println("==============================================");

        return "¡Factura procesada, guardada en Mongo y XML v4.3 generado con éxito para: " + nuevaFactura.getEmisor().getNombre() + "!";
    }

    @Override
    public List<Factura> obtenerTodas() {
        return facturaRepository.findAll();
    }

    @Override
    public Optional<Factura> obtenerPorId(String id) {
        return facturaRepository.findById(id);
    }
}