package io.dexnet.dashkit.models

import io.dexnet.bitcoincore.io.BitcoinOutput
import io.dexnet.bitcoincore.serializers.TransactionSerializer

class CoinbaseTransactionSerializer {

    fun serialize(coinbaseTransaction: CoinbaseTransaction): ByteArray {
        val output = BitcoinOutput()

        output.write(TransactionSerializer.serialize(coinbaseTransaction.transaction))
        output.writeVarInt(coinbaseTransaction.coinbaseTransactionSize)
        output.writeUnsignedShort(coinbaseTransaction.version)
        output.writeUnsignedInt(coinbaseTransaction.height)
        output.write(coinbaseTransaction.merkleRootMNList)

        if (coinbaseTransaction.version >= 2) {
            output.write(coinbaseTransaction.merkleRootQuorums)
        }

        return output.toByteArray()
    }

}
