package org.jasig.portal.security.provider;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;



/**
 * <p>The factory class for the Yale security context using the CASFilter approach.</p>
 *
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.1 $ $Date: 2004/07/14 23:43:47 $
 */

public class YaleCasFilteredContextFactory implements ISecurityContextFactory {
  public ISecurityContext getSecurityContext() {
    return new YaleCasFilteredContext();
  }
}
