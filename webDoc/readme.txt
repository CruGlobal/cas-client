README file for 
CAS Client 2.1.0
$Revision: 1.6 $ $Date: 2004/09/10 20:57:41 $

Contact: Drew Mazurek (drew dot mazurek at yale dot edu), CAS project maintainer

This release contains evolutionary improvement over CAS Java Client 2.0.11 and previously existing CAS Java Client betas.

Contributions of this release:

0) Fix of outstanding thread safety bug in CAS Client beta: 
CASFilter no longer has instance-scope pgtIou String which is set in the
getAuthenticatedUser() method for each request.  
Instead, getAuthenticatedUser() returns a CASReceipt, which bundles both the username and the pgtIou.

1) Commons logging:  While not comprehensive, Commons logging statements have been added to much of the client code.

2) JUnit testcases: While not comprehensive, JUnit tests exercise much of the CAS client code.

3) CASFilter improvements:

3.1) Fails faster: Some common fatal misconfigurations result in ServletException at filter initialization, 
in a way that we hope will make the filter easier to use for beginners.

3.2) CASReceipt: The CASFilter exposes a CASReceipt object, encapsulating the information about the successful validation, as a session attribute.
NOTE: this is a different approach than was taken in previous CAS Java Client beta releases.  In particular, this CASFilter
does not expose the PGTIOU as its own session attribute.  Instead, client applications must read the pgtIou from the CASReceipt that is put into the session.

The CASReceipt can also be used outside the context of the CASFilter.
When using the ProxyTicketValidator / ServiceTicketValidator CAS Java client API, you can choose to
validate your own Validator and then feed this to a CASReceipt, you can choose to let CASReceipt
validate your ProxyTicketValidator for you, or you can build your own CASReceipt if you want.

3.3) Enhanced strictness -- more "filter-like": 
The CASFilter in this release uses the CASReceipt to check requests subsequent to that in which initial ticket validation occurs to
determine whether the validated ticket would have been sufficient to pass through the current filter.

This enables mapping multiple instances of the CASFilter within a single application, 
with all behaving as expected.  
A CASFilter with the renew initialization parameter set, for instance, will not
pass through requests from sessions that were authenticated with tickets that were not validated with renew=true.

4) ProxyTicketReceptor improvements:

4.1) Allows multiple instances of ProxyTicketReceptor to be mapped, configured to use different CAS servers if desired.
4.2) Simplifies ProxyTicketReceptor by moving code to callback to CAS into a ProxyGrantingTicket helper class.
4.3) ProxyEchoFilter to echo received Proxy Granting Ticket IOUs and IDs to other ProxyTicketReceptors, perhaps for use in the case where
an application is load balanced.

5) ProxyChainScrutizerFilter which makes more convenient consideration of more than just the most previous proxy in the proxy chain.

6) CASValidateFilter which performs only the ticket validation portion of the full CASFilter functionality.  CASValidateFilter has no authorization role: it does not
scrutinize proxy chains or a CASReceipt previously set into the session.

Known issues:

StaticCasReceiptCacherFilter should expire its cache, but does not yet do so.
ProxyTicketReceptor should expire its cache, but does not yet do so.

Test coverage is far from complete (but getting better...).
Logging coverage is far from complete (but getting better...).

This file last modified by $Author: awp9 $