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
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 21 Jun, 2020; 23:39.
 */
abstract class CombinedNetworkBoundResource<ResultType, RequestType> {
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
    fun asFlow(): Flow<Resource<ResultType>> {
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
                            //Do something on error completion of requests
                            emit(error(it.localizedMessage))
                            job.complete()
                        }
                } else
            }
        }
    }

    // Called to get the cached data from the database
    protected abstract suspend fun loadFromDb(): ResultType

    // Called with the data in the database to determine when it was fetched
    // from the network
    protected abstract suspend fun getDataFetchDate(data: ResultType?): Date?

    // Called with the data in the database to decide whether it should be
    // fetched from the network
    protected abstract suspend fun shouldFetch(
        data: ResultType?,
        dataFetchDate: Date?
    ): Boolean

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

    open fun zipRequests(): List<Observable<ResultType>> = listOf()

    open fun makeZipRequestAndSave() {}

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

    /**
     * Called to save the result of the API response into the database.
     */
    protected abstract suspend fun saveCallResult(data: ResultType?)
}