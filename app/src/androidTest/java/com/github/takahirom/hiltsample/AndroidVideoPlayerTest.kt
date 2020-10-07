package com.github.takahirom.hiltsample

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(DataModule::class)
class AndroidVideoPlayerTest {
    @InstallIn(SingletonComponent::class)
    @Module
    class TestDataModule {
        @Provides
        fun provideVideoDatabase(): VideoDatabase {
            return Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                VideoDatabase::class.java
            ).build()
        }
    }

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var videoPlayer: VideoPlayer

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun normalTest() {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            VideoDatabase::class.java
        ).build()
        val videoPlayer = VideoPlayer(database)

        videoPlayer.play()

        assertThat(videoPlayer.isPlaying, `is`(true))
    }

    @Test
    fun play() {
        videoPlayer.play()

        assertThat(videoPlayer.isPlaying, `is`(true))
    }
}