/*
 *  Copyright (c) 2000-2003 Yale University. All rights reserved.
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

package edu.yale.its.tp.cas;

import java.io.*;
import java.util.*;
import java.net.*;

/** Utility methods for services that use the central authentication service. */
public class CASServiceUtil {

    private static final boolean debug = false;

    /** The URL for CAS's page for primary authentication. */
    public static final String authenticateURL
        = "https://secure.its.yale.edu/cas/servlet/login";

    /** The URL exposing CAS's validation protocol. */
    public static final String validateURL
        = "https://secure.its.yale.edu/cas/servlet/validate";

    /** The URL which users can visit to destroy CAS credentials. */
    public static final String logoutURL
        = "https://secure.its.yale.edu/cas/servlet/logout";

    /**
     * Returns the NetID of the owner of the given ticket, or null if the
     * ticket isn't valid.
     * @param service the service ID for the application validating the
     *        ticket
     * @param ticket the opaque service ticket (ST) to validate
     */
    public static String validate(String service, String ticket)
            throws java.io.IOException {
        Properties p = System.getProperties();
        p.put("java.protocol.handler.pkgs",
            "com.sun.net.ssl.internal.www.protocol");
        System.setProperties(p);                // < 1.2 compatible
        URL u = new URL(
            validateURL + "?ticket=" + ticket + "&service=" + service);
        BufferedReader in = new BufferedReader(new InputStreamReader(
            u.openStream()));
        if (in == null)
            return null;
        else {
            String line1 = in.readLine();
            String line2 = in.readLine();
            if (line1.equals("no"))
                return null;
            else
                return line2;
        }
    }
}
