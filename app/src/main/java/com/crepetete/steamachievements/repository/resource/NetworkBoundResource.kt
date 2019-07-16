package com.crepetete.steamachievements.repository.resource

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.response.ApiEmptyResponse
import com.crepetete.steamachievements.api.response.ApiErrorResponse
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.vo.Resource

/**
 * NetworkBoundResource is a plain class which does the job of data flow between
 * local database and your web-service using the power of MediatorLiveData and the
 * Resource class to send state and data upstream to the Activity or Fragment.
 *
 * https://cdn-images-1.medium.com/max/1600/1*-yY0l4XD3kLcZz0rO1sfRA.png
 *
 * 1. Initially it sends event for loading
 * 2. Fetches the data from local database (Note: It can be empty too)
 * 3. Checks the condition in “shouldFetch”
 * 4. If false, then sends the data upstream from local database
 * 5. If true, it performs the required network call and fetch the data
 * 6. On response, the data is then saved into local database and this data and
 *    event is sent upstream
 *
 * You can read more about it in the [Architecture Guide](https://developer.android.com/arch).
 * @param <ResultType>
 * @param <RequestType>
 * </RequestType></ResultType>
 */
abstract class NetworkBoundResource<ResultType, RequestType> @MainThread constructor(
    private val appExecutors: AppExecutors
) {

    /**
     * The final result LiveData
     */
    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        /* Send loading state to UI */
        result.value = Resource.loading()
        val dbSource = this.loadFromDb()

        /* If the data in the original source changes, this observer gets notified */
        result.addSource(dbSource) { data ->
            /* When we receive data, remove the original source and handle the data. */
            result.removeSource(dbSource)
            /* Check if the data should be refreshed with an API call. */
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource)
            } else {
                /* If we don't have to refresh the data, re-add the original DB source
                   and listen to DB changes from here on. */
                result.addSource(dbSource) { newData -> setValue(Resource.success(newData)) }
            }
        }
    }

    /**
     * Fetch the data from network and persist into DB and then
     * send it back to UI.
     */
    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        /* We re-attach dbSource as a new source, it will dispatch its latest value quickly */
        result.addSource(dbSource) { setValue(Resource.loading()) }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)

            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        val requestType = processResponse(response)
                        if (requestType != null) {
                            saveCallResult(requestType)
                        }
                        appExecutors.mainThread().execute {
                            /* We specially request a new live data,
                               otherwise we will get immediately last cached value,
                               which may not be updated with latest results received from network. */
                            result.addSource(loadFromDb()) { newData -> setValue(Resource.success(newData)) }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        /* Reload from disk whatever we had. */
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    onFetchFailed()
                    result.addSource(dbSource) { newData ->
                        result.setValue(Resource.error(response.errorMessage ?: "No error message available.", newData))
                    }
                }
                else -> onFetchFailed()
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) result.value = newValue
    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>): RequestType? = response.body

    /* Called to save the result of the API response into the database. */
    @WorkerThread
    abstract fun saveCallResult(item: RequestType)

    /* Called with the data in the database to decide whether it should be fetched from the network. */
    @MainThread
    abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}