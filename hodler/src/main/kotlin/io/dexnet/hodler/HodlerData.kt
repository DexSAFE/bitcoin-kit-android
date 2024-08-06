package io.dexnet.hodler

import io.dexnet.bitcoincore.core.IPluginData

data class HodlerData(val lockTimeInterval: LockTimeInterval) : IPluginData
