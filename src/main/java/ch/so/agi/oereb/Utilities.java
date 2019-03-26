package ch.so.agi.oereb;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

public class Utilities {
    /**
     * If configFilename is a remote file, save it locally.
     * 
     * @param configFilename The configuration file (local or remote)
     * @return File
     * @throws Exception
     */
    public static File handleConfigInput(String configFileName) throws Exception {
        File configFile = null; 
        if (configFileName.substring(0,4).equalsIgnoreCase("http")) {
            try {
                String decodedRequest = java.net.URLDecoder.decode(configFileName, "UTF-8");
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
                throw new Exception(e);
            }
        } else {
            configFile = new File(configFileName);
        }
        return configFile;
    }
    
    /**
     * Saves a remote image as BufferedImage.
     * 
     * @param url Url to a remote image.
     * @return Remote image as BufferedImage
     * @throws Exception
     */
    public static BufferedImage getRemoteImage(String url) throws Exception {
        try {
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods 
                    .build();
            HttpGet get = new HttpGet(new URL(url).toURI()); 
            CloseableHttpResponse response = httpclient.execute(get);
            
            InputStream inputStream = response.getEntity().getContent();
            BufferedImage image = ImageIO.read(inputStream);

            // force 3 band image
            BufferedImage fixedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) fixedImage.getGraphics();
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, image.getWidth(), image.getHeight());   
            g.drawImage(image, 0, 0, null);               
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(fixedImage, "png", baos); 
            baos.flush();
            baos.close();          
            
            return fixedImage;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
