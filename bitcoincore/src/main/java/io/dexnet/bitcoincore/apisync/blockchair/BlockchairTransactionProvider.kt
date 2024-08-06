package io.dexnet.bitcoincore.apisync.blockchair

import io.dexnet.bitcoincore.apisync.model.TransactionItem
import io.dexnet.bitcoincore.core.IApiTransactionProvider

class BlockchairTransactionProvider(
    val blockchairApi: BlockchairApi,
    private val blockHashFetcher: IBlockHashFetcher
) : IApiTransactionProvider {

    private fun fillBlockHashes(items: List<TransactionItem>): List<TransactionItem> {
        val hashesMap = blockHashFetcher.fetch(items.map { it.blockHeight }.distinct())
        return items.mapNotNull { item ->
            hashesMap[item.blockHeight]?.let {
                item.copy(blockHash = it)
            }
        }
    }

    override fun transactions(addresses: List<String>, stopHeight: Int?): List<TransactionItem> {
        val items = blockchairApi.transactions(addresses, stopHeight)
        return fillBlockHashes(items)
    }

}
