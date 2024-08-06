package io.dexnet.bitcoincore.blocks

import io.dexnet.bitcoincore.models.Block
import io.dexnet.bitcoincore.models.Transaction

interface IBlockchainDataListener {
    fun onBlockInsert(block: Block)
    fun onTransactionsUpdate(inserted: List<Transaction>, updated: List<Transaction>, block: Block?)
    fun onTransactionsDelete(hashes: List<String>)
}
