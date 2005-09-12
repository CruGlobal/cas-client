package org.jasig.portal.security.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.*;

import edu.yale.its.tp.cas.client.CASAuthenticationException;
import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.filter.StaticCasReceiptCacherFilter;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;
import java.io.IOException;

/**
 * <p>
 * A SecurityContext using Yale's Central Authentication Server.
 * Relies on the CASValidationFilter already having done any necessary ticket validation,
 * and the StaticCasReceiptCacherFilter having cached the receipt, keyed by the ticket.
 * Based on Susan Bramhall's YaleSecurityContext.
 * </p>
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.1 $ $Date: 2004/07/14 23:43:47 $
 */
public class YaleCasFilteredContext extends ChainingSecurityContext implements IYaleCasContext {

    private static final Log log = LogFactory.getLog(YaleCasFilteredContext.class);

    /**
     * Receipt stored here upon authenticate() invocation.
     */
    private CASReceipt receipt;

    YaleCasFilteredContext() {
        super();
    }

    public int getAuthType() {
        return IYaleCasContext.YALE_CAS_AUTHTYPE;
    }

    /*
     * Authentication entry-point Opaque credentials are set to value of ticket
     * supplied by CAS.  This method checks to see if the StaticCasReceiptCacherFilter
     * has previously cached a receipt representing the prior validation of this ticket.
     */
    public synchronized void authenticate() throws PortalSecurityException {
        if (log.isTraceEnabled()) {
            log.trace("entering authenticate()");
        }
        String serviceTicket = new String(
                this.myOpaqueCredentials.credentialstring);

        this.isauth = false;
        this.receipt = StaticCasReceiptCacherFilter
                .receiptForTicket(serviceTicket);

        if (this.receipt != null) {
            this.myPrincipal.setUID(this.receipt.getUserName());
            this.isauth = true;
            log.debug("CASContext authenticated [" + this.myPrincipal.getUID()
                    + "] using receipt [" + this.receipt + "]");
        }

        this.myAdditionalDescriptor = null; //no additional descriptor from CAS
        super.authenticate();
        if (log.isTraceEnabled()) {
            log.trace("returning from authenticate()");
        }
        return;
    }

 
    public String getCasServiceToken(String target) throws CASProxyTicketAcquisitionException {
        if (log.isTraceEnabled()) {
            log.trace("entering getCasServiceToken(" + target
                    + "), previously cached receipt=["
                    + this.receipt + "]");
        }
        if (this.receipt == null){
            return null;
        }
        if (this.receipt.getPgtIou() == null){
            return null;
        }
        String proxyTicket;
        try {
            proxyTicket = ProxyTicketReceptor.getProxyTicket(this.receipt
                    .getPgtIou(), target);
        } catch (IOException e) {
            log.error("Error contacting CAS server for proxy ticket", e);
            throw new CASProxyTicketAcquisitionException(target,this.receipt, e);
        }
        if (proxyTicket == null){
            log.error("Failed to obtain proxy ticket using receipt [" + this.receipt + "], has the Proxy Granting Ticket referenced by the pgtIou expired?");
            throw new CASProxyTicketAcquisitionException(target, this.receipt);
        }
        if (log.isTraceEnabled()) {
            log.trace("returning from getCasServiceToken(), returning proxy ticket ["
                    + proxyTicket + "]");
        }
        return proxyTicket;
    }
}