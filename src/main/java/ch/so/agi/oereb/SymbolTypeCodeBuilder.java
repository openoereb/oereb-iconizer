package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface SymbolTypeCodeBuilder {
//    @Deprecated
//    public Map<String, BufferedImage> build() throws Exception;
    
    public List<LegendEntry> build() throws Exception;
}
