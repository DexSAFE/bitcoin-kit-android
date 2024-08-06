package io.dexnet.bitcoincore.core

import io.dexnet.bitcoincore.models.TransactionInfo
import io.dexnet.bitcoincore.storage.FullTransactionInfo

class TransactionInfoConverter : ITransactionInfoConverter {
    override lateinit var baseConverter: BaseTransactionInfoConverter

    override fun transactionInfo(fullTransactionInfo: FullTransactionInfo): TransactionInfo {
        return baseConverter.transactionInfo(fullTransactionInfo)
    }
}
