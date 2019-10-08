package com.crepetete.steamachievements.db.dao

import androidx.room.*

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 01 Feb, 2019; 17:13.
 */
@Dao
abstract class BaseDao<T> {
    /**
     * Insert an object in the database.
     *
     * Fails on conflict so [upsert] can catch the exception.
     *
     * @param obj the object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(obj: T): Long

    /**
     * Insert an array of objects in the database.
     *
     * Fails on conflict so [upsert] can catch the exception.
     *
     * @param objList the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(objList: List<T>): List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param objList the object to be updated.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
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
        insert(obj)
        update(obj)
    }

    /**
     * Update an array of objects from the database if it exists, otherwise insert it.
     *
     * @param objList the object to be 'up-serted'.
     */
    @Transaction
    open suspend fun upsert(objList: List<T>) {
        insert(objList)
        update(objList)
    }


}