package com.formautomation.data.database

import androidx.room.*

@Dao
interface DatasetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DatasetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<DataRowEntity>)

    @Query("SELECT * FROM datasets WHERE id = :id")
    suspend fun getDataset(id: Long): DatasetEntity?

    @Query("SELECT * FROM datasets ORDER BY imported_at DESC")
    suspend fun getAllDatasets(): List<DatasetEntity>

    @Query("SELECT * FROM data_rows WHERE dataset_id = :datasetId ORDER BY row_index ASC")
    suspend fun getDatasetRows(datasetId: Long): List<DataRowEntity>

    @Query("UPDATE data_rows SET status = :status, updated_at = :updatedAt WHERE localId = :rowId")
    suspend fun updateRowStatus(rowId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM datasets WHERE id = :datasetId")
    suspend fun deleteDataset(datasetId: Long)

    @Query("DELETE FROM data_rows WHERE dataset_id = :datasetId")
    suspend fun deleteDatasetRows(datasetId: Long)
}
