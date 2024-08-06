package io.dexnet.dashkit.messages

import io.dexnet.bitcoincore.extensions.toReversedHex
import io.dexnet.bitcoincore.io.BitcoinInputMarkable
import io.dexnet.bitcoincore.network.messages.IMessage
import io.dexnet.bitcoincore.network.messages.IMessageParser
import io.dexnet.bitcoincore.serializers.TransactionSerializer
import io.dexnet.bitcoincore.storage.FullTransaction

class TransactionLockMessage(var transaction: FullTransaction) : IMessage {
    override fun toString(): String {
        return "TransactionLockMessage(${transaction.header.hash.toReversedHex()})"
    }
}

class TransactionLockMessageParser : IMessageParser {
    override val command: String = "ix"

    override fun parseMessage(input: BitcoinInputMarkable): IMessage {
        val transaction = TransactionSerializer.deserialize(input)
        return TransactionLockMessage(transaction)
    }
}
