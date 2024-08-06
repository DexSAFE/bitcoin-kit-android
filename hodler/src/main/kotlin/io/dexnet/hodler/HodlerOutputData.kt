package io.dexnet.hodler

import io.dexnet.bitcoincore.core.IPluginOutputData

class HodlerOutputData(val lockTimeInterval: LockTimeInterval,
                       val addressString: String) : IPluginOutputData {

    var approxUnlockTime: Long? = null

    fun serialize(): String {
        return listOf(lockTimeInterval.serialize(), addressString).joinToString("|")
    }

    companion object {
        fun parse(serialized: String?): HodlerOutputData {
            val (lockTimeIntervalStr, addressString) = checkNotNull(serialized?.split("|"))

            val lockTimeInterval = checkNotNull(LockTimeInterval.deserialize(lockTimeIntervalStr))
            return HodlerOutputData(lockTimeInterval, addressString)
        }
    }
}
