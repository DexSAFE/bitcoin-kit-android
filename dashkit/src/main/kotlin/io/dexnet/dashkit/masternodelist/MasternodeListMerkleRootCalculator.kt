package io.dexnet.dashkit.masternodelist

import io.dexnet.dashkit.models.Masternode

class MasternodeListMerkleRootCalculator(val masternodeMerkleRootCreator: MerkleRootCreator) {

    fun calculateMerkleRoot(sortedMasternodes: List<Masternode>): ByteArray? {
        return masternodeMerkleRootCreator.create(sortedMasternodes.map { it.hash })
    }

}
