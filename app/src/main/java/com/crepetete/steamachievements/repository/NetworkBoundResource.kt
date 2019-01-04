package com.crepetete.steamachievements.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.ApiEmptyResponse
import com.crepetete.steamachievements.api.ApiErrorResponse
import com.crepetete.steamachievements.api.ApiResponse
import com.crepetete.steamachievements.api.ApiSuccessResponse
import com.crepetete.steamachievements.vo.Resource
import timber.log.Timber

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * @param <ResultType>
 * @param <RequestType>
 * </RequestType></ResultType>
 */
abstract class NetworkBoundResource<ResultType, RequestType> @MainThread constructor(
    private val appExecutors: AppExecutors
) {
    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        @Suppress("LeakingThis")
        val dbSource = loadFromDb()

        // If the data in the original source changes, this observer gets notified
        result.addSource(dbSource) { data ->
            // When we receive data, remove the original source and handle the data.
            result.removeSource(dbSource)
            Timber.d("Removed source")
            // Check if the data should be refreshed with an API call.
            if (shouldFetch(data)) {
                Timber.d("Should fetch data")
                fetchFromNetwork(dbSource)
            } else {
                /* If we don't have to refresh the data, re-add the original DB source
                   and listen to DB changes from here on. */
                Timber.d("Should not fetch data")
                result.addSource(dbSource) { newData ->
                    Timber.d("Done")
                    setValue(Resource.success(newData))
                }
            }
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            setValue(Resource.loading(newData))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        saveCallResult(processResponse(response))
                        appExecutors.mainThread().execute {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    onFetchFailed()
                    result.addSource(dbSource) { newData ->
                        setValue(Resource.error(response.errorMessage
                            ?: "No error message available.", newData))
                    }
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    protected open fun onFetchFailed() {}

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body

    // Called to save the result of the API response into the database
    @WorkerThread
    abstract fun saveCallResult(item: RequestType)

    // Called with the data in the database to decide whether it should be
    // fetched from the network
    @MainThread
    abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>

    fun asLiveData() = result
}