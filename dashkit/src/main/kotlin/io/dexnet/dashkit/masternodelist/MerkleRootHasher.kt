package io.dexnet.dashkit.masternodelist

import io.dexnet.bitcoincore.core.IHasher
import io.dexnet.bitcoincore.utils.HashUtils
import io.dexnet.dashkit.IMerkleHasher

class MerkleRootHasher: IHasher, IMerkleHasher {

    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.doubleSha256(data)
    }

    override fun hash(first: ByteArray, second: ByteArray): ByteArray {
        return HashUtils.doubleSha256(first + second)
    }
}
