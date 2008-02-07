/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.services.sessions.SessionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2._PermissionsVerifierDisp;
import Ice.Current;
import Ice.StringHolder;

/**
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 12:28:54 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1175 $")
public class PermissionsVerifierI extends _PermissionsVerifierDisp {

    private final static Log log = LogFactory
            .getLog(PermissionsVerifierI.class);

    private final SessionManager manager;

    public PermissionsVerifierI(SessionManager manager) {
        this.manager = manager;
    }

    public boolean checkPermissions(String userId, String password,
            StringHolder reason, Current __current) {
        boolean value = false;
        try {
            value = manager.executePasswordCheck(userId, password);
        } catch (Throwable t) {
            reason.value = "Internal error. Please contact your administrator.";
            log.error("Exception thrown while checking password for:" + userId,
                    t);
        }
        return value;
    }

}
