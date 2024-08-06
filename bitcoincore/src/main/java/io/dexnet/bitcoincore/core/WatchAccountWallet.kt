package io.dexnet.bitcoincore.core

import io.dexnet.bitcoincore.models.PublicKey
import io.dexnet.hdwalletkit.HDWallet
import io.dexnet.a.HDWalletAccountWatch

class WatchAccountWallet(private val hdWallet: HDWalletAccountWatch, override val gapLimit: Int): IAccountWallet {

    override fun publicKey(index: Int, external: Boolean): PublicKey {
        val pubKey = hdWallet.publicKey(index, if (external) HDWallet.Chain.EXTERNAL else HDWallet.Chain.INTERNAL)
        return PublicKey(0, index, external, pubKey.publicKey, pubKey.publicKeyHash)
    }

    override fun publicKeys(indices: IntRange, external: Boolean): List<PublicKey> {
        val hdPublicKeys = hdWallet.publicKeys(indices, if (external) HDWallet.Chain.EXTERNAL else HDWallet.Chain.INTERNAL)

        if (hdPublicKeys.size != indices.count()) {
            throw Wallet.HDWalletError.PublicKeysDerivationFailed()
        }

        return indices.mapIndexed { position, index ->
            val hdPublicKey = hdPublicKeys[position]
            PublicKey(0, index, external, hdPublicKey.publicKey, hdPublicKey.publicKeyHash)
        }
    }
}
