package com.crepetete.data.helper

import androidx.lifecycle.MutableLiveData
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_FAILED
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_LOADING
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_SUCCESS
import com.crepetete.steamachievements.data.helper.ResourceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

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
            fetchFromNetwork()
        }
    }

    /**
     * Fetch the data from network and persist into DB.
     */
    private suspend fun fetchFromNetwork() {
        try {
            createCall().enqueue(object : Callback<RequestType?> {
                override fun onFailure(call: Call<RequestType?>, t: Throwable) {
                    state.postValue(STATE_FAILED)
                    Timber.e("API Error: ${t.localizedMessage}")
                }

                override fun onResponse(
                    call: Call<RequestType?>,
                    response: Response<RequestType?>
                ) {
                    if (response.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            saveCallResult(response.body())
                        }
                        state.postValue(STATE_SUCCESS)
                    } else {
                        state.postValue(STATE_FAILED)
                        Timber.e("Failed HTTP request. Response was not successful. ${response.code()}")
                    }
                    job.complete()
                }

            })
        } catch (e: Exception) {
            state.postValue(STATE_FAILED)
            error.postValue(e)
            job.complete()
        }
    }

    /**
     * Returns an optional API result.
     */
    protected abstract suspend fun createCall(): Call<RequestType>

    /**
     * Called to save the result of the API response into the database.
     */
    protected abstract suspend fun saveCallResult(data: RequestType?)

    fun asLiveResource() =
        LiveResource(
            state,
            error,
            job
        )
}