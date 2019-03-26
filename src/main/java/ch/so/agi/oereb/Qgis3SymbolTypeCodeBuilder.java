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


public class Qgis3SymbolTypeCodeBuilder implements SymbolTypeCodeBuilder {
    Logger log = LoggerFactory.getLogger(this.getClass());

    private String configFileName = null;
    String legendGraphicUrl = null;
    
    public Qgis3SymbolTypeCodeBuilder(String configFileName, String legendGraphicUrl) {
        this.configFileName = configFileName;
        this.legendGraphicUrl = legendGraphicUrl;
    }
    
    @Override
    public Map<String, BufferedImage> build() throws Exception {
        HashMap<String,BufferedImage> typeCodeSymbolMap = new HashMap<String,BufferedImage>();
        
        File configFile = Utilities.handleConfigInput(configFileName);
        if (configFile == null) {
            throw new Exception("configuration file is null");
        }
               
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
        }};
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);

        XPathExpression expr = xpath.compile("//se:FeatureTypeStyle/se:Rule");

        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
       
        List<SimpleRule> simpleRules = new ArrayList<SimpleRule>();
        for (int i = 0; i < nodes.getLength(); i++) {
            SimpleRule simpleRule = evaluateRule(nodes.item(i));
            simpleRules.add(simpleRule);
        }

        // Get the symbols from the wms server.
        for (SimpleRule simpleRule : simpleRules) {
            String ruleName = URLEncoder.encode(simpleRule.getRuleName(), "UTF-8");
            String requestUrl = legendGraphicUrl + "&RULE=" + ruleName;
            
            log.debug(requestUrl);
            BufferedImage symbol = Utilities.getRemoteImage(requestUrl);
                        
            typeCodeSymbolMap.put(simpleRule.getTypeCodeValue(), symbol);
        }
        
        log.debug(typeCodeSymbolMap.toString());
        return typeCodeSymbolMap;
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
