package com.crepetete.data.helper

import androidx.lifecycle.MutableLiveData
import com.crepetete.data.helper.LiveResource.Companion.STATE_FAILED
import com.crepetete.data.helper.LiveResource.Companion.STATE_LOADING
import com.crepetete.data.helper.LiveResource.Companion.STATE_SUCCESS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
 *
 * Updated to coroutines with https://medium.com/ideas-by-idean/android-adventure-512bbd78b05f.
 */
abstract class NetworkBoundResource<ResultType, RequestType> {

    // The final result LiveData.
    private val result = MutableLiveData<ResultType>()

    // Loading state of the job.
    private val state = MutableLiveData<@ResourceState Int?>()

    // Optional error for when the API call fails.
    private val error = MutableLiveData<Exception?>()

    private val job = Job()

    /**
     * In the init block, an instance of CoroutineScope is created and launched in the IO context
     * as background task to get the data from Network and/or local database.
     */
    init {
        val ioDispatcher = Dispatchers.IO
        ioDispatcher + job

        CoroutineScope(ioDispatcher).launch {
            state.postValue(STATE_LOADING)

            val dbSource = loadFromDb()

            if (shouldFetch(dbSource)) {
                fetchFromNetwork(dbSource)
            } else {
                state.postValue(STATE_SUCCESS)
                result.postValue(dbSource)
            }
        }
    }

    /**
     * Fetch the data from network and persist into DB and then
     * send it back to UI.
     */
    private suspend fun fetchFromNetwork(dbSource: ResultType?) {
        assert(state.value == STATE_LOADING)
        dbSource?.let(result::postValue)

        try {
            createCall()?.let { apiResponse ->
                saveCallResult(apiResponse)
                result.postValue(loadFromDb())
            } ?: run {
                if (dbSource == null)
                    result.postValue(null)
            }

            state.postValue(STATE_SUCCESS)
        } catch (e: Exception) {
            assert(result.value == dbSource)
            state.postValue(STATE_FAILED)
            error.postValue(e)
        }
    }

    /**
     * Returns an optional result from the Room Database.
     */
    protected abstract suspend fun loadFromDb(): ResultType?

    /**
     * Called after fetching data in the database to decide whether it should be updated from the
     * network.
     */
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    /**
     * Returns an optional API result.
     */
    protected abstract suspend fun createCall(): RequestType?

    /**
     * Called to save the result of the API response into the database.
     */
    protected abstract suspend fun saveCallResult(data: RequestType)

    fun asLiveResource() =
        LiveResource(
            result,
            state,
            error,
            job
        )
}