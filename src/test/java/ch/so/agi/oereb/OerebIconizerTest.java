package ch.so.agi.oereb;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OerebIconizerTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void dummy() throws Exception {
        OerebIconizer iconizer = new OerebIconizer();
        iconizer.getSymbolsQgis3("http://localhost:8380/qgis/npl?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=npl&SLD_VERSION=1.1.0", 
                "http://localhost:8380/qgis/npl?SERVICE=WMS&REQUEST=GetLegendGraphic&LAYER=npl&FORMAT=image/png&RULELABEL=false&LAYERTITLE=false&HEIGHT=35&WIDTH=70&SYMBOLHEIGHT=3&SYMBOLWIDTH=6&DPI=300");
        
        System.out.println("fubar");
    }

}
