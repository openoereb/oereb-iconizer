package ch.so.agi.oereb;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Qgis3StyleConfigParser implements StyleConfigParser {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, String> parse(File configFile) {
        log.info(configFile.getAbsolutePath());
        
        
        
        return null;
    }

}
