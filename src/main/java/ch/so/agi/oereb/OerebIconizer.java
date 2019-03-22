package ch.so.agi.oereb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OerebIconizer {
    Logger log = LoggerFactory.getLogger(this.getClass());
    
    public void run(String configFilename) throws Exception {
        log.info(configFilename);
        log.info(configFilename.substring(0,4));
        
        File configFile = null; 
        if (configFilename.substring(0,4).equalsIgnoreCase("http")) {
            try {
                String decodedRequest = java.net.URLDecoder.decode(configFilename, "UTF-8");
                CloseableHttpClient httpclient = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods 
                        .build();
                HttpGet get = new HttpGet(new URL(decodedRequest).toURI()); 
                CloseableHttpResponse response = httpclient.execute(get);
                
                Path tempDir = Files.createTempDirectory("oereb_iconizer_");
                File tempFile = Paths.get(tempDir.toAbsolutePath().toFile().getAbsolutePath(), "getstyles.sld").toFile();
                
                InputStream source = response.getEntity().getContent();
                FileUtils.copyInputStreamToFile(source, tempFile);
                
                configFile = tempFile;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                throw new Exception(e);
            }
        } else {
            configFile = new File(configFilename);
        }
        log.info(configFile.getAbsolutePath());
    }
}
