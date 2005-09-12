/*
 * Created on Jul 14, 2004
 *
 * Copyright(c) Yale University, Jul 14, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portal.security.provider;

/**
 * Interface implemented by CAS security contexts.
 * @author andrew.petro@yale.edu
 */
public interface IYaleCasContext {
    /** Authentication type for Yale CAS authentication */
    public static final int YALE_CAS_AUTHTYPE = 0x1701;
    
    /**
     * Get a proxy ticket for a given target.
     * Implementations should return null if they are have no PGTIOU by which to obtain a proxy ticket.
     * Implementations should throw a CASProxyTicketAcquisitionException if an error occurs during an attempt
     * to obtain a PGT.  In particular, inability to contact the CAS server and expiration of the underlying PGT
     * should result in a CASProxyTicketAcquisitionException.
     * @param target - URL for which a proxy ticket is desired.
     * @return a valid proxy ticket for the target, or null.
     * @throws CASProxyTicketAcquisitionException - when unable to obtain Proxy Ticket.
     */
    public String getCasServiceToken(String target) throws CASProxyTicketAcquisitionException;
}


/* IYaleCasContext.java
 * 
 * Copyright (c) Jul 14, 2004 Yale University.  All rights reserved.
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