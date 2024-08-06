package io.dexnet.bitcoincore.blocks

import io.dexnet.bitcoincore.crypto.BloomFilter
import io.dexnet.bitcoincore.managers.BloomFilterManager
import io.dexnet.bitcoincore.network.peer.Peer
import io.dexnet.bitcoincore.network.peer.PeerGroup
import io.dexnet.bitcoincore.network.peer.PeerManager

class BloomFilterLoader(private val bloomFilterManager: BloomFilterManager, private val peerManager: PeerManager)
    : PeerGroup.Listener, BloomFilterManager.Listener {

    override fun onPeerConnect(peer: Peer) {
        bloomFilterManager.bloomFilter?.let {
            peer.filterLoad(it)
        }
    }

    override fun onFilterUpdated(bloomFilter: BloomFilter) {
        peerManager.connected().forEach {
            it.filterLoad(bloomFilter)
        }
    }
}
