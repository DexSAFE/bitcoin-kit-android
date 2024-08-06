package io.dexnet.bitcoincore.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.dexnet.bitcoincore.models.BlockHashPublicKey

@Dao
interface BlockHashPublicKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<BlockHashPublicKey>)

}
