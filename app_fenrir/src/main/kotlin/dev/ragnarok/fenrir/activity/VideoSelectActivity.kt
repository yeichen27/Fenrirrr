package dev.ragnarok.fenrir.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.applyAlpha
import dev.ragnarok.fenrir.fragment.search.SearchFragmentFactory
import dev.ragnarok.fenrir.fragment.videos.IVideosListView
import dev.ragnarok.fenrir.fragment.videos.VideosFragment
import dev.ragnarok.fenrir.fragment.videos.VideosTabsFragment
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils

class VideoSelectActivity : NoMainActivity(), PlaceProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val accountId = (intent.extras ?: return).getLong(Extra.ACCOUNT_ID)
            val ownerId = (intent.extras ?: return).getLong(Extra.OWNER_ID)
            attachInitialFragment(accountId, ownerId)
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

    private fun attachInitialFragment(accountId: Long, ownerId: Long) {
        val fragment =
            VideosTabsFragment.newInstance(accountId, ownerId, IVideosListView.ACTION_SELECT)
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
            .replace(noMainContainerViewId, fragment)
            .addToBackStack("video-tabs")
            .commit()
    }

    override fun openPlace(place: Place) {
        when (place.type) {
            Place.VIDEO_ALBUM -> {
                val fragment: Fragment = VideosFragment.newInstance(place.safeArguments())
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(noMainContainerViewId, fragment)
                    .addToBackStack("video-album")
                    .commit()
            }

            Place.SINGLE_SEARCH -> {
                val singleTabSearchFragment =
                    SearchFragmentFactory.create(place.safeArguments())
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(noMainContainerViewId, singleTabSearchFragment)
                    .addToBackStack("video-search")
                    .commit()
            }

            Place.VIDEO_PREVIEW -> {
                setResult(
                    RESULT_OK, Intent().putParcelableArrayListExtra(
                        Extra.ATTACHMENTS, Utils.singletonArrayList(
                            place.safeArguments().getParcelableCompat(
                                Extra.VIDEO
                            )
                        )
                    )
                )
                finish()
            }
        }
    }

    companion object {
        /**
         * @param accountId От чьего имени получать
         * @param ownerId   Чьи получать
         */

        fun createIntent(context: Context, accountId: Long, ownerId: Long): Intent {
            return Intent(context, VideoSelectActivity::class.java)
                .putExtra(Extra.ACCOUNT_ID, accountId)
                .putExtra(Extra.OWNER_ID, ownerId)
        }
    }
}