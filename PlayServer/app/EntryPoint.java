import org.apache.commons.codec.binary.*;
import play.Logger;
import play.core.server.NettyServer;
import scala.Option;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A MainClass wrapper for running the Play Server with a System tray and the ability to
 * configure system properties from here, such as keystore, etc.
 */
public class EntryPoint {

    private NettyServer server;
    private boolean serverRunning = false;

    public EntryPoint(String args[]) {

        try {
            System.setProperty("http.port", "9001");
            System.setProperty("https.port", "9443");
            File file = new File("keystore.jks");
            if(!file.exists()){
                InputStream is = EntryPoint.class.getResourceAsStream("keystore.jks");
                File keystore = new File("keystore.jks");
                Files.copy(is, keystore.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.info("Keystore does not exist locally. Copying into local directory.");
            }
            else{
                Logger.info("Keystore exists locally. Using local keystore.");
            }
            System.setProperty("https.keyStore", "keystore.jks");
            // see run.sh
            System.setProperty("https.keyStorePassword", "replacethis");
            Option<NettyServer> serverOption = NettyServer.createServer(new File(""));
            server = serverOption.get();
            if (server != null)
                serverRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown Netty
     */
    private void shutdownServer() {
        if (server != null)
            server.stop();
        serverRunning = false;
        removePid();

    }

    /**
     * Remove the PID file
     */
    private void removePid(){
        File file = new File("PID");
        if (file.exists()) {
            file.delete();
        }
    }


    public static void main(String args[]) {
        new EntryPoint(args);
    }

}
