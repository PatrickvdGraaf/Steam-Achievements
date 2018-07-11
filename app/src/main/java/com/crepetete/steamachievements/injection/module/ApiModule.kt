package com.crepetete.steamachievements.injection.module

import android.content.Context
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.utils.BASE_URL
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
object ApiModule {
    /**
     * Provides the Post service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideSteamApi(retrofit: Retrofit): SteamApiService {
        return retrofit.create(SteamApiService::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(moshiConverterFactory: MoshiConverterFactory,
                                          rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                          okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(moshiConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient)
                .build()
    }

    /**
     * Provides an OkHttpClient with some base values and an Interceptor for debugging purposes.
     * @return the OkHttpClient
     */
    @Provides
    @Singleton
    internal fun provideOkHttpClient(cache: Cache): OkHttpClient {
        val client = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
                })
        return client.build()
    }

    @Provides
    @Singleton
    fun providesOkHttpCache(context: Context): Cache {
        val cacheSize = 10L * 1024L * 1024L // 10MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(): MoshiConverterFactory {
        val customDateAdapter = object : Any() {
            @ToJson
            fun dateToJson(d: Date): Long {
                return d.time
            }

            @FromJson
            @Throws(ParseException::class)
            fun dateToJson(s: Long): Date {
                return Date(s)
            }
        }

        val moshi = Moshi.Builder()
                .add(customDateAdapter)
                .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
    }
}