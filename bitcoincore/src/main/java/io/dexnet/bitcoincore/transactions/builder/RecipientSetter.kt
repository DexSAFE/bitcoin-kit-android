package io.dexnet.bitcoincore.transactions.builder

import io.dexnet.bitcoincore.core.IPluginData
import io.dexnet.bitcoincore.core.IRecipientSetter
import io.dexnet.bitcoincore.core.PluginManager
import io.dexnet.bitcoincore.transactions.builder.MutableTransaction
import io.dexnet.bitcoincore.utils.IAddressConverter

class RecipientSetter(
        private val addressConverter: IAddressConverter,
        private val pluginManager: PluginManager
) : IRecipientSetter {

    override fun setRecipient(mutableTransaction: MutableTransaction, toAddress: String, value: Long, pluginData: Map<Byte, IPluginData>, skipChecking: Boolean) {
        mutableTransaction.recipientAddress = addressConverter.convert(toAddress)
        mutableTransaction.recipientValue = value

        pluginManager.processOutputs(mutableTransaction, pluginData, skipChecking)
    }

}
