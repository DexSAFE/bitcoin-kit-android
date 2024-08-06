package io.dexnet.bitcoincore

import io.dexnet.bitcoincore.apisync.legacy.IPublicKeyFetcher
import io.dexnet.bitcoincore.core.IPublicKeyManager
import io.dexnet.bitcoincore.managers.AccountPublicKeyManager
import io.dexnet.bitcoincore.managers.BloomFilterManager
import io.dexnet.bitcoincore.managers.IBloomFilterProvider
import io.dexnet.bitcoincore.managers.RestoreKeyConverterChain
import io.dexnet.bitcoincore.models.PublicKey
import io.dexnet.bitcoincore.models.WatchAddressPublicKey

class WatchAddressPublicKeyManager(
    private val publicKey: WatchAddressPublicKey,
    private val restoreKeyConverter: RestoreKeyConverterChain
) : IPublicKeyFetcher, IPublicKeyManager, IBloomFilterProvider {

    override fun publicKeys(indices: IntRange, external: Boolean) = listOf(publicKey)

    override fun changePublicKey() = publicKey

    override fun receivePublicKey() = publicKey

    override fun usedExternalPublicKeys(): List<PublicKey> = listOf(publicKey)

    override fun fillGap() {
        bloomFilterManager?.regenerateBloomFilter()
    }

    override fun addKeys(keys: List<PublicKey>) = Unit

    override fun gapShifts(): Boolean = false

    override fun getPublicKeyByPath(path: String): PublicKey {
        throw AccountPublicKeyManager.Error.InvalidPath
    }

    override var bloomFilterManager: BloomFilterManager? = null

    override fun getBloomFilterElements() = restoreKeyConverter.bloomFilterElements(publicKey)
}
