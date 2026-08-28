package infraestructure.xml.validator;

import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SignedXmlMatcher {

    private static final String XMLDSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    /**
     * Comprueba que el documento firmado conserva el mismo contenido de negocio
     * que el XML original. Se ignora exclusivamente el nodo XMLDSig Signature,
     * declaraciones xmlns y un atributo Id agregado por el firmador.
     */
    public boolean correspondeAlOriginal(String xmlOriginal, String xmlFirmado) {
        if (xmlOriginal == null || xmlOriginal.isBlank()
                || xmlFirmado == null || xmlFirmado.isBlank()) {
            return false;
        }

        try {
            Document original = parsearSeguro(xmlOriginal);
            Document firmado = parsearSeguro(xmlFirmado);
            return elementosEquivalentes(
                    original.getDocumentElement(),
                    firmado.getDocumentElement()
            );
        } catch (Exception ex) {
            return false;
        }
    }

    private Document parsearSeguro(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private boolean elementosEquivalentes(Element original, Element firmado) {
        if (!igual(nombreLocal(original), nombreLocal(firmado))) return false;
        if (!igual(normalizarNamespace(original.getNamespaceURI()),
                normalizarNamespace(firmado.getNamespaceURI()))) return false;

        if (!atributos(original, false).equals(atributos(firmado, true))) {
            return false;
        }

        List<NodoComparable> hijosOriginal = hijosComparables(original, false);
        List<NodoComparable> hijosFirmado = hijosComparables(firmado, true);
        if (hijosOriginal.size() != hijosFirmado.size()) return false;

        for (int i = 0; i < hijosOriginal.size(); i++) {
            NodoComparable a = hijosOriginal.get(i);
            NodoComparable b = hijosFirmado.get(i);
            if (a.elemento != null || b.elemento != null) {
                if (a.elemento == null || b.elemento == null) return false;
                if (!elementosEquivalentes(a.elemento, b.elemento)) return false;
            } else if (!igual(a.texto, b.texto)) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> atributos(Element element, boolean documentoFirmado) {
        Map<String, String> result = new LinkedHashMap<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI())
                    || "xmlns".equals(attr.getName())
                    || attr.getName().startsWith("xmlns:")) {
                continue;
            }
            // Algunos firmadores agregan un Id técnico al elemento referenciado.
            if (documentoFirmado && "Id".equalsIgnoreCase(nombreLocal(attr))) {
                continue;
            }
            String key = normalizarNamespace(attr.getNamespaceURI()) + "|" + nombreLocal(attr);
            result.put(key, attr.getValue());
        }
        return result;
    }

    private List<NodoComparable> hijosComparables(Element parent, boolean documentoFirmado) {
        List<NodoComparable> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (documentoFirmado && esFirmaXmlDsig(element)) continue;
                result.add(new NodoComparable(element, null));
            } else if (node.getNodeType() == Node.TEXT_NODE
                    || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                String texto = normalizarTexto(node.getTextContent());
                if (!texto.isBlank()) result.add(new NodoComparable(null, texto));
            }
        }
        return result;
    }

    private boolean esFirmaXmlDsig(Element element) {
        return "Signature".equals(nombreLocal(element))
                && (XMLDSIG_NS.equals(element.getNamespaceURI())
                || element.getNamespaceURI() == null
                || element.getNamespaceURI().isBlank());
    }

    private String nombreLocal(Node node) {
        return node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
    }

    private String normalizarNamespace(String value) {
        return value == null ? "" : value;
    }

    private String normalizarTexto(String value) {
        if (value == null) return "";
        return value.replaceAll("\\s+", " ").trim();
    }

    private boolean igual(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private record NodoComparable(Element elemento, String texto) {
    }
}
