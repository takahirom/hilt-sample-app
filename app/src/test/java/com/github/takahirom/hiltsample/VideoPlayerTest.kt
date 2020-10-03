package com.github.takahirom.hiltsample

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class VideoPlayerTest {
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
        val inMemoryDatabaseBuilder = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            VideoDatabase::class.java
        )
        val videoPlayer = VideoPlayer(inMemoryDatabaseBuilder.build())

        videoPlayer.play()

        assertThat(videoPlayer.isPlaying, `is`(true))
    }

    @Test
    fun play() {
        videoPlayer.play()

        assertThat(videoPlayer.isPlaying, `is`(true))
    }
}