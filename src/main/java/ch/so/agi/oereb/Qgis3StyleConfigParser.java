package ch.so.agi.oereb;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Qgis3StyleConfigParser implements StyleConfigParser {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, String> parse(File configFile) throws Exception {
        log.info(configFile.getAbsolutePath());
        
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document configuration = builder.parse(configFile);
        
        log.info(configuration.getDocumentURI());

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
//        XPathExpression expr = xpath.compile("//FeatureTypeStyle/Rule/Name/text()");
        XPathExpression expr = xpath.compile("//FeatureTypeStyle/Rule");

        Object result = expr.evaluate(configuration, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
//            System.out.println(nodes.item(i).getNodeValue()); 
            System.out.println(nodes.item(i).getChildNodes()); 
            
            NodeList ruleChildNodes = nodes.item(i).getChildNodes();
            for (int j = 0; j < ruleChildNodes.getLength(); j++) {
                System.out.println(ruleChildNodes.item(j).getNodeName());
            }
 
        }

        return null;
    }
    
    private void evaluateRule(Node node) {
        
    }
    
    private void evaluateFilter(Node node) {
        
    }

}
