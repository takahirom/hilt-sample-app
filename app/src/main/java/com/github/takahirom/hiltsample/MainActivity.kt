package com.github.takahirom.hiltsample

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.room.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, PlayerFragment())
            }
        }
    }
}

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragmenet_player) {
    private val videoPlayerViewModel: AbstractVideoPlayerViewModel by viewModels()

    val viewModelHilt: VideoPlayerHiltViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPlayerViewModel.play()

        view.findViewById<TextView>(R.id.content_text)
            ?.let {
                it.text = if (videoPlayerViewModel.isPlaying()) "playing" else "stopped"
            }

        viewModelHilt.play()
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
    @Inject
    lateinit var videoPlayer: VideoPlayer
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

@Module
@InstallIn(ViewModelComponent::class)
abstract class BindsModule {
    @Binds
    @IntoMap
    @StringKey("com.github.takahirom.hiltsample.AbstractVideoPlayerViewModel")
    @HiltViewModelMap
    abstract fun binds(viewModel: AbstractVideoPlayerViewModel): ViewModel
}


@Module
@InstallIn(ActivityRetainedComponent::class)
object KeyModule {
    @Provides
    @IntoSet
    @HiltViewModelMap.KeySet
    fun provide(): String {
        return "com.github.takahirom.hiltsample.AbstractVideoPlayerViewModel"
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class FactoryModule {
    @Provides
    fun provide(factory: VideoPlayerViewModel.Factory): AbstractVideoPlayerViewModel {
        return factory.create("?????")
    }
}

abstract class AbstractVideoPlayerViewModel : ViewModel() {
    abstract fun play()
    abstract fun isPlaying(): Boolean
}

class VideoPlayerViewModel @AssistedInject constructor(
    private val videoPlayer: VideoPlayer,
    @Assisted private val videoId: String
) : AbstractVideoPlayerViewModel() {
    override fun play() {
        videoPlayer.play()
    }

    override fun isPlaying(): Boolean {
        return videoPlayer.isPlaying
    }

    @AssistedFactory
    interface Factory {
        fun create(videoId: String): VideoPlayerViewModel
    }
}

@HiltViewModel
class VideoPlayerHiltViewModel @Inject constructor(val videoPlayer: VideoPlayer) : ViewModel() {
    fun play() {
        videoPlayer.play()
    }
}

class VideoPlayer @Inject constructor(
    private val database: VideoDatabase
) {
    var isPlaying = false

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
