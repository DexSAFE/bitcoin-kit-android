package io.dexnet.bitcoincore.transactions.builder

import io.dexnet.bitcoincore.core.IPrivateWallet
import io.dexnet.bitcoincore.models.Transaction
import io.dexnet.bitcoincore.models.TransactionOutput
import io.dexnet.bitcoincore.serializers.TransactionSerializer
import io.dexnet.bitcoincore.storage.InputToSign
import io.dexnet.hdwalletkit.Utils

class SchnorrInputSigner(
    private val hdWallet: IPrivateWallet
) {
    fun sigScriptData(
        transaction: Transaction,
        inputsToSign: List<InputToSign>,
        outputs: List<TransactionOutput>,
        index: Int
    ): List<ByteArray> {
        val input = inputsToSign[index]
        val publicKey = input.previousOutputPublicKey
        val tweakedPrivateKey = checkNotNull(hdWallet.privateKey(publicKey.account, publicKey.index, publicKey.external).tweakedOutputKey) {
            throw Error.NoPrivateKey()
        }
        val serializedTransaction = TransactionSerializer.serializeForTaprootSignature(transaction, inputsToSign, outputs, index)

        val signatureHash = Utils.taggedHash("TapSighash", serializedTransaction)
        val signature = tweakedPrivateKey.signSchnorr(signatureHash)

        return listOf(signature)
    }

    open class Error : Exception() {
        class NoPrivateKey : Error()
        class NoPreviousOutput : Error()
        class NoPreviousOutputAddress : Error()
    }
}
