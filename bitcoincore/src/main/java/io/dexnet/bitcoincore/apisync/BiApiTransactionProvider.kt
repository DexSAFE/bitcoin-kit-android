package io.dexnet.bitcoincore.apisync

import io.dexnet.bitcoincore.apisync.model.TransactionItem
import io.dexnet.bitcoincore.core.IApiTransactionProvider
import io.dexnet.bitcoincore.managers.ApiSyncStateManager

class BiApiTransactionProvider(
    private val restoreProvider: IApiTransactionProvider,
    private val syncProvider: IApiTransactionProvider,
    private val syncStateManager: ApiSyncStateManager
) : IApiTransactionProvider {

    override fun transactions(addresses: List<String>, stopHeight: Int?): List<TransactionItem> =
        if (syncStateManager.restored) {
            syncProvider.transactions(addresses, stopHeight)
        } else {
            restoreProvider.transactions(addresses, stopHeight)
        }
}
