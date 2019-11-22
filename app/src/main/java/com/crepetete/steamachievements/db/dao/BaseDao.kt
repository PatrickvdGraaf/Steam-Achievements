package com.crepetete.steamachievements.db.dao

import androidx.room.*

/**
 *
 * Doa superclass that alloes children to inherit all the base functionality a
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 01 Feb, 2019; 17:13.
 */
@Dao
abstract class BaseDao<T> {
    /**
     * Insert an object in the database.
     * Ignores on conflict so [upsert] can can automatically update the value instead.
     *
     * @param obj the object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long

    /**
     * Insert an array of objects in the database.
     * Ignores on conflict so [upsert] can can automatically update the value instead.
     *
     * @param objList the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(objList: List<T>): List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated.
     */
    @Update
    abstract suspend fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param objList the object to be updated.
     */
    @Update
    abstract suspend fun update(objList: List<T>)

    /**
     * Delete an object from the database.
     *
     * @param obj the object to be deleted.
     */
    @Delete
    abstract suspend fun delete(obj: T)

    /**
     * Update an object from the database if it exists, otherwise insert it.
     *
     * @param obj the object to be 'up-serted'.
     */
    @Transaction
    open suspend fun upsert(obj: T) {
        val id = insert(obj)
        if (id == -1L) {
            update(obj)
        }
    }

    /**
     * Update an array of objects from the database if it exists, otherwise insert it.
     *
     * @param objList the object to be 'up-serted'.
     */
    @Transaction
    open suspend fun upsert(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}