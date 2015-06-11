
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import sftp.SimpleSFTPServer;

import java.io.IOException;

/**
 * This class configures the environment
 */
public class ServerConfig extends GlobalSettings {

    private SimpleSFTPServer sftpServer;

    @Override
    public void onStart(Application app) {

        final int sftpPort = Play.application().configuration().getInt("sftp.port");
        final String sftpDir = Play.application().configuration().getString("sftp.sftpDir");

        sftpServer = new SimpleSFTPServer(sftpPort, sftpDir);

        try {
            Logger.info(" SFTPserver port " + sftpServer.getPort() + " sftp directory " + sftpServer.getDefaultSFTPir());
            sftpServer.start();
        } catch (IOException e) {
            Logger.info("Failed to start SFTP server on port " + sftpServer.getPort());
            e.printStackTrace();
        }
    }

    @Override
    public void onStop(Application app) {

        //Stop the sftpServer if its running.
        if(!sftpServer.isShuttingDown()) {
            try {
                Logger.info("Stopping SFTP server");
                sftpServer.stop(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.info("Failed to stop SFTP server");
            }
        }
        super.onStop(app);
    }
}
