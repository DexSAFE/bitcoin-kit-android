package io.dexnet.litecoinkit.validators

import io.dexnet.bitcoincore.blocks.validators.BlockValidatorException
import io.dexnet.bitcoincore.blocks.validators.IBlockChainedValidator
import io.dexnet.bitcoincore.crypto.CompactBits
import io.dexnet.bitcoincore.extensions.toHexString
import io.dexnet.bitcoincore.io.BitcoinOutput
import io.dexnet.bitcoincore.models.Block
import io.dexnet.litecoinkit.ScryptHasher
import java.math.BigInteger

class ProofOfWorkValidator(private val scryptHasher: ScryptHasher) : IBlockChainedValidator {

    override fun validate(block: Block, previousBlock: Block) {
        val blockHeaderData = getSerializedBlockHeader(block)

        val powHash = scryptHasher.hash(blockHeaderData).toHexString()

        check(BigInteger(powHash, 16) < CompactBits.decode(block.bits)) {
            throw BlockValidatorException.InvalidProofOfWork()
        }
    }

    private fun getSerializedBlockHeader(block: Block): ByteArray {
        return BitcoinOutput()
                .writeInt(block.version)
                .write(block.previousBlockHash)
                .write(block.merkleRoot)
                .writeUnsignedInt(block.timestamp)
                .writeUnsignedInt(block.bits)
                .writeUnsignedInt(block.nonce)
                .toByteArray()
    }

    override fun isBlockValidatable(block: Block, previousBlock: Block): Boolean {
        return true
    }

}
