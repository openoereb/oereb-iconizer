package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.util.Map;

public interface SymbolTypeCodeBuilder {
    public Map<String, BufferedImage> build() throws Exception;
}
