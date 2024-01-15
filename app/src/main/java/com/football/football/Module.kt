package com.football.football

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideRetrofitClient() = RetrofitClient()

    @Provides
    @Singleton
    fun provideService(retrofitClient: RetrofitClient) = retrofitClient.provideService()

    @Provides
    @Singleton
    fun providePreferencesHelper(@ApplicationContext context: Context) = Preferences(context)
}
