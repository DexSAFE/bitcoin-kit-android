package io.dexnet.dashkit.tasks

import io.dexnet.bitcoincore.network.messages.IMessage
import io.dexnet.bitcoincore.network.peer.task.PeerTask
import io.dexnet.dashkit.messages.GetMasternodeListDiffMessage
import io.dexnet.dashkit.messages.MasternodeListDiffMessage
import java.util.concurrent.TimeUnit

class RequestMasternodeListDiffTask(private val baseBlockHash: ByteArray, private val blockHash: ByteArray) : PeerTask() {

    var masternodeListDiffMessage: MasternodeListDiffMessage? = null

    init {
        allowedIdleTime = TimeUnit.SECONDS.toMillis(5)
    }

    override fun handleTimeout() {
        listener?.onTaskFailed(this, Exception("RequestMasternodeListDiffTask Timeout"))
    }


    override fun start() {
        requester?.send(
            io.dexnet.dashkit.messages.GetMasternodeListDiffMessage(
                baseBlockHash,
                blockHash
            )
        )
        resetTimer()
    }

    override fun handleMessage(message: IMessage): Boolean {
        if (message is MasternodeListDiffMessage
                && message.baseBlockHash.contentEquals(baseBlockHash)
                && message.blockHash.contentEquals(blockHash)) {

            masternodeListDiffMessage = message

            listener?.onTaskCompleted(this)

            return true
        }

        return false
    }
}
