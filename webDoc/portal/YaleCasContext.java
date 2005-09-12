package org.jasig.portal.security.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.*;

import edu.yale.its.tp.cas.client.CASAuthenticationException;
import edu.yale.its.tp.cas.client.ServiceTicketValidator;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;
import java.io.IOException;
import java.util.Properties;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.PortalException;

/**
 * <p>A SecurityContext using Yale's Central Authentication Server.</p>
 *
 * @author Susan Bramhall
 * $Revision: 1.1 $ $Date: 2004/07/14 23:43:47 $
 */
public class YaleCasContext extends ChainingSecurityContext implements IYaleCasContext {

		private static final Log log = LogFactory.getLog(YaleCasContext.class);

    //*********************************************************************
    // Constants
    
    private static String CasProxyCallbackUrl = null;
    private static String PortalServiceUrl = null;
    private static String CasValidateUrl = null;

    private String pgtIou = null;

    YaleCasContext () {
      super();
      log.trace("entering YaleCasContext()");
	  try {
		 String key;
		 // We retrieve the tokens representing the credential and principal
		 // parameters from the security properties file.
		 Properties props = 
			ResourceLoader.getResourceAsProperties(YaleCasContext.class, "/properties/security.properties");

		CasProxyCallbackUrl= props.getProperty("org.jasig.portal.security.provider.YaleCasContext.CasProxyCallbackUrl");
		log.debug("CasProxyCallbackUrl is [" + CasProxyCallbackUrl + "]");
		PortalServiceUrl = props.getProperty("org.jasig.portal.security.provider.YaleCasContext.PortalServiceUrl");
		log.debug("PortalServiceUrl is [" + PortalServiceUrl + "]");
		CasValidateUrl = props.getProperty("org.jasig.portal.security.provider.YaleCasContext.CasValidateUrl");
		log.debug("CasValidateUrl is [" + CasValidateUrl + "]");

	  } catch(Exception e) {
		  log.error("Error instantiating YaleCasContext: "+e, e);
	  }

    }

    public int getAuthType() {
        return IYaleCasContext.YALE_CAS_AUTHTYPE;
    }

    /*
    * Authentication entry-point
    *    Opaque credentials are set to value of ticket supplied by CAS
    *    This method calls the CAS ServiceTicketValidator supplying
    *      CasValidateUrl - Url ServiceTicketValidator should use to contact CAS.
    *           This should point to the
    *      CasProxyCallbackUrl - Url of the ProxyTicketReceptor servlet running
    *           in our context which will receive the proxy granting ticket
    *      PortalServiceUrl - Portal service url - where portal service ticket
    *           was received
    */
    public synchronized void authenticate() throws PortalSecurityException {
    	if (log.isTraceEnabled()){
    		log.trace("entering authenticate()");
    	}
    String m_service_ticket = new String(this.myOpaqueCredentials.credentialstring);

    this.isauth = false;
    try {
      ServiceTicketValidator sv = new ServiceTicketValidator();

    sv.setCasValidateUrl(CasValidateUrl);
    if (CasProxyCallbackUrl != null){
			sv.setProxyCallbackUrl(CasProxyCallbackUrl);
    }
    sv.setService(PortalServiceUrl);
    sv.setServiceTicket(m_service_ticket);
    log.debug("authenticate(): Validating ServiceTicket: ["+ m_service_ticket + "]");
    sv.validate();
    log.debug("authenticate(): got response:[" + sv.getResponse() + "]");

    if (sv.isAuthenticationSuccesful()) {
      this.myPrincipal.setUID(sv.getUser());

      // We keep the pgtIOU around as the key to retrieve the Proxy granting ticket
      // to exchange for a proxy service ticket in the future.  A channel has
      // access to this securityContext and can request a Service ticket. via
      // the  public getCasServiceToken method
      this.pgtIou = sv.getPgtIou();
      this.isauth = true;
      log.debug("CASContext authenticated ["+ this.myPrincipal.getUID() + "]");
    }
    //else {
    //  throw new PortalSecurityException("error code: " + sv.getErrorCode()+"\n error message: "+sv.getErrorMessage());
    // }
    } catch (Exception ex){
    	log.error(ex);
      throw new PortalSecurityException("Error in CAS Authentication", ex);}
    this.myAdditionalDescriptor=null; //no additional descriptor from CAS
    super.authenticate();
    if (log.isTraceEnabled()){
    	log.trace("returning from authenticate()");
    }
    return;
    }

    
    public String getCasServiceToken (String target) throws CASProxyTicketAcquisitionException {
    	if (log.isTraceEnabled()){
				log.trace("entering getCasServiceToken(" + target + "), previously cached pgtIou=[" + this.pgtIou + "]");
    	}
    	String proxyTicket;
        try {
            proxyTicket = ProxyTicketReceptor.getProxyTicket(this.pgtIou, target);
        } catch (IOException e) {
            throw new CASProxyTicketAcquisitionException(target, this.pgtIou, e);
        }
        if (log.isTraceEnabled()){
    		log.trace("returning from getCasServiceToken() with return value [" + proxyTicket + "]");
    	}
    	return proxyTicket;
    }
}
