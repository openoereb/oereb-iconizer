package ch.so.agi.oereb;

import java.awt.image.BufferedImage;

public class LegendEntry {    
    private BufferedImage symbol;
    private String typeCode;
    private String legendText;

    public BufferedImage getSymbol() {
        return symbol;
    }
    public void setSymbol(BufferedImage symbol) {
        this.symbol = symbol;
    }
    public String getTypeCode() {
        return typeCode;
    }
    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }
    public String getLegendText() {
        return legendText;
    }
    public void setLegendText(String legendText) {
        this.legendText = legendText;
    }    
}
