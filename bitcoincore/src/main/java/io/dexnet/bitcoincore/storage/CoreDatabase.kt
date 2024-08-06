package io.dexnet.bitcoincore.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import android.content.Context
import io.dexnet.bitcoincore.models.*
import io.dexnet.bitcoincore.storage.migrations.*

@Database(version = 17, exportSchema = false, entities = [
    BlockchainState::class,
    PeerAddress::class,
    BlockHash::class,
    BlockHashPublicKey::class,
    Block::class,
    SentTransaction::class,
    Transaction::class,
    TransactionInput::class,
    TransactionOutput::class,
    PublicKey::class,
    InvalidTransaction::class,
    TransactionMetadata::class
])
@TypeConverters(
    ScriptTypeConverter::class,
    TransactionTypeConverter::class
)
abstract class CoreDatabase : RoomDatabase() {

    abstract val blockchainState: io.dexnet.bitcoincore.storage.BlockchainStateDao
    abstract val peerAddress: io.dexnet.bitcoincore.storage.PeerAddressDao
    abstract val blockHash: io.dexnet.bitcoincore.storage.BlockHashDao
    abstract val blockHashPublicKey: io.dexnet.bitcoincore.storage.BlockHashPublicKeyDao
    abstract val block: io.dexnet.bitcoincore.storage.BlockDao
    abstract val sentTransaction: io.dexnet.bitcoincore.storage.SentTransactionDao
    abstract val transaction: io.dexnet.bitcoincore.storage.TransactionDao
    abstract val transactionMetadata: io.dexnet.bitcoincore.storage.TransactionMetadataDao
    abstract val input: io.dexnet.bitcoincore.storage.TransactionInputDao
    abstract val output: io.dexnet.bitcoincore.storage.TransactionOutputDao
    abstract val publicKey: io.dexnet.bitcoincore.storage.PublicKeyDao
    abstract val invalidTransaction: io.dexnet.bitcoincore.storage.InvalidTransactionDao

    companion object {

        fun getInstance(context: Context, dbName: String): io.dexnet.bitcoincore.storage.CoreDatabase {
            return Room.databaseBuilder(context, io.dexnet.bitcoincore.storage.CoreDatabase::class.java, dbName)
                    .allowMainThreadQueries()
                    .addMigrations(
                            Migration_16_17,
                            Migration_15_16,
                            Migration_14_15,
                            Migration_13_14,
                            Migration_12_13,
                            Migration_11_12,
                            Migration_10_11,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.add_rawTransaction_to_Transaction,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.add_conflictingTxHash_to_Transaction,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.add_table_InvalidTransaction,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.update_transaction_output,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.update_block_timestamp,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.add_hasTransaction_to_Block,
                        io.dexnet.bitcoincore.storage.CoreDatabase.Companion.add_connectionTime_to_PeerAddress
                    )
                    .fallbackToDestructiveMigration()
                    .build()
        }

        private val add_rawTransaction_to_Transaction = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Transaction` ADD COLUMN `rawTransaction` TEXT")
                database.execSQL("ALTER TABLE `InvalidTransaction` ADD COLUMN `rawTransaction` TEXT")
            }
        }

        private val add_conflictingTxHash_to_Transaction = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Transaction` ADD COLUMN `conflictingTxHash` BLOB")
                database.execSQL("ALTER TABLE `InvalidTransaction` ADD COLUMN `conflictingTxHash` BLOB")
            }
        }

        private val add_table_InvalidTransaction = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `InvalidTransaction` (`hash` BLOB NOT NULL, `blockHash` BLOB, `version` INTEGER NOT NULL, `lockTime` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `order` INTEGER NOT NULL, `isMine` INTEGER NOT NULL, `isOutgoing` INTEGER NOT NULL, `segwit` INTEGER NOT NULL, `status` INTEGER NOT NULL, `serializedTxInfo` TEXT NOT NULL, PRIMARY KEY(`hash`))")
                database.execSQL("ALTER TABLE SentTransaction ADD COLUMN sendSuccess INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE `Transaction` ADD COLUMN serializedTxInfo TEXT DEFAULT '' NOT NULL")
            }
        }

        private val update_transaction_output = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TransactionOutput ADD COLUMN `pluginId` INTEGER")
                database.execSQL("ALTER TABLE TransactionOutput ADD COLUMN `pluginData` TEXT")
            }
        }

        private val update_block_timestamp = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE Block SET block_timestamp = 1559256184 WHERE height = 578592 AND block_timestamp = 1559277784")
            }
        }

        private val add_hasTransaction_to_Block = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Block ADD COLUMN hasTransactions INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("UPDATE Block SET hasTransactions = 1")
            }
        }

        private val add_connectionTime_to_PeerAddress = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE PeerAddress ADD COLUMN connectionTime INTEGER")
            }
        }
    }
}
