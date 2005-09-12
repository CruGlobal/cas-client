/*
 * Created on Aug 8, 2004
 *
 * Copyright(c) Yale University, Aug 8, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.proxy;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.util.SecureURL;

/**
 * Represents a CAS ProxyGrantingTicket.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.1 $ $Date: 2004/08/09 00:55:47 $
 */
class ProxyGrantingTicket {

    private static Log log = LogFactory.getLog(ProxyGrantingTicket.class);

    /**
     * The value of the request parameter of the same name sent by CAS.
     */
    private String pgtId;

    /**
     * The CAS proxy URL whereat the pgtId can be validated to obtain a CAS
     * Proxy Ticket.
     */
    private String casProxyUrl;

    /**
     * Instantiate a new ProxyGrantingTicket with the given pgtId and
     * casProxyUrl.
     * 
     * @param pgtId -
     *            the proxy granting ticket identifier
     * @param casProxyUrl -
     *            the URL whereat the pgtId can be used to obtain a proxy
     *            ticket.
     * @throws IllegalArgumentException -
     *             if either parameter is null
     */
    ProxyGrantingTicket(String pgtId, String casProxyUrl) {
        if (pgtId == null || casProxyUrl == null)
            throw new IllegalArgumentException(
                    "Cannot instantiate ProxyGrantingTicket(" + pgtId + ","
                            + casProxyUrl + ")");
        this.pgtId = pgtId;
        this.casProxyUrl = casProxyUrl;
    }

    /**
     * Retrieves a proxy ticket for the given target using this PGT.
     * 
     * @param target -
     *            the target service for which a proxy ticket is desired.
     * @return Proxy ticket for presentation to the given service, or null if
     *         unable to retrieve proxy ticket for given pgtIou.
     * @throws IOException -
     *             upon failure to contact CAS server.
     */
    public String getProxyTicket(String target) throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("entering getProxyTicket(target=[" + target
                    + "]) of PGT " + this);
        }

        String proxyTicket = null;

        // retrieve an XML response from CAS's "Proxy" actuator
        String url = this.casProxyUrl + "?pgt=" + this.pgtId
                + "&targetService=" + target;
        String response = SecureURL.retrieve(url);

        // parse this response (use a lightweight approach for now)
        if (response.indexOf("<cas:proxySuccess>") != -1
                && response.indexOf("<cas:proxyTicket>") != -1) {
            int startIndex = response.indexOf("<cas:proxyTicket>")
                    + "<cas:proxyTicket>".length();
            int endIndex = response.indexOf("</cas:proxyTicket>");
            proxyTicket = response.substring(startIndex, endIndex);
        } else {
            log.error("CAS server responded with error for request [" + url
                    + "].  Full response was [" + response + "]");
        }

        log.trace("returning from getProxyTicket() with proxy ticket ["
                + proxyTicket + "]");
        return proxyTicket;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" pgtId=[").append(this.pgtId).append("] ");
        sb.append(" casProxyUrl=[").append(this.casProxyUrl).append("]");
        return sb.toString();
    }
}

/*
 * ProxyGrantingTicket.java
 * 
 * Copyright (c) Aug 8, 2004 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */