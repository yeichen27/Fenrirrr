package dev.ragnarok.filegallery.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.filegallery.Extra
import dev.ragnarok.filegallery.activity.slidr.Slidr
import dev.ragnarok.filegallery.activity.slidr.model.SlidrConfig
import dev.ragnarok.filegallery.applyAlpha
import dev.ragnarok.filegallery.fragment.filemanagerselect.FileManagerSelectFragment
import dev.ragnarok.filegallery.settings.CurrentTheme
import dev.ragnarok.filegallery.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.filegallery.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.filegallery.settings.Settings

class FileManagerSelectActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Slidr.attach(
            this,
            SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this)).build()
        )
        if (savedInstanceState == null) {
            attachFragment()
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

    private fun attachFragment() {
        val args = Bundle()
        args.putString(Extra.PATH, intent.extras?.getString(Extra.PATH))
        args.putString(Extra.EXT, intent.extras?.getString(Extra.EXT))
        if (intent.extras?.containsKey(Extra.TITLE) == true) {
            args.putString(Extra.TITLE, intent.extras?.getString(Extra.TITLE))
        }
        val fileManagerFragment = FileManagerSelectFragment()
        fileManagerFragment.arguments = args
        supportFragmentManager
            .beginTransaction()
            .replace(noMainContainerViewId, fileManagerFragment)
            .commit()
    }

    companion object {
        fun makeFileManager(context: Context, path: String, ext: String?, header: String?): Intent {
            val intent = Intent(context, FileManagerSelectActivity::class.java)
            intent.putExtra(Extra.PATH, path)
            intent.putExtra(Extra.EXT, ext)
            header?.let {
                intent.putExtra(Extra.TITLE, it)
            }
            return intent
        }
    }
}
