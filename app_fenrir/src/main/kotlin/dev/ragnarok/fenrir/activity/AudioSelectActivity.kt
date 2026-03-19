package dev.ragnarok.fenrir.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.applyAlpha
import dev.ragnarok.fenrir.fragment.audio.AudioSelectTabsFragment
import dev.ragnarok.fenrir.fragment.audio.audios.AudiosFragment
import dev.ragnarok.fenrir.fragment.search.SearchFragmentFactory
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.Settings

class AudioSelectActivity : NoMainActivity(), PlaceProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val accountId = (intent.extras ?: return@run).getLong(Extra.ACCOUNT_ID)
            attachInitialFragment(accountId)
        }

        val statusBarColor = getStatusBarColor(this)
        val navigationBarColor = getNavigationBarColor(this)
        val invertIcons = !Settings.get().ui().isDarkModeEnabled(this)
        val statusBarStyle = if (invertIcons) SystemBarStyle.light(
            statusBarColor.applyAlpha(180),
            statusBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(statusBarColor.applyAlpha(180))
        val navigationBarStyle = if (invertIcons) SystemBarStyle.light(
            navigationBarColor.applyAlpha(180),
            navigationBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(navigationBarColor.applyAlpha(180))

        for (i in (window.decorView as ViewGroup)) {
            if (i is ProtectionLayout) {
                (window.decorView as ViewGroup).removeView(i)
            }
        }
        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
    }

    private fun attachInitialFragment(accountId: Long) {
        val fragment = AudioSelectTabsFragment.newInstance(accountId)
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
            .replace(noMainContainerViewId, fragment)
            .addToBackStack("audio-select")
            .commit()
    }

    override fun openPlace(place: Place) {
        if (place.type == Place.SINGLE_SEARCH) {
            val singleTabSearchFragment = SearchFragmentFactory.create(place.safeArguments())
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(noMainContainerViewId, singleTabSearchFragment)
                .addToBackStack("audio-search-select")
                .commit()
        } else if (place.type == Place.AUDIOS_IN_ALBUM) {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(
                    noMainContainerViewId,
                    AudiosFragment.newInstance(place.safeArguments(), true)
                )
                .addToBackStack("audio-in_playlist-select")
                .commit()
        }
    }

    companion object {
        /**
         * @param accountId От чьего имени получать
         */

        fun createIntent(context: Context, accountId: Long): Intent {
            return Intent(context, AudioSelectActivity::class.java)
                .putExtra(Extra.ACCOUNT_ID, accountId)
        }
    }
}