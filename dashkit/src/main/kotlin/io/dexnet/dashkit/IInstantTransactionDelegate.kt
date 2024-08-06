package io.dexnet.dashkit

// TODO Rename to listener
interface IInstantTransactionDelegate {
    fun onUpdateInstant(transactionHash: ByteArray)
}
