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
import dev.ragnarok.fenrir.fragment.photos.vkphotoalbums.VKPhotoAlbumsFragment
import dev.ragnarok.fenrir.fragment.photos.vkphotos.VKPhotosFragment
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.Settings

class PhotoAlbumsActivity : NoMainActivity(), PlaceProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = intent
            val accountId = (intent.extras ?: return).getLong(Extra.ACCOUNT_ID)
            val ownerId = (intent.extras ?: return).getLong(Extra.OWNER_ID)
            val action = intent.getStringExtra(Extra.ACTION)
            val fragment =
                VKPhotoAlbumsFragment.newInstance(accountId, ownerId, action, null, false)
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .add(noMainContainerViewId, fragment)
                .addToBackStack(null)
                .commit()
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

    override fun openPlace(place: Place) {
        if (place.type == Place.VK_PHOTO_ALBUM) {
            val fragment = VKPhotosFragment.newInstance(place.safeArguments())
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(noMainContainerViewId, fragment)
                .addToBackStack("photos")
                .commit()
        }
    }
}