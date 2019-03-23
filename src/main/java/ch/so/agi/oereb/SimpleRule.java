package ch.so.agi.oereb;

public class SimpleRule {
    private String ruleName;
    private String typeCodeValue;
    
    public SimpleRule(String ruleName, String typeCodeValue) {
        this.ruleName = ruleName;
        this.typeCodeValue = typeCodeValue;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    public String getTypeCodeValue() {
        return typeCodeValue;
    }
    public void setTypeCodeValue(String typeCodeValue) {
        this.typeCodeValue = typeCodeValue;
    }
}
