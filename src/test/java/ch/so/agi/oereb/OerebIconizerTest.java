package ch.so.agi.oereb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class OerebIconizerTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    private static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";

    private static String dbusr = "ddluser";
    private static String dbpwd = "ddluser";
    private static String dbdatabase = "oereb";

    @Container
    private static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgisContainerProvider().newInstance()
            .withDatabaseName(dbdatabase).withUsername(dbusr).withPassword(dbpwd).withInitScript("init_postgresql.sql")
            .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));
    
    @Container
    private static GenericContainer qgis = new GenericContainer("sogis/qgis-server-base:3.4")
            .withExposedPorts(80).withClasspathResourceMapping("qgis3", "/data", BindMode.READ_WRITE).waitingFor(Wait.forHttp("/"));
    
    // TODO another approach: postgresqlContainer.addFileSystemBind(scriptsPath + File.separator + "create.sh", "/docker-entrypoint-initdb.d/00_create.sh", BindMode.READ_ONLY);

    @Test
    public void getTypeCodeSymbols_Ok() throws Exception {
        String ipAddress = qgis.getContainerIpAddress();
        String port = String.valueOf(qgis.getFirstMappedPort());
        
        String getStylesRequest = "http://" + ipAddress + ":" + port + "/qgis/singlesymbol?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=singlepolygon&SLD_VERSION=1.1.0";
        log.info(getStylesRequest);
        
        String getLegendGraphicRequest = "http://" + ipAddress + ":" + port + "/qgis/singlesymbol?SERVICE=WMS&REQUEST=GetLegendGraphic&LAYER=singlepolygon&FORMAT=image/png&RULELABEL=false&LAYERTITLE=false&HEIGHT=35&WIDTH=70&SYMBOLHEIGHT=3&SYMBOLWIDTH=6&DPI=300";
        log.info(getLegendGraphicRequest);

        OerebIconizer iconizer = new OerebIconizer();
        List<LegendEntry> legendEntries = iconizer.getSymbolsQgis3(getStylesRequest, getLegendGraphicRequest);
        
        assertEquals(1, legendEntries.size());
        
        String typeCode = "N111".toLowerCase();
        File symbolFile = new File("src/test/data/gruen_und_freihaltezone_innerhalb_bauzone.png");
        BufferedImage symbolFileImage = ImageIO.read(symbolFile);

        String resultTypeCode = null;
        BufferedImage resultImage = null;
        for (LegendEntry entry:  legendEntries) {
            resultTypeCode = entry.getTypeCode();
            resultImage = entry.getSymbol();
            break;
        }
        
        assertEquals(typeCode, resultTypeCode.toLowerCase());
        assertEquals(symbolFileImage.getHeight(), resultImage.getHeight());
        assertEquals(symbolFileImage.getWidth(), resultImage.getWidth());
        assertEquals(symbolFileImage.isAlphaPremultiplied(), resultImage.isAlphaPremultiplied());
    }
    
    // see: https://bugs.openjdk.java.net/browse/JDK-8196123 and https://stackoverflow.com/questions/11153200/with-imageio-write-api-call-i-get-nullpointerexception
    // ImageIO.write throws a NPE if it cannot write the file and not a FileNotFoundException.
    @Test
    public void saveSymbolsToDisk_Permission_Fail() throws Exception {
        String directory = "/";
        String typeCode = "N111".toLowerCase();
        File symbolFile = new File("src/test/data/gruen_und_freihaltezone_innerhalb_bauzone.png");

        LegendEntry entry = new LegendEntry();
        entry.setTypeCode(typeCode);
        entry.setSymbol(ImageIO.read(symbolFile));
        
        List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
        legendEntries.add(entry);
        
        try {
            OerebIconizer iconizer = new OerebIconizer();
            iconizer.saveSymbolsToDisk(legendEntries, directory);

        } catch (java.lang.NullPointerException e) {            
            // do nothing
        }
    }
    
    @Test
    public void saveSymbolsToDisk_Ok(@TempDir Path tempDir) throws Exception {
        String directory = tempDir.toFile().getAbsolutePath();
        String typeCode = "N111".toLowerCase();
        File symbolFile = new File("src/test/data/gruen_und_freihaltezone_innerhalb_bauzone.png");

        LegendEntry entry = new LegendEntry();
        entry.setTypeCode(typeCode);
        entry.setSymbol(ImageIO.read(symbolFile));
        
        List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
        legendEntries.add(entry);
                
        OerebIconizer iconizer = new OerebIconizer();
        iconizer.saveSymbolsToDisk(legendEntries, directory);

        File resultFile = Paths.get(tempDir.toFile().getAbsolutePath(), "N111.png".toLowerCase()).toFile();
        BufferedImage resultImage = ImageIO.read(resultFile);

        assertEquals(ImageIO.read(symbolFile).getHeight(), resultImage.getHeight());
        assertEquals(ImageIO.read(symbolFile).getWidth(), resultImage.getWidth());
        assertEquals(ImageIO.read(symbolFile).isAlphaPremultiplied(), resultImage.isAlphaPremultiplied());
    }
    
    @Test
    public void updateSymbol_Ok() throws Exception {
        String schemaName = "insertsymbols".toLowerCase();
        String tableName = "test".toLowerCase();
        String dbQTable = schemaName+"."+tableName;
        String typeCodeAttrName = "artcode";
        String symbolAttrName = "symbol";
        String legendTextAttrName = "legendetext";
        
        Connection con = null;
        
        String typeCode = "N390";
        File symbolFile = new File("src/test/data/weitere_schutzzone_ausserhalb_bauzone.png");
        String legendText = "weitere Schutzzonen ausserhalb Bauzonen";
  
        try {
            // Prepare database: create table.
            con = connect(postgres);
            createOrReplaceSchema(con, schemaName);
            
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE " + dbQTable + "(t_id SERIAL, artcode TEXT, symbol BYTEA, legendetext TEXT);");
            s1.execute("INSERT INTO " + dbQTable + "(artcode) VALUES('" + typeCode +"');");
            s1.close();
            con.commit();
            closeConnection(con);
                        
            // Insert typecode and symbol with the iconizer.
            List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
            LegendEntry entry = new LegendEntry();
            entry.setTypeCode(typeCode);
            entry.setSymbol(ImageIO.read(symbolFile));
            entry.setLegendText(legendText);
            legendEntries.add(entry);
                        
            OerebIconizer iconizer = new OerebIconizer();
            int count = iconizer.updateSymbols(legendEntries, postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), dbQTable, typeCodeAttrName, symbolAttrName, legendTextAttrName);

            
            // Check if everything is ok.
            con = connect(postgres);
            Statement s2 = con.createStatement();
            ResultSet rs = s2.executeQuery("SELECT artcode, symbol, legendetext FROM " + dbQTable);
            
            if(!rs.next()) {
                fail();
            }
            
            assertEquals(1, count);
            
            assertEquals(typeCode, rs.getString(1));
            
            System.out.println(rs.getBytes(2));
            
            // TODO: Compare images: Is there a smarter approach?
            ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBytes(2));
            BufferedImage bim = ImageIO.read(bis);
            
            File outputfile = new File("/Users/stefan/tmp/image.png");
            ImageIO.write(bim, "png", outputfile);

            
            assertEquals(ImageIO.read(symbolFile).getHeight(), bim.getHeight());
            assertEquals(ImageIO.read(symbolFile).getWidth(), bim.getWidth());
            assertEquals(ImageIO.read(symbolFile).isAlphaPremultiplied(), bim.isAlphaPremultiplied());
            
            assertEquals(legendText, rs.getString(3));
            
            if(rs.next()) {
                fail();
            }
            
            rs.close();
            s2.close();
        } finally {
            closeConnection(con);
        }
    }

    /*
     * Tries to update a symbol which does not exist in the database.
     */
    @Test
    public void updateNonExistingSymbol_Ok() throws Exception {
        String schemaName = "insertsymbols".toLowerCase();
        String tableName = "test".toLowerCase();
        String dbQTable = schemaName+"."+tableName;
        String typeCodeAttrName = "artcode";
        String symbolAttrName = "symbol";
        String legendTextAttrName = "legendetext";
        
        Connection con = null;
        
        String typeCode = "N390";
        File symbolFile = new File("src/test/data/weitere_schutzzone_ausserhalb_bauzone.png");
        String legendText = "weitere Schutzzonen ausserhalb Bauzonen";
  
        try {
            // Prepare database: create table.
            con = connect(postgres);
            createOrReplaceSchema(con, schemaName);
            
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE " + dbQTable + "(t_id SERIAL, artcode TEXT, symbol BYTEA, legendetext TEXT);");
            s1.execute("INSERT INTO " + dbQTable + "(artcode) VALUES('" + typeCode +"');");
            s1.close();
            con.commit();
            closeConnection(con);
                        
            // Insert typecode and symbol with the iconizer.
            List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
            LegendEntry entryNonExisting = new LegendEntry();
//            entry.setTypeCode(typeCode);
            entryNonExisting.setTypeCode("N999");
            entryNonExisting.setSymbol(ImageIO.read(symbolFile));
            entryNonExisting.setLegendText(legendText);
            legendEntries.add(entryNonExisting);
            
            LegendEntry entryExisting = new LegendEntry();
            entryExisting.setTypeCode(typeCode);
            entryExisting.setSymbol(ImageIO.read(symbolFile));
            entryExisting.setLegendText(legendText);
            legendEntries.add(entryExisting);

            OerebIconizer iconizer = new OerebIconizer();
            int count = iconizer.updateSymbols(legendEntries, postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), dbQTable, typeCodeAttrName, symbolAttrName, legendTextAttrName);

            //System.out.println(count);
            
            assertEquals(1, count);
        } finally {
            closeConnection(con);
        }
    }    
    
    private Connection connect(PostgreSQLContainer postgres) {
        Connection con = null;
        try {
            String url = postgres.getJdbcUrl();
            String user = postgres.getUsername();
            String password = postgres.getPassword();

            con = DriverManager.getConnection(url, user, password);

            con.setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return con;
    }
    
    private void createOrReplaceSchema(Connection con, String schemaName) {

        try {
            Statement s = con.createStatement();
            s.addBatch(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
            s.addBatch("CREATE SCHEMA " + schemaName);
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO dmluser", schemaName));
            s.addBatch(String.format("GRANT USAGE ON SCHEMA %s TO readeruser", schemaName));
            s.executeBatch();
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void closeConnection(Connection con) {
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
