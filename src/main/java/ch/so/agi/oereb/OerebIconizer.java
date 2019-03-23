package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OerebIconizer {
    Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 
     * @param configFileName GetStyles request url (= SLD file).
     * @param legendGraphicUrl GetLegendGraphic request url with the vendor specific parameters for single symbol support. 
     * The RULE parameter is added dynamically. The LAYER parameter must be included.
     * @throws Exception
     */
    public Map<String,BufferedImage> getSymbolsQgis3(String configFileName, String legendGraphicUrl) throws Exception {        
//        try {
//            SymbolTypeCodeBuilders.valueOf(vendor); 
//        } catch (IllegalArgumentException e) {
//            throw new Exception("No StyleConfigParser implementation found for: " + vendor);
//        }
        
        // Save remote file if necessary.
//        File configFile = handleConfigInput(configFilename);
//        log.info(configFile.getAbsolutePath());
        
        // Create the type code value / symbol map.
        SymbolTypeCodeBuilder styleConfigBuilder = new Qgis3SymbolTypeCodeBuilder(configFileName, legendGraphicUrl);
        Map<String,BufferedImage> typeCodeSymbols = styleConfigBuilder.build();
        return typeCodeSymbols;
        
        // Insert symbols into database.
//        insertSymbols(typeCodeSymbols);
        
//        SymbolTypeCodeBuilder styleConfigParser = null;        
//        if (SymbolTypeCodeBuilders.valueOf(vendor).equals(SymbolTypeCodeBuilders.QGIS3)) {
//            styleConfigParser = new Qgis3SymbolTypeCodeBuilder();
////            styleConfigParser.build(configFile);
//        } 
    }
    
    public void insertSymbols(Map<String,BufferedImage> typeCodeSymbols, String jdbcUrl, String dbUsr, String dbPwd, String dbQTable) {
        for (String key : typeCodeSymbols.keySet()) {
            System.out.println(key + " " + typeCodeSymbols.get(key));
        }
    }
}
