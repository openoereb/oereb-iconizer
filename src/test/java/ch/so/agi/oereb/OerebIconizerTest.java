package ch.so.agi.oereb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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
        
        String getStylesRequest = "http://" + ipAddress + ":" + port + "/qgis/npl?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=npl&SLD_VERSION=1.1.0";
        log.info(getStylesRequest);
        
        String getLegendGraphicRequest = "http://" + ipAddress + ":" + port + "/qgis/npl?SERVICE=WMS&REQUEST=GetLegendGraphic&LAYER=npl&FORMAT=image/png&RULELABEL=false&LAYERTITLE=false&HEIGHT=35&WIDTH=70&SYMBOLHEIGHT=3&SYMBOLWIDTH=6&DPI=300";
        log.info(getLegendGraphicRequest);

        OerebIconizer iconizer = new OerebIconizer();
        Map<String, BufferedImage> typeCodeSymbols = iconizer.getSymbolsQgis3(getStylesRequest, getLegendGraphicRequest);
        
        // TODO:
        // - very simple qgis project with only one rule (symbol).
        // - very few data
        // - assert / test
        // - use npl for integration test in gretl
    }

    @Test
    public void saveSymbolsToDisk_Ok(@TempDir Path tempDir) throws Exception {
        String directory = tempDir.toFile().getAbsolutePath();
        String typeCode = "N390".toLowerCase();
        File symbolFile = new File("src/test/data/weitere_schutzzone_ausserhalb_bauzone.png");

        Map<String,BufferedImage> typeCodeSymbols = new HashMap<String,BufferedImage>();
        typeCodeSymbols.put(typeCode, ImageIO.read(symbolFile));
        
        OerebIconizer iconizer = new OerebIconizer();
        iconizer.saveSymbolsToDisk(typeCodeSymbols, directory);

        File resultFile = Paths.get(tempDir.toFile().getAbsolutePath(), "N390.png".toLowerCase()).toFile();
        BufferedImage resultImage = ImageIO.read(resultFile);

        assertEquals(ImageIO.read(symbolFile).getHeight(), resultImage.getHeight());
        assertEquals(ImageIO.read(symbolFile).getWidth(), resultImage.getWidth());
        assertEquals(ImageIO.read(symbolFile).getColorModel(), resultImage.getColorModel());
    }
    
    @Test
    public void insertSymbol_Ok() throws Exception {
        String schemaName = "insertsymbols".toLowerCase();
        String tableName = "test".toLowerCase();
        String dbQTable = schemaName+"."+tableName;
        String typeCodeAttrName = "artcode";
        String symbolAttrName = "symbol";
        
        Connection con = null;
        
        String typeCode = "N390";
        File symbolFile = new File("src/test/data/weitere_schutzzone_ausserhalb_bauzone.png");
  
        try {
            // Prepare database: create table.
            con = connect(postgres);
            createOrReplaceSchema(con, schemaName);
            
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE " + dbQTable + "(t_id SERIAL, artcode TEXT, symbol BYTEA);");
            // TODO: INSERT row with missing symbol
            s1.close();
            con.commit();
            closeConnection(con);
                        
            // Insert typecode and symbol with the iconizer.
            Map<String,BufferedImage> typeCodeSymbols = new HashMap<String,BufferedImage>();
            typeCodeSymbols.put(typeCode, ImageIO.read(symbolFile));
            
            OerebIconizer iconizer = new OerebIconizer();
            iconizer.insertSymbols(typeCodeSymbols, postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), dbQTable, typeCodeAttrName, symbolAttrName);

            // Check if everything is ok.
            con = connect(postgres);
            Statement s2 = con.createStatement();
            ResultSet rs = s2.executeQuery("SELECT artcode, symbol FROM " + dbQTable);
            
            if(!rs.next()) {
                fail();
            }
            
            assertEquals(typeCode, rs.getString(1));
            
            // TODO: Compare images: Is there a smarter approach?
            ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBytes(2));
            BufferedImage bim = ImageIO.read(bis);
            assertEquals(ImageIO.read(symbolFile).getHeight(), bim.getHeight());
            assertEquals(ImageIO.read(symbolFile).getWidth(), bim.getWidth());
            assertEquals(ImageIO.read(symbolFile).getColorModel(), bim.getColorModel());
            
            if(rs.next()) {
                fail();
            }
            
            rs.close();
            s2.close();
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
