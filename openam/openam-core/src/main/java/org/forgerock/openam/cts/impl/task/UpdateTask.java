/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */
package org.forgerock.openam.cts.impl.task;

import org.forgerock.openam.cts.api.tokens.Token;
import org.forgerock.openam.cts.exceptions.CoreTokenException;
import org.forgerock.openam.cts.exceptions.LDAPOperationFailedException;
import org.forgerock.openam.cts.impl.LDAPAdapter;
import org.forgerock.openam.cts.impl.queue.ResultHandler;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;

import java.text.MessageFormat;

/**
 * Responsible for updating the LDAP persistence with the provided Token.
 */
public class UpdateTask implements Task {
    private final Token token;
    private final ResultHandler<Token> handler;

    /**
     * @param token Non null Token to update.
     * @param handler Non null handler to notify.
     */
    public UpdateTask(Token token, ResultHandler<Token> handler) {
        this.token = token;
        this.handler = handler;
    }

    /**
     * Performs a read of the store to determine the state of the Token.
     *
     * If the Token exists, then an update is performed, otherwise a create is
     * performed.
     *
     * @param connection Non null Connection.
     * @param ldapAdapter Non null for LDAP operations.
     * @throws CoreTokenException If there was an error of any kind.
     */
    @Override
    public void execute(Connection connection, LDAPAdapter ldapAdapter) throws CoreTokenException {
        try {
            Token previous = ldapAdapter.read(connection, token.getTokenId());
            if (previous == null) {
                ldapAdapter.create(connection, token);
            } else {
                ldapAdapter.update(connection, previous, token);
            }
            handler.processResults(token);
        } catch (ErrorResultException e) {
            LDAPOperationFailedException error = new LDAPOperationFailedException(e.getResult());
            handler.processError(error);
            throw error;
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format("UpdateTask: {0}", token.getTokenId());
    }
}
