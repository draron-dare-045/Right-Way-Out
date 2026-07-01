package com.example.rightway_out.di

import android.content.Context
import androidx.room.Room
import com.example.rightway_out.data.local.ShoppingItemDao
import com.example.rightway_out.data.local.StudentDao
import com.example.rightway_out.data.local.StudentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideStudentDatabase(@ApplicationContext context: Context): StudentDatabase {
        return Room.databaseBuilder(context, StudentDatabase::class.java, "rightwayout_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStudentDao(database: StudentDatabase): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideShoppingItemDao(database: StudentDatabase): ShoppingItemDao = database.shoppingItemDao()
}
