/*
 *  Copyright (c) 2000-2004 Yale University. All rights reserved.
 *  See notice at end of file.
 */

package edu.yale.its.tp.cas.proxy;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Receives and keeps track fo PGTs and serial PGT identifiers (IOUs) sent by
 * CAS in response to a ServiceValidate request.
 * This version allows you to map multiple ProxyTicketReceptors, with different proxyUrls.
 * @author Shawn Bayern
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.7 $ $Date: 2004/08/09 00:57:18 $
 */
public class ProxyTicketReceptor extends HttpServlet {

    /**
     * The name of the servlet initialization parameter the value of which
     * should be the secure (https:) URL whereat CAS offers its proxy ticket
     * vending service. If this servlet parameter is not set, this servlet will
     * try the application context parameter of this same name.
     */
    public static final String CAS_PROXYURL_INIT_PARAM = "edu.yale.its.tp.cas.proxyUrl";

    /**
     * The name of the request parameter the value of which should be the Proxy
     * Granting Ticket IOU being sent by the CAS server.
     */
    static final String PGT_IOU_PARAM = "pgtIou";

    /**
     * The name of the request parameter the value of which should be the Proxy
     * Granting Ticket itself being sent by the CAS server.
     */
    static final String PGT_ID_PARAM = "pgtId";

    //*********************************************************************
    // Private state

    /**
     * A Map from proxy granting ticket IOUs to ProxyGrantingTicket s.
     */
    private static Map pgtMap = Collections.synchronizedMap(new HashMap());

    /**
     * The URL whereat CAS offers its proxy ticket vending service.
     */
    private String casProxyUrl;

    private static final Log log = LogFactory.getLog(ProxyTicketReceptor.class);

    //*********************************************************************
    // Initialization

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (log.isTraceEnabled()) {
            log.trace("entering init(" + config + ")");
        }

        // first try to get the proxy URL as a filter initialization parameter
        this.casProxyUrl = config.getInitParameter(CAS_PROXYURL_INIT_PARAM);

        // if it wasn't configured for this filter, maybe it is an application
        // context parameter
        if (this.casProxyUrl == null) {
            ServletContext app = config.getServletContext();
            this.casProxyUrl = app.getInitParameter(CAS_PROXYURL_INIT_PARAM);
            if (this.casProxyUrl == null)
                throw new ServletException(
                        "The servlet (or application context) initialization parameter "
                                + ProxyTicketReceptor.CAS_PROXYURL_INIT_PARAM
                                + " must be set.");
        }
        if (!this.casProxyUrl.toUpperCase().startsWith("HTTPS:")) {
            throw new ServletException(
                    "Initialization parameter "
                            + CAS_PROXYURL_INIT_PARAM
                            + " must specify an https: address; its current, unacceptable value is ["
                            + this.casProxyUrl + "]");
        }
        if (log.isTraceEnabled()){
            log.trace("returning from init() having configured a ProxyTicketReceptor as [" + this + "]");
        }
    }

    //*********************************************************************
    // Request handling

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pgtId = request.getParameter(PGT_ID_PARAM);
        String pgtIou = request.getParameter(PGT_IOU_PARAM);
        if (pgtId != null && pgtIou != null) {
            ProxyGrantingTicket pgt = new ProxyGrantingTicket(pgtId,
                    this.casProxyUrl);
            log.debug("adding pgtIou=[" + pgtIou + "], pgt=[" + pgt
                    + "] to the cache.");
            // put is synchronized because pgt is a synchronized Map.
            pgtMap.put(pgtIou, pgt);

            // inform CAS of success.
            PrintWriter out = response.getWriter();
            // TODO: almost certainly should have an <?xml version="1.0"?>
            // here... -awp9
            out.println("<casClient:proxySuccess "
                    + "xmlns:casClient=\"http://www.yale.edu/tp/casClient\"/>");
            out.flush();
        }
    }

    /**
     * Retrieves a proxy ticket using the PGT that corresponds to the given PGT
     * IOU.
     * 
     * @param pgtIou -
     *            the proxy granting ticket IOU, sent with the validation
     *            response.
     * @param target -
     *            the target service for which a proxy ticket is desired.
     * @return Proxy ticket for presentation to the given service, or null if
     *         unable to retrieve proxy ticket for given pgtIou.
     * @throws IOException -
     *             upon failure to contact CAS server.
     */
    public static String getProxyTicket(String pgtIou, String target)
            throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("entering getProxyTicket(pgtIou=[" + pgtIou
                    + "], target=[" + target + "]");
        }
        
        // get is synchronized because pgtMap is a synchronized map
        ProxyGrantingTicket pgt = (ProxyGrantingTicket) pgtMap.get(pgtIou);
        String proxyTicket = null;

        if (pgt == null) {
            log.error("No ProxyGrantingTicket found for pgtIou=[" + pgtIou + "]");
        } else {
            proxyTicket = pgt.getProxyTicket(target);
        }
        log.trace("returning from getProxyTicket() with proxy ticket ["
                + proxyTicket + "]");
        return proxyTicket;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" ");
        sb.append("casProxyUrl=[");
        sb.append(this.casProxyUrl);
        sb.append("]");
        sb.append(" static map from pgtIous to ProxyGrantingTickets: ");
        sb.append(ProxyTicketReceptor.pgtMap);
        return sb.toString();
    }
}

/*
 *  Copyright (c) 2000-2004 Yale University. All rights reserved.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 *  DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 *  DAMAGE.
 *
 *  Redistribution and use of this software in source or binary forms,
 *  with or without modification, are permitted, provided that the
 *  following conditions are met:
 *
 *  1. Any redistribution must include the above copyright notice and
 *  disclaimer and this list of conditions in any related documentation
 *  and, if feasible, in the redistributed software.
 *
 *  2. Any redistribution must include the acknowledgment, "This product
 *  includes software developed by Yale University," in any related
 *  documentation and, if feasible, in the redistributed software.
 *
 *  3. The names "Yale" and "Yale University" must not be used to endorse
 *  or promote products derived from this software.
 */