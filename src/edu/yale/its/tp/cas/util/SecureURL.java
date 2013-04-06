package edu.yale.its.tp.cas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * <p>
 * A class housing some utility functions exposing secure URL validation and
 * content retrieval. The rules are intended to be about as restrictive as a
 * common browser with respect to server-certificate validation.
 * </p>
 * 
 * NOTE: Depends on JSSE or JDK 1.4!
 */
public class SecureURL
{

    /**
     * For testing only...
     */
    public static void main(String args[]) throws IOException
    {
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

        System.out.println(SecureURL.retrieve(args[0]));
    }

    /**
     * Functions the same as retrieve(String url) but adds timeout
     * functionality.
     * 
     * @throws Exception
     */
    public static String retrieve(String url, int timeout) throws Exception
    {
        // Use a funky anonymous inner class to put a timeout on the
        // "retrieve" method. This seems to be necessary because
        // the RLConnection.openConnection() method (called by retrieve)
        // doesn't seem to pay attention to the timeout that we set
        // in the initialization
        TimeoutThread timeoutRetrieve = new TimeoutThread()
        {
            public Object run(Object[] params) throws Exception
            {
                return retrieve((String) params[0]);
            }
        };

        return (String) timeoutRetrieve.startWithTimeout(new Object[] { url }, timeout);
    }

    /**
     * Retrieve the contents from the given URL as a String, assuming the URL's
     * server matches what we expect it to match.
     * 
     * @throws Exception
     */
    public static String retrieve(String url) throws IOException
    {
        BufferedReader r = null;
        try
        {
            URL u = new URL(url);
            // NK-CCCI - disable HTTPS check
            // if (!u.getProtocol().equals("https"))
            // throw new
            // IOException("only 'https' URLs are valid for this method");
            URLConnection uc = u.openConnection();
            uc.setRequestProperty("Connection", "close");
            r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line;
            StringBuffer buf = new StringBuffer();
            while ((line = r.readLine()) != null)
                buf.append(line + "\n");
            return buf.toString();
        }
        finally
        {
            try
            {
                if (r != null) r.close();
            }
            catch (IOException ex)
            {
                // ignore
            }
        }
    }

    public static class Response
    {
        public String content;
        public Map headers;
    }

    /**
     * CCCI
     * 
     * Retrieve a response, containing both the HTTP content and the HTTP
     * headers.
     */
    public static Response retrieveResponse(String url) throws IOException
    {
        BufferedReader r = null;
        try
        {
            URL u = new URL(url);
            // CCCI
            // if (!u.getProtocol().equals("https")){
            // // IOException may not be the best exception we could throw here
            // // since the problem is with the URL argument we were passed, not
            // // IO. -awp9
            // log.error("retrieve(" + url +
            // ") on an illegal URL since protocol was not https.");
            // throw new
            // IOException("only 'https' URLs are valid for this method");
            // }

            URLConnection uc = u.openConnection();
            uc.setRequestProperty("Connection", "close");
            r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line;
            StringBuffer buf = new StringBuffer();
            while ((line = r.readLine()) != null)
                buf.append(line + "\n");

            Response res = new Response();
            res.content = buf.toString();
            res.headers = uc.getHeaderFields();
            return res;

        }
        finally
        {
            try
            {
                if (r != null) r.close();
            }
            catch (IOException ex)
            {
                // ignore
            }
        }
    }

}
