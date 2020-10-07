package com.github.takahirom.hiltsample

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@UninstallModules(DataModule::class)
@RunWith(RobolectricTestRunner::class)
class PlayerFragmentTest {
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

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun play() {
        launchFragmentInHiltContainerInInstrumentation<PlayerFragment> {
            onView(withText("playing")).check(matches(isDisplayed()))
        }
    }
}

inline fun <reified T : Fragment> launchFragmentInHiltContainerInInstrumentation(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    crossinline action: Fragment.() -> Unit = {}
) {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, themeResId)

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        fragment.action()
    }
}