package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Qgis3SimpleSymbolTypeCodeBuilder implements SymbolTypeCodeBuilder {
    Logger log = LoggerFactory.getLogger(this.getClass());
    
    String geometryType;

    private String configFileName = null;
    String legendGraphicUrl = null;
    
    public Qgis3SimpleSymbolTypeCodeBuilder(String configFileName, String legendGraphicUrl) {
        this.configFileName = configFileName;
        this.legendGraphicUrl = legendGraphicUrl;
    }
    
    @Override
    public List<LegendEntry> build() throws Exception {
        List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
        
        File configFile = Utilities.handleConfigInput(configFileName);
        if (configFile == null) {
            throw new Exception("configuration file is null");
        }
        
        //log.info(configFile.getAbsolutePath());
               
        // Find all Rules first. Then process them to find the rule name and
        // the type code value.
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document document = builder.parse(configFile);
        
        log.info(document.getDocumentURI());

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        HashMap<String, String> prefMap = new HashMap<String, String>() {{
            put("se", "http://www.opengis.net/se");
            put("ogc", "http://www.opengis.net/ogc");
            put("sld", "http://www.opengis.net/sld");
        }};
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);

//        XPathExpression exprName = xpath.compile("//sld:NamedLayer/se:Name");
//        Object resultName = exprName.evaluate(document, XPathConstants.NODESET);
//        NodeList nodesName = (NodeList) resultName;
//        
////        log.info(String.valueOf(nodesName.getLength()));
//
//        String layerName = nodesName.item(0).getTextContent();
//        if (layerName.contains(".Flaeche")) {
//            geometryType = "Flaeche";
//        } else if (layerName.contains(".Linie")) {
//            geometryType = "Linie";
//        } else if (layerName.contains(".Punkt")) {
//            geometryType = "Punkt";
//        } else {
//            geometryType = null;
//        }
////        log.info(geometryType);
        
        XPathExpression expr = xpath.compile("//se:FeatureTypeStyle/se:Rule");

        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
       
        for (int i = 0; i < nodes.getLength(); i++) {
            SimpleRule simpleRule = evaluateRule(nodes.item(i));
            String typeCodeValue = simpleRule.getTypeCodeValue();            
            String ruleName = URLEncoder.encode(simpleRule.getRuleName(), "UTF-8");
            //log.info(ruleName);
            
            String requestUrl = legendGraphicUrl + "&RULE=" + ruleName;
            log.debug(requestUrl);
            
            BufferedImage symbol = Utilities.getRemoteImage(requestUrl);

            LegendEntry legendEntry = new LegendEntry();
            legendEntry.setTypeCode(typeCodeValue);
            legendEntry.setLegendText(simpleRule.getRuleName());
            legendEntry.setSymbol(symbol);
            legendEntry.setGeometryType(geometryType);
            
            legendEntries.add(legendEntry);
        }
        return legendEntries;
    }
    
    /*
     * A Rule must have a Name and a PropertyIsEqualTo filter.
     */
    private SimpleRule evaluateRule(Node node) throws Exception {
        String ruleName = null;
        String typeCodeValue = null;

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
              
            if (childNode.getLocalName() != null && childNode.getLocalName().equalsIgnoreCase("Name")) {
                ruleName = childNode.getTextContent();
            }
            
            if (childNode.getLocalName() != null && childNode.getLocalName().equalsIgnoreCase("Filter")) {
                typeCodeValue = evaluateFilter(childNode);
            }
        }
        
        if (ruleName == null || typeCodeValue == null) {
            throw new Exception("rule name or typecode value not found");
        }
        log.debug(ruleName + " " + typeCodeValue);
        return new SimpleRule(ruleName, typeCodeValue);
    }
    
    /*
     * This is where we are very very specific: the typecode of the oereb-rahmenmodell is the literal of
     * the PropertyIsEqualTo filter.
     */
    private String evaluateFilter(Node node) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if (childNode.getLocalName() != null && childNode.getLocalName().equalsIgnoreCase("PropertyIsEqualTo")) {
                NodeList filterChildNodes = childNode.getChildNodes();
                for (int j = 0; j < filterChildNodes.getLength(); j++) {
                    Node filterChildNode = filterChildNodes.item(j);
                    if (filterChildNode.getLocalName() != null && filterChildNode.getLocalName().equalsIgnoreCase("Literal")) {
                        String typeCodeValue = filterChildNode.getTextContent();
                        log.debug(typeCodeValue);
                        return typeCodeValue;
                    } 
                }
            }            
        }
        return null;
    }
}
