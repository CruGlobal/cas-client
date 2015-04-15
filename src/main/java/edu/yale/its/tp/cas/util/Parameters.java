package edu.yale.its.tp.cas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.FilterConfig;

/**
 * @author Matt Drees
 */
public class Parameters
{
    private static Log log = LogFactory.getLog(Parameters.class);

    public static String getParameter(FilterConfig config, String parameterName) {
        String jndiValue = jndiLookup(parameterName);
        if (jndiValue != null)
        {
            return jndiValue;
        }
        else
        {
            return config.getInitParameter(parameterName);
        }
    }

    private static String jndiLookup(String parameterName) {
        String location = "java:comp/env/cas/" + parameterName;
        try {
            return (String) new InitialContext().lookup(location);
        } catch (NameNotFoundException e) {
            return null;
        } catch (NamingException e) {
            log.warn("unable to look up in jndi: " + location, e);
            return null;
        }
    }

}
