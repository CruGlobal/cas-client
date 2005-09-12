/*
 * Created on Aug 17, 2004
 *
 * Copyright(c) Yale University, Aug 17, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.client.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * This Filter scrutinizes the proxy chain described in the CASReceipt
 * session attribute exposed by the CASFilter.
 * It takes a filter initialization parameter named 
 * edu.yale.its.tp.cas.client.filter.authorizedProxyChains
 * which is a whitespace-delimited list of tokens. Tokens are either entries in the proxy chain (in the same order
 * as they are presented in the proxy ticket validation response from the CAS server) or semicolon characters, which 
 * delineate the beginning of a new authorized proxy chain.  See the accompanying JUnit testcase, the source code, and documentation
 * for additional information.
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.1 $ $Date: 2004/08/18 03:58:12 $
 */
public class ProxyChainScrutinizerFilter implements Filter {

    private static final Log log = LogFactory.getLog(ProxyChainScrutinizerFilter.class);
    
    /**
     * Set of Lists.  Each List represents one authorized proxy chain.
     */
    private Set authorizedProxyChains = new HashSet();
    
    /**
     * The name of the filter initialization parameter the value of which
     * is a semicolon-delimited list of whitespace-delimited lists of authorized proxy chains.
     * Note that proxy chains are written back from the filtered service to the CAS server -- that is,
     * the most previous service in the chain appears first in the list, followed by the service which proxied 
     * authentication to that most previous service, and so forth.  See accompanying documentation.
     */
    public static final String AUTHORIZED_PROXIES_INITPARAM = "edu.yale.its.tp.cas.client.filter.authorizedProxyChains";
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        String authorizedProxiesString = config.getInitParameter(AUTHORIZED_PROXIES_INITPARAM);
        if (authorizedProxiesString == null) {
            throw new ServletException("The filter initialization parameter " + AUTHORIZED_PROXIES_INITPARAM + " must be a semicolon delimited list of authorized filter chains.");
        }
        List currentAuthorizedChain = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(authorizedProxiesString);
        if (! (tokenizer.hasMoreTokens())){
            throw new ServletException("The filter initialization paramter " + AUTHORIZED_PROXIES_INITPARAM + " must contain at least one token.");
        }
        while (tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if (token.equals(";")){
                // terminate the current authorized chain and start a new one
                this.authorizedProxyChains.add(currentAuthorizedChain);
                currentAuthorizedChain = new LinkedList();
            } else {
                // add the authorized chain element if it is valid
                if (! token.toUpperCase().startsWith("HTTPS://")){
                    throw new ServletException("Illegal authorized proxy chain element [" + token + "] in value of filter initialization parameter " + AUTHORIZED_PROXIES_INITPARAM);                
                }
                currentAuthorizedChain.add(token);
            }
        }
        // check if there's an open authorizedProxyChain that wasn't terminated with a semicolon
        if (! currentAuthorizedChain.isEmpty()){
            this.authorizedProxyChains.add(currentAuthorizedChain);
        }
        if (log.isTraceEnabled()){
            log.trace("Configured filter named [" + config.getFilterName() + "] as " + toString()); 
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain fc) throws IOException, ServletException {
        
        if (isRequestAuthorized(request)){
            if (log.isDebugEnabled()){
                log.debug("Filter " + this + " is passing through request " + request);
            }
            fc.doFilter(request, response);
            return;
        }
            if (response instanceof HttpServletResponse){
                log.info("Sending FORBIDDEN.");
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                throw new ServletException("Request was unauthorized (probably not an HttpServletRequest at all) and response was not an HttpServletResponse so couldn't send 403/Forbidden.");
            }
        
    }

    /**
     * Is this request authorized, considering the CASReceipt in
     * light of the authorized proxy chains defined at filter initialization.
     * @param request - the request to be scrutinized.
     * @return true if the request is authorized, false otherwise.
     */
    private boolean isRequestAuthorized(ServletRequest request) {
        if (log.isTraceEnabled()){
            log.trace("entering isRequestAuthorized(" + request + ")");
        }
        if (! (request instanceof HttpServletRequest)) {
            log.warn("request was not of expected type HttpServletRequest - considering request unauthorized.");
            return false;
        }
        HttpSession session = ((HttpServletRequest) request).getSession(false);
        if (session == null) {
            log.info("No HttpSession was established into which a CASReceipt might have been stored - considering request unauthorized.");
            return false;
        }
        Object potentialReceipt = session.getAttribute(CASFilter.CAS_FILTER_RECEIPT);
        if (potentialReceipt == null) {
            log.info("CASReceipt was not present in HttpSession - considered request unauthorized.");
            return false;
        }
        if (! (potentialReceipt instanceof CASReceipt)) {
            log.warn("An object was present in the session attribute " + CASFilter.CAS_FILTER_RECEIPT + " but it wasn't of type " + CASReceipt.class.getName());
            return false;
        }
        CASReceipt receipt = (CASReceipt) potentialReceipt;
        
        if (! this.authorizedProxyChains.contains(receipt.getProxyList())){
            log.info("CAS receipt: " + receipt + " did not present a proxy chain among those authorized: " + this.authorizedProxyChains + " - considering request unauthorized.");
            return false;
        }
        log.trace("returning from isRequestAuthorized() with true");
        return true;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub

    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" authorizedProxyChains:").append(this.authorizedProxyChains);
        return sb.toString();
    }
    
}


/* ProxyChainScrutinizerFilter.java
 * 
 * Copyright (c) Aug 17, 2004 Yale University.  All rights reserved.
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