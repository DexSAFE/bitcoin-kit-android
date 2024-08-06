package io.dexnet.dashkit.instantsend.instantsendlock

import io.dexnet.dashkit.IInstantTransactionDelegate
import io.dexnet.dashkit.instantsend.InstantTransactionManager
import java.util.logging.Logger

class InstantSendLockHandler(
        private val instantTransactionManager: InstantTransactionManager,
        private val instantLockManager: InstantSendLockManager
) {

    var delegate: IInstantTransactionDelegate? = null
    private val logger = Logger.getLogger("InstantSendLockHandler")

    fun handle(transactionHash: ByteArray) {
        // get relayed lock for inserted transaction and check it
        instantLockManager.takeRelayedLock(transactionHash)?.let { lock ->
            validateSendLock(lock)
        }
    }

    fun handle(isLock: io.dexnet.dashkit.messages.ISLockMessage) {
        // check transaction already not in instant
        if (instantTransactionManager.isTransactionInstant(isLock.txHash)) {
            return
        }

        // do nothing if tx doesn't exist
        if (!instantTransactionManager.isTransactionExists(isLock.txHash)) {
            instantLockManager.add(isLock)
            return
        }

        // validation
        validateSendLock(isLock)
    }

    private fun validateSendLock(isLock: io.dexnet.dashkit.messages.ISLockMessage) {
        try {
            instantLockManager.validate(isLock)

            instantTransactionManager.makeInstant(isLock.txHash)
            delegate?.onUpdateInstant(isLock.txHash)
        } catch (e: Exception) {
            logger.severe(e.message)
        }
    }

}
