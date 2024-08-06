package io.dexnet.dashkit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.dexnet.bitcoincore.AbstractKit
import io.dexnet.bitcoincore.BitcoinCore
import io.dexnet.bitcoincore.BitcoinCore.SyncMode
import io.dexnet.bitcoincore.BitcoinCoreBuilder
import io.dexnet.bitcoincore.apisync.BiApiTransactionProvider
import io.dexnet.bitcoincore.apisync.InsightApi
import io.dexnet.bitcoincore.apisync.blockchair.BlockchairApi
import io.dexnet.bitcoincore.apisync.blockchair.BlockchairBlockHashFetcher
import io.dexnet.bitcoincore.apisync.blockchair.BlockchairTransactionProvider
import io.dexnet.bitcoincore.blocks.validators.BlockValidatorChain
import io.dexnet.bitcoincore.blocks.validators.BlockValidatorSet
import io.dexnet.bitcoincore.blocks.validators.ProofOfWorkValidator
import io.dexnet.bitcoincore.extensions.hexToByteArray
import io.dexnet.bitcoincore.managers.*
import io.dexnet.bitcoincore.models.Address
import io.dexnet.bitcoincore.models.BalanceInfo
import io.dexnet.bitcoincore.models.BlockInfo
import io.dexnet.bitcoincore.models.Checkpoint
import io.dexnet.bitcoincore.models.TransactionFilterType
import io.dexnet.bitcoincore.models.TransactionInfo
import io.dexnet.bitcoincore.models.WatchAddressPublicKey
import io.dexnet.bitcoincore.network.Network
import io.dexnet.bitcoincore.storage.Storage
import io.dexnet.bitcoincore.transactions.TransactionSizeCalculator
import io.dexnet.bitcoincore.utils.Base58AddressConverter
import io.dexnet.bitcoincore.utils.MerkleBranch
import io.dexnet.bitcoincore.utils.PaymentAddressParser
import io.dexnet.dashkit.core.SingleSha256Hasher
import io.dexnet.dashkit.instantsend.*
import io.dexnet.dashkit.instantsend.instantsendlock.InstantSendLockHandler
import io.dexnet.dashkit.instantsend.instantsendlock.InstantSendLockManager
import io.dexnet.dashkit.instantsend.transactionlockvote.TransactionLockVoteHandler
import io.dexnet.dashkit.instantsend.transactionlockvote.TransactionLockVoteManager
import io.dexnet.dashkit.managers.*
import io.dexnet.dashkit.masternodelist.*
import io.dexnet.dashkit.messages.*
import io.dexnet.dashkit.models.CoinbaseTransactionSerializer
import io.dexnet.dashkit.models.DashTransactionInfo
import io.dexnet.dashkit.models.InstantTransactionState
import io.dexnet.dashkit.storage.DashKitDatabase
import io.dexnet.dashkit.storage.DashStorage
import io.dexnet.dashkit.tasks.PeerTaskFactory
import io.dexnet.dashkit.validators.DarkGravityWaveTestnetValidator
import io.dexnet.dashkit.validators.DarkGravityWaveValidator
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDWallet.Purpose
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.Single

class DashKit : AbstractKit, IInstantTransactionDelegate, BitcoinCore.Listener {
    enum class NetworkType {
        MainNet,
        TestNet
    }

    interface Listener {
        fun onTransactionsUpdate(inserted: List<DashTransactionInfo>, updated: List<DashTransactionInfo>)
        fun onTransactionsDelete(hashes: List<String>)
        fun onBalanceUpdate(balance: BalanceInfo)
        fun onLastBlockInfoUpdate(blockInfo: BlockInfo)
        fun onKitStateUpdate(state: BitcoinCore.KitState)
    }

    var listener: Listener? = null

    override var bitcoinCore: BitcoinCore
    override var network: Network

    private val dashStorage: DashStorage
    private val instantSend: InstantSend
    private val dashTransactionInfoConverter: io.dexnet.dashkit.core.DashTransactionInfoConverter

    constructor(
        context: Context,
        words: List<String>,
        passphrase: String,
        walletId: String,
        networkType: NetworkType = defaultNetworkType,
        peerSize: Int = defaultPeerSize,
        syncMode: SyncMode = defaultSyncMode,
        confirmationsThreshold: Int = defaultConfirmationsThreshold
    ) : this(context, Mnemonic().toSeed(words, passphrase), walletId, networkType, peerSize, syncMode, confirmationsThreshold)

    constructor(
        context: Context,
        seed: ByteArray,
        walletId: String,
        networkType: NetworkType = defaultNetworkType,
        peerSize: Int = defaultPeerSize,
        syncMode: SyncMode = defaultSyncMode,
        confirmationsThreshold: Int = defaultConfirmationsThreshold
    ) : this(
        context = context,
        extendedKey = HDExtendedKey(seed, Purpose.BIP44),
        walletId = walletId,
        networkType = networkType,
        peerSize = peerSize,
        syncMode = syncMode,
        confirmationsThreshold = confirmationsThreshold
    )

    constructor(
        context: Context,
        watchAddress: String,
        walletId: String,
        networkType: NetworkType = defaultNetworkType,
        peerSize: Int = defaultPeerSize,
        syncMode: SyncMode = defaultSyncMode,
        confirmationsThreshold: Int = defaultConfirmationsThreshold
    ) : this(
        context = context,
        extendedKey = null,
        watchAddress = parseAddress(watchAddress, network(networkType)),
        walletId = walletId,
        networkType = networkType,
        peerSize = peerSize,
        syncMode = syncMode,
        confirmationsThreshold = confirmationsThreshold
    )

    constructor(
        context: Context,
        extendedKey: HDExtendedKey,
        walletId: String,
        networkType: NetworkType = defaultNetworkType,
        peerSize: Int = defaultPeerSize,
        syncMode: SyncMode = defaultSyncMode,
        confirmationsThreshold: Int = defaultConfirmationsThreshold
    ) : this(
        context = context,
        extendedKey = extendedKey,
        watchAddress = null,
        walletId = walletId,
        networkType = networkType,
        peerSize = peerSize,
        syncMode = syncMode,
        confirmationsThreshold = confirmationsThreshold
    )
    /**
     * @constructor Creates and initializes the BitcoinKit
     * @param context The Android context
     * @param extendedKey HDExtendedKey that contains HDKey and version
     * @param watchAddress address for watching in read-only mode
     * @param walletId an arbitrary ID of type String.
     * @param networkType The network type. The default is MainNet.
     * @param peerSize The # of peer-nodes required. The default is 10 peers.
     * @param syncMode How the kit syncs with the blockchain. The default is SyncMode.Api().
     * @param confirmationsThreshold How many confirmations required to be considered confirmed. The default is 6 confirmations.
     */
    private constructor(
        context: Context,
        extendedKey: HDExtendedKey?,
        watchAddress: Address?,
        walletId: String,
        networkType: NetworkType,
        peerSize: Int,
        syncMode: SyncMode,
        confirmationsThreshold: Int
    ) {
        val coreDatabase = io.dexnet.bitcoincore.storage.CoreDatabase.getInstance(context, getDatabaseNameCore(networkType, walletId, syncMode))
        val dashDatabase = DashKitDatabase.getInstance(context, getDatabaseName(networkType, walletId, syncMode))

        val coreStorage = Storage(coreDatabase)
        dashStorage = DashStorage(dashDatabase, coreStorage)

        network = network(networkType)

        val checkpoint = Checkpoint.resolveCheckpoint(syncMode, network, coreStorage)
        val apiSyncStateManager = ApiSyncStateManager(coreStorage, network.syncableFromApi && syncMode !is SyncMode.Full)

        val apiTransactionProvider = apiTransactionProvider(networkType, syncMode, apiSyncStateManager)

        val paymentAddressParser = PaymentAddressParser("dash", removeScheme = true)
        val instantTransactionManager = InstantTransactionManager(dashStorage, InstantSendFactory(), InstantTransactionState())

        dashTransactionInfoConverter =
            io.dexnet.dashkit.core.DashTransactionInfoConverter(instantTransactionManager)

        val blockHelper = BlockValidatorHelper(coreStorage)

        val blockValidatorSet = BlockValidatorSet()
        blockValidatorSet.addBlockValidator(ProofOfWorkValidator())

        val blockValidatorChain = BlockValidatorChain()

        if (network is MainNetDash) {
            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 68589))
        } else {
            blockValidatorChain.add(DarkGravityWaveTestnetValidator(targetSpacing, targetTimespan, maxTargetBits, 4002))
            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 4002))
        }

        blockValidatorSet.addBlockValidator(blockValidatorChain)

        val watchAddressPublicKey = watchAddress?.let {
            WatchAddressPublicKey(watchAddress.lockingScriptPayload, watchAddress.scriptType)
        }

        bitcoinCore = BitcoinCoreBuilder()
            .setContext(context)
            .setExtendedKey(extendedKey)
            .setWatchAddressPublicKey(watchAddressPublicKey)
            .setPurpose(Purpose.BIP44)
            .setNetwork(network)
            .setCheckpoint(checkpoint)
            .setPaymentAddressParser(paymentAddressParser)
            .setPeerSize(peerSize)
            .setSyncMode(syncMode)
            .setConfirmationThreshold(confirmationsThreshold)
            .setStorage(coreStorage)
            .setBlockHeaderHasher(X11Hasher())
            .setApiTransactionProvider(apiTransactionProvider)
            .setApiSyncStateManager(apiSyncStateManager)
            .setTransactionInfoConverter(dashTransactionInfoConverter)
            .setBlockValidator(blockValidatorSet)
            .build()

        bitcoinCore.listener = this

        //  extending bitcoinCore

        bitcoinCore.addMessageParser(MasternodeListDiffMessageParser())
            .addMessageParser(TransactionLockMessageParser())
            .addMessageParser(TransactionLockVoteMessageParser())
            .addMessageParser(io.dexnet.dashkit.messages.ISLockMessageParser())
            .addMessageParser(TransactionMessageParser())

        bitcoinCore.addMessageSerializer(io.dexnet.dashkit.messages.GetMasternodeListDiffMessageSerializer())

        val merkleRootHasher = MerkleRootHasher()
        val merkleRootCreator = MerkleRootCreator(merkleRootHasher)
        val masternodeListMerkleRootCalculator = MasternodeListMerkleRootCalculator(merkleRootCreator)
        val masternodeCbTxHasher = MasternodeCbTxHasher(CoinbaseTransactionSerializer(), merkleRootHasher)

        val quorumListManager = QuorumListManager(dashStorage, QuorumListMerkleRootCalculator(merkleRootCreator), QuorumSortedList())
        val masternodeListManager = MasternodeListManager(
            dashStorage,
            masternodeListMerkleRootCalculator,
            masternodeCbTxHasher,
            MerkleBranch(),
            MasternodeSortedList(),
            quorumListManager
        )
        val masternodeSyncer = MasternodeListSyncer(bitcoinCore, PeerTaskFactory(), masternodeListManager, bitcoinCore.initialDownload)

        bitcoinCore.addPeerTaskHandler(masternodeSyncer)
        bitcoinCore.addPeerSyncListener(masternodeSyncer)
        bitcoinCore.addPeerGroupListener(masternodeSyncer)

        val base58AddressConverter = Base58AddressConverter(network.addressVersion, network.addressScriptVersion)
        bitcoinCore.addRestoreKeyConverter(Bip44RestoreKeyConverter(base58AddressConverter))

        val singleHasher = SingleSha256Hasher()
        val bls = BLS()
        val transactionLockVoteValidator = TransactionLockVoteValidator(dashStorage, singleHasher, bls)
        val instantSendLockValidator = InstantSendLockValidator(quorumListManager, bls)

        val transactionLockVoteManager = TransactionLockVoteManager(transactionLockVoteValidator)
        val instantSendLockManager = InstantSendLockManager(instantSendLockValidator)

        val instantSendLockHandler = InstantSendLockHandler(instantTransactionManager, instantSendLockManager)
        instantSendLockHandler.delegate = this
        val transactionLockVoteHandler = TransactionLockVoteHandler(instantTransactionManager, transactionLockVoteManager)
        transactionLockVoteHandler.delegate = this

        val instantSend = InstantSend(bitcoinCore.transactionSyncer, transactionLockVoteHandler, instantSendLockHandler)
        this.instantSend = instantSend

        bitcoinCore.addInventoryItemsHandler(instantSend)
        bitcoinCore.addPeerTaskHandler(instantSend)

        val calculator = TransactionSizeCalculator()
        val confirmedUnspentOutputProvider = ConfirmedUnspentOutputProvider(coreStorage, confirmationsThreshold)
        bitcoinCore.prependUnspentOutputSelector(UnspentOutputSelector(calculator, confirmedUnspentOutputProvider))
        bitcoinCore.prependUnspentOutputSelector(UnspentOutputSelectorSingleNoChange(calculator, confirmedUnspentOutputProvider))
    }

    private fun apiTransactionProvider(
        networkType: NetworkType,
        syncMode: SyncMode,
        apiSyncStateManager: ApiSyncStateManager
    ) = when (networkType) {
        NetworkType.MainNet -> {
            val insightApiProvider = InsightApi("https://insight.dash.org/insight-api")

            if (syncMode is SyncMode.Blockchair) {
                val blockchairApi = BlockchairApi(syncMode.key, network.blockchairChainId)
                val blockchairBlockHashFetcher = BlockchairBlockHashFetcher(blockchairApi)
                val blockchairProvider = BlockchairTransactionProvider(blockchairApi, blockchairBlockHashFetcher)

                BiApiTransactionProvider(
                    restoreProvider = insightApiProvider,
                    syncProvider = blockchairProvider,
                    syncStateManager = apiSyncStateManager
                )
            } else {
                insightApiProvider
            }
        }

        NetworkType.TestNet -> {
            InsightApi("https://testnet-insight.dash.org/insight-api")
        }
    }

    fun dashTransactions(fromUid: String? = null, type: TransactionFilterType? = null, limit: Int? = null): Single<List<DashTransactionInfo>> {
        return transactions(fromUid, type, limit).map {
            it.mapNotNull { it as? DashTransactionInfo }
        }
    }

    fun getDashTransaction(hash: String): DashTransactionInfo? {
        return getTransaction(hash) as? DashTransactionInfo
    }

    // BitcoinCore.Listener
    override fun onTransactionsUpdate(inserted: List<TransactionInfo>, updated: List<TransactionInfo>) {
        // check for all new transactions if it's has instant lock
        inserted.map { it.transactionHash.hexToByteArray().reversedArray() }.forEach {
            instantSend.handle(it)
        }

        listener?.onTransactionsUpdate(inserted.mapNotNull { it as? DashTransactionInfo }, updated.mapNotNull { it as? DashTransactionInfo })
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        listener?.onTransactionsDelete(hashes)
    }

    override fun onBalanceUpdate(balance: BalanceInfo) {
        listener?.onBalanceUpdate(balance)
    }

    override fun onLastBlockInfoUpdate(blockInfo: BlockInfo) {
        listener?.onLastBlockInfoUpdate(blockInfo)
    }

    override fun onKitStateUpdate(state: BitcoinCore.KitState) {
        listener?.onKitStateUpdate(state)
    }

    // IInstantTransactionDelegate
    override fun onUpdateInstant(transactionHash: ByteArray) {
        val transaction = dashStorage.getFullTransactionInfo(transactionHash) ?: return
        val transactionInfo = dashTransactionInfoConverter.transactionInfo(transaction)

        bitcoinCore.listenerExecutor.execute {
            listener?.onTransactionsUpdate(listOf(), listOf(transactionInfo))
        }
    }

    companion object {
        const val maxTargetBits: Long = 0x1e0fffff

        const val targetSpacing = 150             // 2.5 min. for mining 1 Block
        const val targetTimespan = 3600L          // 1 hour for 24 blocks
        const val heightInterval = targetTimespan / targetSpacing

        val defaultNetworkType: NetworkType = NetworkType.MainNet
        val defaultSyncMode: SyncMode = SyncMode.Api()
        const val defaultPeerSize: Int = 10
        const val defaultConfirmationsThreshold: Int = 6

        private fun getDatabaseNameCore(networkType: NetworkType, walletId: String, syncMode: SyncMode) =
            "${getDatabaseName(networkType, walletId, syncMode)}-core"

        private fun getDatabaseName(networkType: NetworkType, walletId: String, syncMode: SyncMode) =
            "Dash-${networkType.name}-$walletId-${syncMode.javaClass.simpleName}"

        private fun parseAddress(address: String, network: Network): Address {
            return Base58AddressConverter(network.addressVersion, network.addressScriptVersion).convert(address)
        }

        private fun network(networkType: NetworkType) = when (networkType) {
            NetworkType.MainNet -> MainNetDash()
            NetworkType.TestNet -> TestNetDash()
        }

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            for (syncMode in listOf(SyncMode.Api(), SyncMode.Full(), SyncMode.Blockchair(""))) {
                try {
                    SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseNameCore(networkType, walletId, syncMode)))
                    SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseName(networkType, walletId, syncMode)))
                } catch (ex: Exception) {
                    continue
                }
            }
        }
    }

}
