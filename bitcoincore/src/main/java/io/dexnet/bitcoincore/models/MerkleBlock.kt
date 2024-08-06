package io.dexnet.bitcoincore.models

import io.dexnet.bitcoincore.core.HashBytes
import io.dexnet.bitcoincore.storage.BlockHeader
import io.dexnet.bitcoincore.storage.FullTransaction

class MerkleBlock(val header: BlockHeader, val associatedTransactionHashes: Map<HashBytes, Boolean>) {

    var height: Int? = null
    var associatedTransactions = mutableListOf<FullTransaction>()
    val blockHash = header.hash

    val complete: Boolean
        get() = associatedTransactionHashes.size == associatedTransactions.size

}
