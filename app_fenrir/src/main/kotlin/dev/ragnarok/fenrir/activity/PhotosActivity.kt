package dev.ragnarok.fenrir.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.applyAlpha
import dev.ragnarok.fenrir.fragment.photos.localimagealbums.LocalImageAlbumsFragment
import dev.ragnarok.fenrir.fragment.photos.localphotos.LocalPhotosFragment
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.model.LocalImageAlbum
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.Settings

class PhotosActivity : NoMainActivity(), PlaceProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            attachAlbumsFragment()
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

    private fun attachAlbumsFragment() {
        val ignoredFragment = LocalImageAlbumsFragment()
        ignoredFragment.arguments = intent.extras
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
            .replace(noMainContainerViewId, ignoredFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun openPlace(place: Place) {
        if (place.type == Place.LOCAL_IMAGE_ALBUM) {
            val maxSelectionCount = intent.getIntExtra(EXTRA_MAX_SELECTION_COUNT, 10)
            val album: LocalImageAlbum? = place.safeArguments().getParcelableCompat(Extra.ALBUM)
            val localPhotosFragment =
                LocalPhotosFragment.newInstance(maxSelectionCount, album, false)
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(noMainContainerViewId, localPhotosFragment)
                .addToBackStack("photos")
                .commit()
        } else if (place.type == Place.SINGLE_PHOTO) {
            place.launchActivityForResult(
                this,
                SinglePhotoActivity.newInstance(this, place.safeArguments())
            )
        }
    }

    companion object {
        const val EXTRA_MAX_SELECTION_COUNT = "max_selection_count"
    }
}