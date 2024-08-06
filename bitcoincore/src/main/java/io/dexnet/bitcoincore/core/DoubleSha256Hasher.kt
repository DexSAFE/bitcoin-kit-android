package io.dexnet.bitcoincore.core

import io.dexnet.bitcoincore.utils.HashUtils

class DoubleSha256Hasher : IHasher {
    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.doubleSha256(data)
    }
}
