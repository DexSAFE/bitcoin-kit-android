package io.dexnet.bitcoincore.managers

import io.dexnet.bitcoincore.storage.UnspentOutput

interface IUnspentOutputProvider {
    fun getSpendableUtxo(): List<UnspentOutput>
}
