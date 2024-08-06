package io.dexnet.dashkit.tasks

class PeerTaskFactory {

    fun createRequestMasternodeListDiffTask(baseBlockHash: ByteArray, blockHash: ByteArray): RequestMasternodeListDiffTask {
        return RequestMasternodeListDiffTask(baseBlockHash, blockHash)
    }

}
