package com.github.takahirom.hiltsample

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var videoPlayer: VideoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoPlayer.play()
    }
}

class NonHiltActivity : AppCompatActivity() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NonHiltActivityEntryPoint {
        fun videoPlayer(): VideoPlayer
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NonHiltActivityEntryPoint::class.java
        )
        val videoPlayer = entryPoint.videoPlayer()
        videoPlayer.play()
    }
}

@Subcomponent
interface JustDaggerComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): JustDaggerComponent
    }

    fun inject(justDaggerActivity: JustDaggerActivity)
}

@InstallIn(SingletonComponent::class)
@EntryPoint
interface JustDaggerEntryPoint {
    fun activityComponentFactory(): JustDaggerComponent.Factory
}

class JustDaggerActivity : AppCompatActivity() {
    @Inject lateinit var videoPlayer: VideoPlayer
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        // old: appComponent.justDaggerComponent().inject(this)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            JustDaggerEntryPoint::class.java
        )
        entryPoint.activityComponentFactory().create().inject(this)

        videoPlayer.play()
    }
}

class VideoPlayer @Inject constructor(
    private val database: VideoDatabase
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

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideVideoDB(@ApplicationContext context: Context): VideoDatabase {
        return Room
            .databaseBuilder(
                context,
                VideoDatabase::class.java,
                "database"
            )
            .createFromAsset("videos.db")
            .build()
    }
}
