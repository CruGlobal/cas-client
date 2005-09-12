package org.jasig.portal.security.provider;

import org.jasig.portal.security.*;

/**
 * <p>The factory class for the Yale security context.</p>
 *
 * @author Shawn Bayern
 * $Revision: 1.1 $ $Date: 2004/07/14 23:43:47 $
 */

public class YaleCasContextFactory implements ISecurityContextFactory {
  public ISecurityContext getSecurityContext() {
    return new YaleCasContext();
  }
}
