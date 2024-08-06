package io.dexnet.bitcoincore.network.messages

import io.dexnet.bitcoincore.io.BitcoinInputMarkable
import io.dexnet.bitcoincore.io.BitcoinOutput

class PongMessage(val nonce: Long) : IMessage {
    override fun toString(): String {
        return "PongMessage(nonce=$nonce)"
    }
}

class PongMessageParser : IMessageParser {
    override val command: String = "pong"

    override fun parseMessage(input: BitcoinInputMarkable): IMessage {
        return PongMessage(input.readLong())
    }
}

class PongMessageSerializer : IMessageSerializer {
    override val command: String = "pong"

    override fun serialize(message: IMessage): ByteArray? {
        if (message !is PongMessage) {
            return null
        }

        return BitcoinOutput()
                .writeLong(message.nonce)
                .toByteArray()
    }
}