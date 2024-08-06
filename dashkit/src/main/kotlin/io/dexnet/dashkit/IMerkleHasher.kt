package io.dexnet.dashkit

interface IMerkleHasher {
    fun hash(first: ByteArray, second: ByteArray) : ByteArray
}