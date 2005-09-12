/*
 * Created on Aug 6, 2004
 *
 * Copyright(c) Yale University, Aug 6, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.proxy;

import java.io.IOException;
import java.util.Collections;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.util.SecureURL;

/**
 * A filter to echo proxy tickets to other instances of uPortal operating behind a load balancer.
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.2 $ $Date: 2004/08/06 18:48:55 $
 */
public class ProxyEchoFilter implements Filter {
    private static final Log log = LogFactory.getLog(ProxyEchoFilter.class);
    
    /**
     * The name of the filter initialization parameter the value of which must be a 
     * whitespace-delimited list of targets to which the PGTIOUs and PGTIDs should be echoed.
     */
    public static final String INIT_PARAM_ECHO_TARGETS = "edu.yale.its.tp.cas.proxy.echo.targets";
    
    /**
     * The set of PGTIOUs received so far.
     */
    private Set receivedPgtIous = Collections.synchronizedSet(new HashSet());
    
    /**
     * The set of URLs of ProxyTicketReceptor instances to which this filter should echo.
     */
    private Set echoTargets = new HashSet();
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        if (log.isTraceEnabled()){
            log.trace("initializing ProxyExchoFilter using config " + config);
        }
        String echoTargetsParam = config.getInitParameter(INIT_PARAM_ECHO_TARGETS);
        if (echoTargetsParam == null){
            throw new ServletException("The ProxyEchoFilter requires initialization parameter " + INIT_PARAM_ECHO_TARGETS + " to be a whitespace delimited list of echo targets.");
        }
        StringTokenizer st = new StringTokenizer(echoTargetsParam);
        while (st.hasMoreTokens()){
            String target = st.nextToken();
            this.echoTargets.add(target);
        }
        if (log.isTraceEnabled()){
            log.trace("returning from init() having initialized " + this);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain fc) throws IOException, ServletException {
        String pgtIou = request.getParameter(ProxyTicketReceptor.PGT_IOU_PARAM);
        if (this.receivedPgtIous.add(pgtIou)){
            int successes = echoRequest(pgtIou, request.getParameter(ProxyTicketReceptor.PGT_ID_PARAM));
            if (log.isDebugEnabled()){
                log.debug("Echoed the PGT request to " + successes + " of " + this.echoTargets.size() + " targets.");
            }
            // pass on the request so that this ProxyTicketReceptor will receive it.
            fc.doFilter(request, response);
         } else {
             if (log.isDebugEnabled()){
                 log.debug("Have already seen pgtIou=[" + pgtIou + "] and so am not echoing it.");   
             }
             // fail to pass on the request, since our ProxyTicketReceptor already received it
        }
    }

    /**
     * Echo the CAS server's "push" of the PGTIOU and PGTID.
     * @param pgtIou - the proxy granting ticket IOU
     * @param pgtID - the proxy granting ticket (id)
     * @return the number of echoes successfully executed.
     */
    private int echoRequest(String pgtIou, String pgtID){
        /*
         * I hate this URLs as concatenated strings.
         * This should be refactored to use a real URL class, preferably back
         * when the URLs were parsed in init().
         * However, this should work.  -awp9
         */
        int successes = 0;
        for (Iterator iter = this.echoTargets.iterator(); iter.hasNext();){
            StringBuffer target = new StringBuffer();
             target.append((String) iter.next());
            if (target.indexOf("?") == -1){
                target.append("?");
            } else {
                target.append("&");
            }
            target.append(ProxyTicketReceptor.PGT_IOU_PARAM).append("=").append(pgtIou);
            target.append("&").append(ProxyTicketReceptor.PGT_ID_PARAM).append("=").append(pgtID);
            try {
                SecureURL.retrieve(target.toString());
                successes++;
            } catch (Throwable t) {
                log.error("Failed to retrieve [" + target.toString() + "]", t);
            }
        }
        return successes;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
      // do nothing
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" echoTargets=");
        sb.append(this.echoTargets);
        sb.append(" receivedPgtIous=");
        sb.append(this.receivedPgtIous);
        return sb.toString();
    }

}


/* ProxyEchoFilter.java
 * 
 * Copyright (c) Aug 6, 2004 Yale University.  All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms,
 * with or without modification, are permitted, provided that the
 * following conditions are met.
 * 
 * 1. Any redistribution must include the above copyright notice and
 * disclaimer and this list of conditions in any related documentation
 * and, if feasible, in the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product
 * includes software developed by Yale University," in any related
 * documentation and, if feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse
 * or promote products derived from this software.
 */