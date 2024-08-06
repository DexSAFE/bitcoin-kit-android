package io.dexnet.bitcoincore.core

interface IHasher {
    fun hash(data: ByteArray) : ByteArray
}
