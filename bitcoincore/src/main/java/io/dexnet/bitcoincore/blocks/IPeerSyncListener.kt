package io.dexnet.bitcoincore.blocks

import io.dexnet.bitcoincore.network.peer.Peer

interface IPeerSyncListener {
    fun onAllPeersSynced() = Unit
    fun onPeerSynced(peer: Peer) = Unit
}
