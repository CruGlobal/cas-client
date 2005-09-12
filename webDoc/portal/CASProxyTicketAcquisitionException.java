/*
 * Created on Jul 14, 2004
 *
 * Copyright(c) Yale University, Jul 14, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portal.security.provider;

import org.jasig.portal.PortalException;
import org.jasig.portal.exceptions.ErrorCategory;
import org.jasig.portal.exceptions.ErrorID;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * Thrown when unbable to obtain CAS proxy ticket.
 * @author andrew.petro@yale.edu
 */
public class CASProxyTicketAcquisitionException extends PortalException {

    /**
     * Was unable to obtain a proxy ticket.
     */
    public static final ErrorID couldNotObtainProxyTicket = new ErrorID(ErrorCategory.SECURITY, "couldNotObtainProxyTicket", "Could not obtain proxy ticket for service [{0}] using credentials [{1}]");
 
    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given receipt.
     * @param service - service for which a PT was requested
     * @param receipt - receipt the pgtIou of which was being used to obtain the PT.
     */
    public CASProxyTicketAcquisitionException(String service, CASReceipt receipt){
        super(couldNotObtainProxyTicket);
        
        String[] parameters = new String[2];
        parameters[0] = service;
        parameters[1] = receipt.toString();
        this.setParameters(parameters);
    }
    
    /**
     * Exception thrown when cannot obtain proxy ticket for a given service using the given receipt.
     * @param service - service for which a PT was requested
     * @param receipt - receipt the pgtIou of which was being used to obtain the PT.
     * @param cause - underlying throwable causing the error condition
     */
    public CASProxyTicketAcquisitionException(String service, CASReceipt receipt, Throwable cause){
        super(couldNotObtainProxyTicket, cause);
        
        String[] parameters = new String[2];
        parameters[0] = service;
        parameters[1] = receipt.toString();
        this.setParameters(parameters);
    }
    
    /** 
     * Exception thrown when cannot obtain proxy ticket for a given service using the given pgtIou.
     * @param service - service for which a PT was requested.
     * @param pgtIou - the pgtIou for the PGT which was to be used to obtain the PT.
     */
    public CASProxyTicketAcquisitionException(String service, String pgtIou){
        super(couldNotObtainProxyTicket);
        
        String[] parameters = new String[2];
        parameters[0] = service;
        parameters[1] = pgtIou;
        this.setParameters(parameters);
    }
    
    /** 
     * Exception thrown when cannot obtain proxy ticket for a given service using the given pgtIou.
     * @param service - service for which a PT was requested.
     * @param pgtIou - the pgtIou for the PGT which was to be used to obtain the PT.
     * @param cause - underlying cause of the error condition
     */
    public CASProxyTicketAcquisitionException(String service, String pgtIou, Throwable cause){
        super(couldNotObtainProxyTicket, cause);
        
        String[] parameters = new String[2];
        parameters[0] = service;
        parameters[1] = pgtIou;
        this.setParameters(parameters);
    }
}


/* CASException.java
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