/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.extension.implementation.storage.transactions;

import com.djrapitops.plan.db.access.transactions.Transaction;

import java.util.Collection;
import java.util.UUID;

/**
 * Transaction to remove method results that correspond to {@link com.djrapitops.plan.extension.annotation.InvalidateMethod} annotations.
 *
 * @author Rsl1122
 */
public class RemoveInvalidResultsTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final Collection<String> invalidatedMethods;

    public RemoveInvalidResultsTransaction(String pluginName, UUID serverUUID, Collection<String> invalidatedMethods) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.invalidatedMethods = invalidatedMethods;
    }

    @Override
    protected void performOperations() {
        // TODO implement after storage
    }
}