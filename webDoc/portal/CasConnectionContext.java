package org.jasig.portal.security.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.LocalConnectionContext;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Connection context that uses a security context implementing
 * IYaleSecurityContext to obtain a proxy tickets for use in descriptors
 * provided by this connection context.
 * 
 * @author unattributed
 * @author andrew.petro@yale.edu
 */
public class CasConnectionContext extends LocalConnectionContext {
    private static final Log log = LogFactory
            .getLog(CasConnectionContext.class);

    private ChannelStaticData staticData = null;

    private IPerson person = null;

    private IYaleCasContext yaleSecurityContext = null;

    public void init(ChannelStaticData sd) {
        this.staticData = sd;
        this.person = sd.getPerson();

        ISecurityContext ic = this.person.getSecurityContext();
        if (ic instanceof IYaleCasContext)
            this.yaleSecurityContext = (IYaleCasContext) ic;

        // loop through subcontexts to find implementations of
        // IYaleSecurityContext
        Enumeration en = ic.getSubContexts();
        while (en.hasMoreElements()) {
            ISecurityContext sctx = (ISecurityContext) en.nextElement();
            if (sctx instanceof YaleCasContext)
                this.yaleSecurityContext = (IYaleCasContext) sctx;
        }

        if (this.yaleSecurityContext == null)
            log.error("Unable to find CAS Security Context");
    }

    public String getDescriptor(String descriptor, ChannelRuntimeData rd) {
        if (log.isTraceEnabled()) {
            log.trace("getDescriptor(" + descriptor + ", " + rd + ")");
        }
        if (rd.getHttpRequestMethod().equals("GET")) {
            // get proxy service ticket for the service if needed
            String pst = null;
            if (this.yaleSecurityContext != null)
                try {
                    String xmlUri = rd.getParameter("cw_xml");
                    if (xmlUri == null)
                        xmlUri = this.staticData.getParameter("cw_xml");
                    pst = this.yaleSecurityContext.getCasServiceToken(xmlUri);
                } catch (CASProxyTicketAcquisitionException casex) {
                    log.error(
                            "getDescriptor() - Error retreiving proxy ticket.",
                            casex);
                }

            // append ticket parameter and value to query string
            if (descriptor.indexOf("?") != -1)
                return descriptor + "&ticket=" + pst;
            else
                return descriptor + "?ticket=" + pst;

        } else {
            return descriptor;
        }
    }

    /**
     * Returns url with proxy service ticket appended. Looks for static
     * parameter upc_cas_service_uri and uses that for service. If not
     * specified, uses the passed uri
     * 
     * @param xmlUri
     *            The original descriptor.
     * @return xmlUri with CAS proxy ticket parameter appended.
     */
    public String getDescriptor(String xmlUri) {
        // get proxy service ticket for the service
        String pst = null;
        if (this.yaleSecurityContext != null)
            try {
                // if no specified parameter for service, use target descriptor
                String casUri = this.staticData
                        .getParameter("upc_cas_service_uri");
                if (casUri != null)
                    pst = this.yaleSecurityContext.getCasServiceToken(casUri);
                else
                    pst = this.yaleSecurityContext.getCasServiceToken(xmlUri);
            } catch (CASProxyTicketAcquisitionException casex) {
                log.error("CasConnectionContext::getDescriptor() - Error retreiving proxy ticket.",
                                casex);
            }
        // append ticket parameter and value to query string
        if (xmlUri.indexOf("?") != -1)
            return xmlUri + "&ticket=" + pst;
        else
            return xmlUri + "?ticket=" + pst;
    }

    public void sendLocalData(Object conParam, ChannelRuntimeData rd) {
        if (conParam instanceof HttpURLConnection) {
            HttpURLConnection conn = (HttpURLConnection) conParam;

            if (rd.getHttpRequestMethod().equals("POST")) {
                // get proxy service ticket for the service if needed
                String pst = null;
                if (this.yaleSecurityContext != null)
                    try {
                        String xmlUri = rd.getParameter("cw_xml");
                        if (xmlUri == null)
                            xmlUri = this.staticData.getParameter("cw_xml");
                        pst = this.yaleSecurityContext
                                .getCasServiceToken(xmlUri);
                    } catch (CASProxyTicketAcquisitionException casex) {
                        log.error("sendLocalData() - Error retreiving proxy ticket.",
                                        casex);
                    }

                try {
                    // send ticket parameter and value via POST to backend app
                    conn.setDoOutput(true);
                    PrintWriter pw = new PrintWriter(conn.getOutputStream());
                    pw.print("ticket=" + pst);
                    pw.flush();
                    pw.close();
                } catch (IOException ioex) {
                    log.error("sendLocalData() - IO Error sending proxy ticket"
                            + ioex);
                }
            }
        }
    }
}