package com.github.takahirom.hiltsample

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val videoDatabase = Room
            .databaseBuilder(
                App.instance,
                VideoDatabase::class.java,
                "database"
            )
            .createFromAsset("videos.db")
            .build()
        val codecs = listOf(FMP4, WebM, MPEG_TS, AV1)
        val videoPlayer = VideoPlayer(videoDatabase, codecs)
        videoPlayer.play()
    }
}

class VideoPlayer(
    private val database: VideoDatabase,
    private val codecs: List<Codec>
) {
    private var isPlaying = false

    fun play() {
        isPlaying = true
//        video?.url
//        codecs.forEach {
//        }
        // ...
    }
}

interface Codec
object FMP4 : Codec
object WebM : Codec
object MPEG_TS : Codec
object AV1 : Codec

@Database(entities = arrayOf(Video::class), version = 1)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM video")
    fun getAll(): List<Video>
}


@Entity
data class Video(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "manifestUrl") val url: String?
)