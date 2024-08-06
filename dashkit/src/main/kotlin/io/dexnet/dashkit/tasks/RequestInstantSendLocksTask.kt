package io.dexnet.dashkit.tasks

import io.dexnet.bitcoincore.models.InventoryItem
import io.dexnet.bitcoincore.network.messages.GetDataMessage
import io.dexnet.bitcoincore.network.messages.IMessage
import io.dexnet.bitcoincore.network.peer.task.PeerTask
import io.dexnet.dashkit.InventoryType

class RequestInstantSendLocksTask(hashes: List<ByteArray>) : PeerTask() {

    val hashes = hashes.toMutableList()
    var isLocks = mutableListOf<io.dexnet.dashkit.messages.ISLockMessage>()

    override fun start() {
        requester?.send(GetDataMessage(hashes.map { InventoryItem(InventoryType.MSG_ISLOCK, it) }))
    }

    override fun handleMessage(message: IMessage) = when (message) {
        is io.dexnet.dashkit.messages.ISLockMessage -> handleISLockVote(message)
        else -> false
    }

    private fun handleISLockVote(isLockMessage: io.dexnet.dashkit.messages.ISLockMessage): Boolean {
        val hash = hashes.firstOrNull { it.contentEquals(isLockMessage.hash) } ?: return false

        hashes.remove(hash)
        isLocks.add(isLockMessage)

        if (hashes.isEmpty()) {
            listener?.onTaskCompleted(this)
        }

        return true
    }

}
