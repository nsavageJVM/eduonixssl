package com.eduonix.ssl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * this class will demonstrate the ssl handshake with the server
 *
 * Certificate format X.509
 *
 */

@Slf4j
public class SSLManager {

    private CloseableHttpClient client;
    protected static final String HOST_NAME = "host_name_or_ip";
    protected static final int HOST_PORT = 9443;
    public static final String BASE_URL = "https://" + HOST_NAME + ":" + HOST_PORT;
    protected static final String SFTP_HOST = HOST_NAME;
    protected static final int SFTP_PORT = 2554;



    public SSLManager( ) {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //ClassLoader classLoader =
            //File file = new File(classLoader.getResource("mycert.cer").getFile());
            //InputStream caInput = new BufferedInputStream(new FileInputStream("C:\\Users\\User\\Documents\\KlearKapture\\KlearServerClient\\mycert.cer"));
            InputStream caInput = SSLManager.class.getClassLoader().getResourceAsStream("mycert.cer");//new BufferedInputStream(new FileInputStream(file));

            Certificate ca = cf.generateCertificate(caInput);
            log.debug("ca=" + ((X509Certificate) ca).getSubjectDN());

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1"},
                    null,
                    new HostnameVerifier() {
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals(HOST_NAME);
                        }
                    });


            /**
             * Allow cookies from server by setting cookie policy to easy
             * Taken from http://hc.apache.org/httpcomponents-client-ga/tutorial/html/statemgmt.html#d5e489
             */
            CookieSpecProvider easySpecProvider = new CookieSpecProvider() {
                public CookieSpec create(HttpContext context) {
                    return new BrowserCompatSpec() {
                        @Override
                        public void validate(Cookie cookie, CookieOrigin origin)
                                throws MalformedCookieException {
                            // Oh, I am easy
                        }
                    };
                }
            };
            Registry<CookieSpecProvider> r = RegistryBuilder.<CookieSpecProvider>create()
                    .register(CookieSpecs.BEST_MATCH,
                            new BestMatchSpecFactory())
                    .register(CookieSpecs.BROWSER_COMPATIBILITY,
                            new BrowserCompatSpecFactory())
                    .register("easy", easySpecProvider)
                    .build();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setCookieSpec("easy")
                    .build();


            client = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCookieSpecRegistry(r)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String doGet(String url) throws  Exception {

        HttpGet get = new HttpGet(url);


        CloseableHttpResponse closeableHttpResponse = null;
        String strResponse = null;
        try {
            closeableHttpResponse = client.execute(get);
            strResponse = EntityUtils.toString(closeableHttpResponse.getEntity());


            log.trace("entity= {}", closeableHttpResponse.getEntity());
            log.trace("entity-body= {}", strResponse);
        } catch (IOException e) {
            e.printStackTrace();
            throw new  Exception("Can't connect to the server");
        }


        StatusLine sl = closeableHttpResponse.getStatusLine();

        switch (sl.getStatusCode()) {
            case 200: //HTTP OK
                return strResponse;
            case 400: //HTTP BAD REQUEST
                throw new Exception(strResponse);
            case 401: //HTTP UNAUTHORIZED
                throw new Exception(strResponse);
            case 500: //Internal Server Error (not good)
                throw new Exception("Internal server error. Please contact administrator");
            default:
                throw new  Exception("Unhandled Klear Kapture Exception");
        }
    }



}
