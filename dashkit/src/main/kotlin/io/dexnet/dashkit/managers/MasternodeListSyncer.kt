package io.dexnet.dashkit.managers

import io.dexnet.bitcoincore.BitcoinCore
import io.dexnet.bitcoincore.blocks.IPeerSyncListener
import io.dexnet.bitcoincore.core.IInitialDownload
import io.dexnet.bitcoincore.extensions.toReversedByteArray
import io.dexnet.bitcoincore.network.peer.IPeerTaskHandler
import io.dexnet.bitcoincore.network.peer.Peer
import io.dexnet.bitcoincore.network.peer.PeerGroup
import io.dexnet.bitcoincore.network.peer.task.PeerTask
import io.dexnet.dashkit.tasks.PeerTaskFactory
import io.dexnet.dashkit.tasks.RequestMasternodeListDiffTask
import java.util.concurrent.Executors

class MasternodeListSyncer(
    private val bitcoinCore: BitcoinCore,
    private val peerTaskFactory: PeerTaskFactory,
    private val masternodeListManager: MasternodeListManager,
    private val initialBlockDownload: IInitialDownload
) : IPeerTaskHandler, IPeerSyncListener, PeerGroup.Listener {

    @Volatile
    private var workingPeer: Peer? = null
    private val peersQueue = Executors.newSingleThreadExecutor()

    override fun onPeerSynced(peer: Peer) {
        assignNextSyncPeer()
    }

    override fun onPeerDisconnect(peer: Peer, e: Exception?) {
        if (peer == workingPeer) {
            workingPeer = null

            assignNextSyncPeer()
        }
    }

    private fun assignNextSyncPeer() {
        peersQueue.execute {
            if (workingPeer == null) {
                bitcoinCore.lastBlockInfo?.let { lastBlockInfo ->
                    initialBlockDownload.syncedPeers.firstOrNull()?.let { syncedPeer ->
                        val blockHash = lastBlockInfo.headerHash.toReversedByteArray()
                        val baseBlockHash = masternodeListManager.baseBlockHash

                        if (!blockHash.contentEquals(baseBlockHash)) {
                            val task = peerTaskFactory.createRequestMasternodeListDiffTask(baseBlockHash, blockHash)
                            syncedPeer.addTask(task)

                            workingPeer = syncedPeer
                        }
                    }
                }
            }
        }
    }


    override fun handleCompletedTask(peer: Peer, task: PeerTask): Boolean {
        return when (task) {
            is RequestMasternodeListDiffTask -> {
                task.masternodeListDiffMessage?.let { masternodeListDiffMessage ->
                    masternodeListManager.updateList(masternodeListDiffMessage)
                    workingPeer = null
                }
                true
            }

            else -> false
        }
    }

}