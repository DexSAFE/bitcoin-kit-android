package io.dexnet.dashkit.masternodelist

import io.dexnet.bitcoincore.core.HashBytes
import io.dexnet.bitcoincore.core.IHasher
import io.dexnet.dashkit.models.CoinbaseTransaction
import io.dexnet.dashkit.models.CoinbaseTransactionSerializer

class MasternodeCbTxHasher(private val coinbaseTransactionSerializer: CoinbaseTransactionSerializer, private val hasher: IHasher) {

    fun hash(coinbaseTransaction: CoinbaseTransaction): HashBytes {
        val serialized = coinbaseTransactionSerializer.serialize(coinbaseTransaction)

        return HashBytes(hasher.hash(serialized))
    }

}
