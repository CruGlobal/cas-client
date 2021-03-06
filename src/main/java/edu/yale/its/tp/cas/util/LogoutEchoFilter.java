/*
 * Created on Aug 6, 2004
 *
 * Copyright(c) Yale University, Aug 6, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import edu.yale.its.tp.cas.client.filter.InfinispanLogoutStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CCCI<br/>
 * <br/>
 * A filter to echo logout requests to other instances operating behind a load
 * balancer. This is copied from ProxyEchoFilter.
 *
 * This is useful if the host names of the other instances can be determined up-front.
 * It is not needed when using {@link InfinispanLogoutStorage}.
 *
 * @author andrew.petro@yale.edu
 * @author Nathan.Kopp@ccci.org
 * @author Matt Drees
 */
public class LogoutEchoFilter implements Filter
{
    private static final Log log = LogFactory.getLog(LogoutEchoFilter.class);

    /**
     * The name of the filter initialization parameter the value of which must
     * be a whitespace-delimited list of targets to which the logout requests
     * should be echoed.
     * 
     * Uses the same settings as the proxy echo filter
     */
    public static final String INIT_PARAM_ECHO_TARGETS = "edu.yale.its.tp.cas.logout.echo.targets";
    
    public static final String INIT_PARAM_CONTINUE_CHAIN = "edu.yale.its.tp.cas.logout.echo.continueChain";

    /**
     * The set of URLs of ProxyTicketReceptor instances to which this filter
     * should echo.
     */
    private Set echoTargets = new HashSet();

    private boolean continueChain = true;
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        if (log.isTraceEnabled())
        {
            log.trace("initializing ProxyExchoFilter using config " + config);
        }
        String echoTargetsParam = Configuration.getParameter(config, INIT_PARAM_ECHO_TARGETS);
        if (echoTargetsParam == null) { throw new ServletException(
            "The ProxyEchoFilter requires initialization parameter " + INIT_PARAM_ECHO_TARGETS
                    + " to be a whitespace delimited list of echo targets."); }
        StringTokenizer st = new StringTokenizer(echoTargetsParam);
        while (st.hasMoreTokens())
        {
            String target = st.nextToken();
            this.echoTargets.add(target);
        }
        String continueChainParam = Configuration.getParameter(config, INIT_PARAM_CONTINUE_CHAIN);
        if (continueChainParam != null)
        {
            this.continueChain = Boolean.parseBoolean(continueChainParam);
        }
        
        if (log.isTraceEnabled())
        {
            log.trace("returning from init() having initialized " + this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException,
            ServletException
    {

        // test to see if this should be echoed
        if (request.getParameter("ticket") != null && request.getParameter("ticket").startsWith("-"))
        {
            // int successes = echoRequest(pgtIou,
            // request.getParameter(ProxyTicketReceptor.PGT_ID_PARAM));
            int successes = echoRequest(request);

            log.debug("Echoed the logout request to " + successes + " of " + this.echoTargets.size() + " targets.");

            if (continueChain)
            {
                // pass on the request so that this ProxyTicketReceptor will receive
                // it.
                fc.doFilter(request, response);
            }
            else
            {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_OK);
                httpResponse.setContentType("text/plain");
                httpResponse.getWriter().write("logout request received");
            }
        }
    }

    /**
     * Echo something out to the others in the cluster.
     * 
     * @return the number of echoes successfully executed.
     * @throws MalformedURLException
     */
    private int echoRequest(ServletRequest request) throws MalformedURLException
    {
        /*
         * I hate this URLs as concatenated strings. This should be refactored
         * to use a real URL class, preferably back when the URLs were parsed in
         * init(). However, this should work. -awp9
         */
        int successes = 0;
        for (Iterator iter = this.echoTargets.iterator(); iter.hasNext();)
        {
            StringBuffer target = new StringBuffer((String) iter.next());

            Enumeration enumeration = request.getParameterNames();
            boolean first = true;
            for (; enumeration.hasMoreElements();)
            {
                String name = (String) enumeration.nextElement();
                if (first && target.indexOf("?") == -1)
                    target.append("?");
                else
                    target.append("&");
                target.append(name);
                target.append("=");
                target.append(request.getParameter(name));
                first = false;
            }
            try
            {
                SecureURL.retrieve(target.toString());
                successes++;
            }
            catch (Throwable t)
            {
                log.error("Failed to retrieve [" + target.toString() + "]", t);
            }
        }
        return successes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        // do nothing
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" echoTargets=");
        sb.append(this.echoTargets);
        return sb.toString();
    }

}
