package io.dexnet.dashkit.messages

import io.dexnet.bitcoincore.extensions.toReversedHex
import io.dexnet.bitcoincore.io.BitcoinOutput
import io.dexnet.bitcoincore.network.messages.IMessage
import io.dexnet.bitcoincore.network.messages.IMessageSerializer

class GetMasternodeListDiffMessage(val baseBlockHash: ByteArray, val blockHash: ByteArray) : IMessage {
    override fun toString(): String {
        return "GetMasternodeListDiffMessage(baseBlockHash=${baseBlockHash.toReversedHex()}, blockHash=${blockHash.toReversedHex()})"
    }
}

class GetMasternodeListDiffMessageSerializer : IMessageSerializer {
    override val command: String = "getmnlistd"

    override fun serialize(message: IMessage): ByteArray? {
        if (message !is io.dexnet.dashkit.messages.GetMasternodeListDiffMessage) {
            return null
        }

        return BitcoinOutput()
                .write(message.baseBlockHash)
                .write(message.blockHash)
                .toByteArray()
    }
}
