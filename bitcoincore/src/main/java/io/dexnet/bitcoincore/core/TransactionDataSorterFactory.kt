package io.dexnet.bitcoincore.core

import io.dexnet.bitcoincore.models.TransactionDataSortType
import io.dexnet.bitcoincore.utils.Bip69Sorter
import io.dexnet.bitcoincore.utils.ShuffleSorter
import io.dexnet.bitcoincore.utils.StraightSorter

class TransactionDataSorterFactory : ITransactionDataSorterFactory {
    override fun sorter(type: TransactionDataSortType): ITransactionDataSorter {
        return when (type) {
            TransactionDataSortType.None -> StraightSorter()
            TransactionDataSortType.Shuffle -> ShuffleSorter()
            TransactionDataSortType.Bip69 -> Bip69Sorter()
        }
    }
}
