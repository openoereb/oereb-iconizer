package ch.so.agi.oereb;

import java.io.File;
import java.util.Map;

public interface StyleConfigParser {
    public Map<String, String> parse(File configFile) throws Exception;
}
