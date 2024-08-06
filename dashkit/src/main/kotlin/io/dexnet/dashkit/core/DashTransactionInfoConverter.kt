package io.dexnet.dashkit.core

import io.dexnet.bitcoincore.core.BaseTransactionInfoConverter
import io.dexnet.bitcoincore.core.ITransactionInfoConverter
import io.dexnet.bitcoincore.extensions.toHexString
import io.dexnet.bitcoincore.models.InvalidTransaction
import io.dexnet.bitcoincore.models.Transaction
import io.dexnet.bitcoincore.models.TransactionMetadata
import io.dexnet.bitcoincore.models.TransactionStatus
import io.dexnet.bitcoincore.storage.FullTransactionInfo
import io.dexnet.dashkit.instantsend.InstantTransactionManager
import io.dexnet.dashkit.models.DashTransactionInfo

class DashTransactionInfoConverter(private val instantTransactionManager: InstantTransactionManager) : ITransactionInfoConverter {
    override lateinit var baseConverter: BaseTransactionInfoConverter

    override fun transactionInfo(fullTransactionInfo: FullTransactionInfo): DashTransactionInfo {
        val transaction = fullTransactionInfo.header

        if (transaction.status == Transaction.Status.INVALID) {
            (transaction as? InvalidTransaction)?.let {
                return getInvalidTransactionInfo(it, fullTransactionInfo.metadata)
            }
        }

        val txInfo = baseConverter.transactionInfo(fullTransactionInfo)

        return DashTransactionInfo(
                txInfo.uid,
                txInfo.transactionHash,
                txInfo.transactionIndex,
                txInfo.inputs,
                txInfo.outputs,
                txInfo.amount,
                txInfo.type,
                txInfo.fee,
                txInfo.blockHeight,
                txInfo.timestamp,
                txInfo.status,
                txInfo.conflictingTxHash,
                instantTransactionManager.isTransactionInstant(fullTransactionInfo.header.hash)
        )
    }

    private fun getInvalidTransactionInfo(
        transaction: InvalidTransaction,
        metadata: TransactionMetadata
    ): DashTransactionInfo {
        return try {
            DashTransactionInfo(transaction.serializedTxInfo)
        } catch (ex: Exception) {
            DashTransactionInfo(
                uid = transaction.uid,
                transactionHash = transaction.hash.toHexString(),
                transactionIndex = transaction.order,
                inputs = listOf(),
                outputs = listOf(),
                amount = metadata.amount,
                type = metadata.type,
                fee = null,
                blockHeight = null,
                timestamp = transaction.timestamp,
                status = TransactionStatus.INVALID,
                conflictingTxHash = null,
                instantTx = false
            )
        }
    }

}