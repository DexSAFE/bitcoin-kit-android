package io.dexnet.dashkit.core

import io.dexnet.bitcoincore.core.IHasher
import io.dexnet.bitcoincore.utils.HashUtils

class SingleSha256Hasher : IHasher {
    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.sha256(data)
    }
}