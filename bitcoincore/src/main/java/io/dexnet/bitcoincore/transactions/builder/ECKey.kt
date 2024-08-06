package io.dexnet.bitcoincore.transactions.builder

import io.dexnet.bitcoincore.crypto.schnorr.Schnorr
import io.horizontalsystems.hdwalletkit.ECKey

fun ECKey.signSchnorr(input: ByteArray, auxRand: ByteArray = ByteArray(32)): ByteArray {
    return Schnorr.sign(input, privKeyBytes, auxRand)
}

fun ECKey.verifySchnorr(input: ByteArray, signature: ByteArray): Boolean {
    return Schnorr.verify(input, pubKeyXCoord, signature)
}
