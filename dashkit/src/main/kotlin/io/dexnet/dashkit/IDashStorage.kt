package io.dexnet.dashkit

import io.dexnet.bitcoincore.models.Block
import io.dexnet.bitcoincore.models.TransactionInput
import io.dexnet.dashkit.models.*

interface IDashStorage {
    fun getBlock(blockHash: ByteArray): Block?
    fun instantTransactionHashes(): List<ByteArray>
    fun instantTransactionInputs(txHash: ByteArray): List<InstantTransactionInput>
    fun getTransactionInputs(txHash: ByteArray): List<TransactionInput>
    fun addInstantTransactionInput(instantTransactionInput: InstantTransactionInput)
    fun addInstantTransactionHash(txHash: ByteArray)
    fun removeInstantTransactionInputs(txHash: ByteArray)
    fun isTransactionExists(txHash: ByteArray): Boolean
    fun getQuorumsByType(quorumType: QuorumType): List<Quorum>

    var masternodes: List<Masternode>
    var masternodeListState: MasternodeListState?
    var quorums: List<Quorum>
}
