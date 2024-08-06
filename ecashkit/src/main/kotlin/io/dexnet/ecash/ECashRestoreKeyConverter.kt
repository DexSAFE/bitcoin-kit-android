package io.dexnet.ecash

import io.dexnet.bitcoincore.extensions.toHexString
import io.dexnet.bitcoincore.managers.IRestoreKeyConverter
import io.dexnet.bitcoincore.models.PublicKey

class ECashRestoreKeyConverter: IRestoreKeyConverter {
    override fun keysForApiRestore(publicKey: PublicKey): List<String> {
        return listOf(publicKey.publicKeyHash.toHexString())
    }

    override fun bloomFilterElements(publicKey: PublicKey): List<ByteArray> {
        return listOf(publicKey.publicKeyHash, publicKey.publicKey)
    }
}
