package com.crepetete.steamachievements.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*

/**
 * Created at 20 January, 2019.
 */
@Dao
abstract class BaseDao<T> {
    /**
     * Insert an object in the database.
     *
     * Fails on conflict so [upsert] can catch the exception.
     *
     * @param obj the object to be inserted.
     * @return the SQL row id.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    abstract fun insert(vararg obj: T)

    /**
     * Insert an array of objects in the database.
     *
     * Fails on conflict so [upsert] can catch the exception.
     *
     * @param obj the objects to be inserted.
     * @return the SQL row ids.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    abstract fun insert(obj: List<T>)

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated.
     */
    @Update
    abstract fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated.
     */
    @Update
    abstract fun update(obj: List<T>)

    /**
     * Delete an object from the database.
     *
     * @param obj the object to be deleted.
     */
    @Delete
    abstract fun delete(obj: T)

    @Transaction
    open fun upsert(obj: T) {
        try {
            insert(obj)
        } catch (exception: SQLiteConstraintException) {
            update(obj)
        }
    }

    @Transaction
    open fun upsert(objList: List<T>) {
        try {
            insert(objList)
        } catch (exception: SQLiteConstraintException) {
            update(objList)
        }
    }
}