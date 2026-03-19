package dev.ragnarok.filegallery.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.filegallery.applyAlpha
import dev.ragnarok.filegallery.fragment.pin.createpin.CreatePinFragment
import dev.ragnarok.filegallery.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.filegallery.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.filegallery.settings.Settings

class CreatePinActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(noMainContainerViewId, CreatePinFragment.newInstance())
                .commit()
        }

        val statusBarColor = getStatusBarColor(this)
        val navigationBarColor = getNavigationBarColor(this)
        val invertIcons = !Settings.get().main().isDarkModeEnabled(this)
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
}