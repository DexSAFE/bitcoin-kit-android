package io.dexnet.dashkit.instantsend.instantsendlock

import io.dexnet.bitcoincore.core.HashBytes
import io.dexnet.dashkit.instantsend.InstantSendLockValidator
import io.dexnet.dashkit.messages.ISLockMessage

class InstantSendLockManager(private val instantSendLockValidator: InstantSendLockValidator) {
    private val relayedLocks = mutableMapOf<HashBytes, io.dexnet.dashkit.messages.ISLockMessage>()

    fun add(relayed: io.dexnet.dashkit.messages.ISLockMessage) {
        relayedLocks[HashBytes(relayed.txHash)] = relayed
    }

    fun takeRelayedLock(txHash: ByteArray): io.dexnet.dashkit.messages.ISLockMessage? {
        relayedLocks[HashBytes(txHash)]?.let {
            relayedLocks.remove(HashBytes(txHash))
            return it
        }
        return null
    }

    @Throws
    fun validate(isLock: io.dexnet.dashkit.messages.ISLockMessage) {
        instantSendLockValidator.validate(isLock)
    }

}
