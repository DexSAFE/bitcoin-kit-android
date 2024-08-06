package io.dexnet.bitcoincore.apisync.blockchair

import io.dexnet.bitcoincore.apisync.model.BlockHeaderItem

class BlockchairLastBlockProvider(
    private val blockchairApi: BlockchairApi
) {
    fun lastBlockHeader(): BlockHeaderItem {
        return blockchairApi.lastBlockHeader()
    }
}
