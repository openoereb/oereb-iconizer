package ch.so.agi.oereb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OerebIconizer {
    Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Gets all the symbols and the according type code from a QGIS 3 wms server.
     * 
     * @param configFileName GetStyles request url (= SLD file).
     * @param legendGraphicUrl GetLegendGraphic request url with the vendor specific parameters for single symbol support. 
     * The RULE parameter is added dynamically. The LAYER parameter must be included.
     * @throws Exception
     */
    public Map<String,BufferedImage> getSymbolsQgis3(String configFileName, String legendGraphicUrl) throws Exception {                
        SymbolTypeCodeBuilder styleConfigBuilder = new Qgis3SymbolTypeCodeBuilder(configFileName, legendGraphicUrl);
        Map<String,BufferedImage> typeCodeSymbols = styleConfigBuilder.build();
        return typeCodeSymbols;
    }
    
    /**
     * Saves symbols to disk. The type code is the file name.
     * 
     * @param typeCodeSymbols Map with the type code and the symbols. 
     * @param directory Directory to save the symbols.
     * @throws IOException 
     * @throws Exception
     */
    public void saveSymbolsToDisk(Map<String,BufferedImage> typeCodeSymbols, String directory) throws Exception {
        for (String key : typeCodeSymbols.keySet()) {
            File symbolFile = Paths.get(directory, key + ".png").toFile();
            ImageIO.write(typeCodeSymbols.get(key), "png", symbolFile);
        }
    }
    
    /**
     * Updates the symbols in a database table of the according type code.
     * 
     * @param typeCodeSymbols  Map with the type code and the symbols.
     * @param jdbcUrl JDBC url
     * @param dbUsr User name
     * @param dbPwd Password
     * @param dbQTable Qualified table name.
     * @param typeCodeAttrName Name of the type code attribute in the database.
     * @param symbolAttrName Name of the symbol attribute in the database.
     * @throws Exception
     */
    public int updateSymbols(Map<String,BufferedImage> typeCodeSymbols, String jdbcUrl, String dbUsr, String dbPwd, String dbQTable, String typeCodeAttrName, String symbolAttrName) throws Exception {
        Connection conn = getDbConnection(jdbcUrl, dbUsr, dbPwd);
        
        PreparedStatement pstmt = null;
        String updateSql = "UPDATE " + dbQTable + " SET " + symbolAttrName + " = ? WHERE " + typeCodeAttrName + " = ?;";

        try {
            pstmt = conn.prepareStatement(updateSql);
            for (String key : typeCodeSymbols.keySet()) {
                log.debug(key + " " + typeCodeSymbols.get(key));
                                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(typeCodeSymbols.get(key), "png", baos);
                byte[] symbolInByte = baos.toByteArray();
                pstmt.setBytes(1, symbolInByte);
                
                pstmt.setString(2, key);
            }
            int count = pstmt.executeUpdate();
            
            if (pstmt != null) {
                pstmt.close();
            }
            
            conn.close();
            
            return count;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e);
        }
    }
        
    private Connection getDbConnection(String jdbcUrl, String dbUsr, String dbPwd) {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(jdbcUrl, dbUsr, dbPwd);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return conn;
    }
}
