package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface SymbolTypeCodeBuilder {
    public List<LegendEntry> build() throws Exception;
}
