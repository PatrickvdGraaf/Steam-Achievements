package com.crepetete.steamachievements.di.submodules

import android.content.Context
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.util.LiveDataCallAdapterFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.Module
import dagger.Provides
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
class ApiModule {

    @Provides
    @Singleton
    fun provideRetrofitInterface(
        moshiConverterFactory: MoshiConverterFactory,
        rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
        liveDataCallAdapterFactory: LiveDataCallAdapterFactory,
        okHttpClient: OkHttpClient
    ): SteamApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .addConverterFactory(moshiConverterFactory)
        .addCallAdapterFactory(rxJava2CallAdapterFactory)
        .addCallAdapterFactory(liveDataCallAdapterFactory)
        .client(okHttpClient)
        .build()
        .create(SteamApiService::class.java)

    @Provides
    @Singleton
    fun provideLiveDataCallAdapter() = LiveDataCallAdapterFactory()

    /**
     * Provides an OkHttpClient with some base values and an Interceptor for debugging purposes.
     * @return the OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides
    @Singleton
    fun providesOkHttpCache(context: Context): Cache {
        return Cache(context.cacheDir, (10L * 1024L * 1024L)) // 10MB
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