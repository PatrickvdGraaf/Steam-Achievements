package com.crepetete.steamachievements.data.api.helper

import com.crepetete.steamachievements.data.helper.Resource
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date

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
abstract class NetworkBoundResource<T> {

    private val job = Job()

    private val ioDispatcher = Dispatchers.IO

    /**
     * In the init block, an instance of CoroutineScope is created and launched in the IO context
     * as background task to get the data from Network and/or local database.
     */
    init {
        ioDispatcher + job
    }

    @FlowPreview
    fun asFlow(): Flow<Resource<T>> {
        return flow {
            CoroutineScope(ioDispatcher).launch {
                val dbValue = loadFromDb()
                val dateFetched = getDataFetchDate(dbValue)

                if (shouldFetch(dbValue, dateFetched)) {
                    if (shouldLogin()) {
                        autoReAuthenticate()
                        emit(Resource.reAuthenticate())
                    }

                    emit(Resource.loading(dbValue))
//                loadFromNetwork(dbValue, dateFetched, this)

                    if (zipRequests().isEmpty()) {
                        val data = fetchFromNetwork()
                        emit(Resource.success(data, dateFetched))
                    } else {
                        Observable
                            .zip(zipRequests()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    emit(Resource.success(it, dateFetched))
                                }
                            }
                            // Will be triggered if all requests will end successfully (4xx and 5xx also are successful requests too)
                            .subscribe({
                                //Do something on successful completion of all requests
                            }) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    //Do something on error completion of requests
                                    emit(error(it.localizedMessage))
                                    job.complete()
                                }
                            }
                    }
                } else {
                    emit(Resource.success(dbValue, dateFetched))
                    job.complete()
                }
            }
        }
    }
//
//    private suspend fun loadFromNetwork(
//        data: T?,
//        dataFetchDate: Date?,
//        flowCollector: FlowCollector<Resource<T>>
//    ) {
//        fetchFromNetwork().enqueue(object : Callback<RequestType?> {
//            override fun onFailure(call: Call<RequestType?>, t: Throwable) {
//                CoroutineScope(ioDispatcher).launch {
//                    // Post error with cached data.
//                    flowCollector.emit(
//                        Resource.error(
//                            "Error while making API request.",
//                            data,
//                            dataFetchDate
//                        )
//                    )
//                }
//                job.complete()
//                Timber.e("API Error: ${t.localizedMessage}")
//            }
//
//            override fun onResponse(call: Call<RequestType?>, response: Response<RequestType?>) {
//                if (response.isSuccessful) {
//                    CoroutineScope(ioDispatcher).launch {
//                        val dataBody = response.body()
//
//                        // Post result
//                        flowCollector.emit(
//                            Resource.success(
//                                convertApiResult(dataBody),
//                                dataFetchDate
//                            )
//                        )
//
//                        // Save result in DB
//                        saveCallResult(dataBody)
//
//                        job.complete()
//                    }
//                } else {
//                    CoroutineScope(ioDispatcher).launch {
//                        // Post cached Data
//                        flowCollector.emit(Resource.cached(data, dataFetchDate))
//                        job.complete()
//                    }
//                    Timber.e("API Error: Failed HTTP request (${response.code()}).")
//                }
//            }
//        })
//    }

    // Called to get the cached data from the database
    protected abstract suspend fun loadFromDb(): T

    // Called with the data in the database to determine when it was fetched
    // from the network
    protected abstract suspend fun getDataFetchDate(data: T?): Date?

    // Called with the data in the database to decide whether it should be
    // fetched from the network
    protected abstract suspend fun shouldFetch(data: T?, dataFetchDate: Date?): Boolean

    /**
     * Returns an optional API result.
     */
    protected abstract suspend fun fetchFromNetwork(): T?

    /**
     * Called to save the result of the API response into the database.
     */
    protected abstract suspend fun saveCallResult(data: T?)

    /**
     * Here we can implement logic if we worry about logging out.
     * Returns whether the user is allowed to make a certain call.
     *
     * For this app we probably don't need it, as we only need to authenticate once to get the users
     * appId. So it returns true for now.
     *
     * This code serves as boilerplate if I want to use it in another app one day.
     */
    private fun shouldLogin(): Boolean {
        return true
    }

    /**
     * Here we can implement logic if we worry about logging out.
     * Returns whether the re-authentication was successful and the call may proceed.
     *
     * For this app we probably don't need it, as we only need to authenticate once to get the users
     * appId. So it returns true for now.
     *
     * This code serves as boilerplate if I want to use it in another app one day.
     */
    private fun autoReAuthenticate(): Boolean {
        return true
    }
}