package com.eduonix;

import com.eduonix.ssl.SSLManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Entry Point for application code
 */
public class EntryPoint {



    public static void main(String[] args) {


        SSLManager sslConnection = new SSLManager();

        try {
            sslConnection.doGet( );
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
