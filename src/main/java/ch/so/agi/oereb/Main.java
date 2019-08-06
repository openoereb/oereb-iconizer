package ch.so.agi.oereb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static Logger log = LoggerFactory.getLogger(Main.class);

    private static String dbhost;
    private static String dbdatabase;
    private static String dbport = "5432";
    private static String dbusr;
    private static String dbpwd;
    private static String sldUrl;
    private static String legendGraphicUrl;
    private static String dbQTable;
    private static String typeCodeAttrName;
    private static String symbolAttrName;
    private static String legendTextAttrName;
    private static String downloadDir;
    
    public static void main(String[] args) throws Exception {
        int argi = 0;
        for(;argi<args.length;argi++) {
            String arg = args[argi];
            
            if(arg.equals("--dbhost")) {
                argi++;
                dbhost = args[argi];
            } else if (arg.equals("--dbdatabase")) {
                argi++;
                dbdatabase = args[argi];
            } else if (arg.equals("--dbport")) {
                argi++;
                dbport = args[argi];
            } else if (arg.equals("--dbusr")) {
                argi++;
                dbusr = args[argi];
            } else if (arg.equals("--dbpwd")) {
                argi++;
                dbpwd = args[argi];
            } else if (arg.equals("--sldUrl")) {
                argi++;
                sldUrl = args[argi];
            } else if (arg.equals("--legendGraphicUrl")) {
                argi++;
                legendGraphicUrl = args[argi];
            } else if (arg.equals("--dbQTable")) {
                argi++;
                dbQTable = args[argi];
            } else if (arg.equals("--typeCodeAttrName")) {
                argi++;
                typeCodeAttrName = args[argi];
            } else if (arg.equals("--symbolAttrName")) {
                argi++;
                symbolAttrName = args[argi];
            } else if (arg.equals("--legendTextAttrName")) {
                argi++;
                legendTextAttrName = args[argi];
            } else if (arg.equals("--downloadDir")) {
                argi++;
                downloadDir = args[argi];
            } else if (arg.equals("--help")) {
                System.err.println();
                System.err.println("--dbhost                Database host.");
                System.err.println("--dbdatabase            Database name.");
                System.err.println("--dbport                Database port.");
                System.err.println("--dbusr                 Database user name.");
                System.err.println("--dbpwd                 Database user password.");
                System.err.println("--sldUrl                GetStyles request.");
                System.err.println("--legendGraphicUrl      GetLegendGraphic request incl. RULELABEL, LAYERTITLE, HEIGHT, WIDTH, SYMBOLHEIGHT, SYMBOLWIDTH and DPI parameter.");
                System.err.println("--dbQTable              Qualified table name.");
                System.err.println("--typeCodeAttrName      Name of type code attribute in table.");
                System.err.println("--symbolAttrName        Name of symbol attribute in table.");
                System.err.println("--legendTextAttrName    Name of legend text entry in table.");
                System.err.println("--downloadDir           Download directory.");
                System.err.println();
                return;
            }
        }
        
        if (sldUrl == null || legendGraphicUrl == null) {
            log.error("sldUrl and legendGraphicUrl are required.");
            System.exit(2);
        }
        
        if (downloadDir == null) {
            if (dbhost == null || dbdatabase == null || dbport == null || dbusr == null || dbpwd == null 
                    //|| dbQTable == null || typeCodeAttrName == null || symbolAttrName == null || legendTextAttrName == null) {
                    || dbQTable == null || typeCodeAttrName == null || symbolAttrName == null) {
                log.error("Missing database, table oder attribute parameters.");
                System.exit(2);
            } else {
                OerebIconizer iconizer = new OerebIconizer();
                List<LegendEntry> legendEntries =  iconizer.getSymbolsQgis3Simple(sldUrl, legendGraphicUrl);
                String jdbcUrl = "jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbdatabase;
                iconizer.updateSymbols(legendEntries, jdbcUrl, dbusr, dbpwd, dbQTable, typeCodeAttrName, symbolAttrName, legendTextAttrName, false); // TODO: expose last paramater
            }
        }
        
        if (downloadDir != null) {
            OerebIconizer iconizer = new OerebIconizer();
            List<LegendEntry> legendEntries =  iconizer.getSymbolsQgis3Simple(sldUrl, legendGraphicUrl);
            iconizer.saveSymbolsToDisk(legendEntries, downloadDir);
            return;
        }
    }
}
