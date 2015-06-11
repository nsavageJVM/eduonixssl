package sftp;


import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.SessionListener;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.SessionFactory;
import org.apache.sshd.server.sftp.SftpSubsystem;
import play.Logger;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 simpple sftp server
 *
 */
public class SimpleSFTPServer {

    private int port;
    private String defaultSFTPDir;
    private SshServer sshd;
    private SessionFactory sessionFactory;
    private Map<String, List<Session>> simpleSession;

    /**
     * Default constructor for the SFTP server to be used by the system
     * @param port - Port that the SFTP server will use. Anything under 1000 usually requires sudo
     * @param defaultHomeDir - Where the uploaded media should be stored
     */
    public SimpleSFTPServer(int port, String defaultSFTPDir){
        this.port = port;
        this.defaultSFTPDir = defaultSFTPDir;
        simpleSession = new HashMap<String, List<Session>>();

        init();
    }

    /**
     * Initialize the SSHD server with he provided settings
     */
    private void init(){

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        sshd.setCommandFactory(new ScpCommandFactory());

        sshd.setPasswordAuthenticator(new SimplePasswordAuthenticator());

        sessionFactory = new SessionFactory();
        sessionFactory.addListener(new SimpleSessionListener());
        sshd.setSessionFactory(sessionFactory);

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);

    }

    /**
     * Add user session to the session map
     * @param user to be added
     * @param session to be added to the map
     */
    private void addToSession(String user, Session session){

        if(user == null){
            Logger.error("user is null!");
            return;
        }

        //Set user to lowercase
        user=user.toLowerCase();

        if(!simpleSession.containsKey(user)){
            List<Session> list = new ArrayList<Session>();
            simpleSession.put(user, list);
            Logger.debug("Added user " + user + " to the session");
        }

        simpleSession.get(user).add(session);
    }

    /**
     * Removes the user session from the session map. If the user has no sessions then the user is
     * removed from the session map.
     * @param username
     * @param session
     */
    private void removeFromSession(String username, final Session session){

        if(username == null){
            Logger.error("name cannot be null ");
            return;
        }

        //Set user to lowercase
        final String user = username.toLowerCase();

        if(!simpleSession.containsKey(user)){
            Logger.error("name not in the session ");
            return;
        }
        simpleSession.get(user).remove(session);
        Logger.debug("Removed session from user " + user);

    }



    /**
     * Start the SFTP server
     * @throws IOException - If something went wrong
     */
    public void start() throws IOException {
        sshd.start();
    }

    /**
     * Stop the SFTP server
     * @param immediately - Stop immediately or after tasks in progress finish
     * @throws InterruptedException - If something went wrong
     */
    public void stop(boolean immediately) throws InterruptedException {
        sshd.stop(immediately);
    }

    /**
     * @return The port of the sftp server
     */
    public int getPort(){
        return sshd.getPort();
    }

    /**
     * @return True if the SFTP server is closed or closing or was never started in the first place
     */
    public boolean isShuttingDown(){
        return (sshd.isClosed() || sshd.isClosing());
    }

    /**
     * @return The default home directory of the SFTP server
     */
    public String getDefaultSFTPir(){
        return this.defaultSFTPDir;
    }


    /**
     * This class servers as the SFTP password authenticator. This class should compare
     * user names, passwords and roles from the database to the provided input
     */
    class SimplePasswordAuthenticator implements PasswordAuthenticator {

        @Override
        public boolean authenticate(String username, String password, ServerSession session) {

            if(username == null || password == null){
                Logger.error("name or password was null");
                return false;
            }


            boolean correctPassword = true;


            return correctPassword;
        }
    }


    /**
     * This class serves as the session listener for SFTP sessions
     */
    class SimpleSessionListener implements SessionListener {
        @Override
        public void sessionCreated(Session session) {}

        @Override
        public void sessionEvent(Session session, Event event) {
            if(event == Event.Authenticated){
                addToSession(session.getUsername(), session);
            }
        }

        @Override
        public void sessionClosed(Session session) {
            removeFromSession(session.getUsername(), session);
        }
    }

}
