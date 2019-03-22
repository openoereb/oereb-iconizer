package ch.so.agi.oereb;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OerebIconizerTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void dummy() throws Exception {
        OerebIconizer iconizer = new OerebIconizer();
        iconizer.run("http://localhost/qgis/npl?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=npl&SLD_VERSION=1.1.0");
        
        
        
        
        
        System.out.println("fubar");
    }

}
