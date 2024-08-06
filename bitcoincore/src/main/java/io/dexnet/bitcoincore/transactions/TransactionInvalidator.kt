package io.dexnet.bitcoincore.transactions

import io.dexnet.bitcoincore.blocks.IBlockchainDataListener
import io.dexnet.bitcoincore.core.IStorage
import io.dexnet.bitcoincore.core.ITransactionInfoConverter
import io.dexnet.bitcoincore.models.InvalidTransaction
import io.dexnet.bitcoincore.models.Transaction
import io.dexnet.bitcoincore.storage.FullTransactionInfo

class TransactionInvalidator(
        private val storage: IStorage,
        private val transactionInfoConverter: ITransactionInfoConverter,
        private val listener: IBlockchainDataListener
) {

    fun invalidate(transaction: Transaction) {
        val invalidTransactionsFullInfo = getDescendantTransactionsFullInfo(transaction.hash)

        if (invalidTransactionsFullInfo.isEmpty()) return

        invalidTransactionsFullInfo.forEach { fullTxInfo ->
            fullTxInfo.header.status = Transaction.Status.INVALID
        }

        val invalidTransactions = invalidTransactionsFullInfo.map { fullTxInfo ->
            val txInfo = transactionInfoConverter.transactionInfo(fullTxInfo)
            val serializedTxInfo = txInfo.serialize()
            InvalidTransaction(fullTxInfo.header, serializedTxInfo, fullTxInfo.rawTransaction)
        }

        storage.moveTransactionToInvalidTransactions(invalidTransactions)
        listener.onTransactionsUpdate(listOf(), invalidTransactions, null)
    }

    private fun getDescendantTransactionsFullInfo(txHash: ByteArray): List<FullTransactionInfo> {
        val fullTransactionInfo = storage.getFullTransactionInfo(txHash) ?: return listOf()
        val list = mutableListOf(fullTransactionInfo)

        val inputs = storage.getTransactionInputsByPrevOutputTxHash(fullTransactionInfo.header.hash)

        inputs.forEach { input ->
            val descendantTxs = getDescendantTransactionsFullInfo(input.transactionHash)
            list.addAll(descendantTxs)
        }

        return list
    }

}
