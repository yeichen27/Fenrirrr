package dev.ragnarok.fenrir.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.crypt.ExchangeMessage
import dev.ragnarok.fenrir.crypt.KeyExchangeService
import dev.ragnarok.fenrir.getParcelableExtraCompat
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.theme.ThemeOverlay
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils
import kotlin.math.max

class KeyExchangeCommitActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        @StyleRes val theme: Int = when (Settings.get().main().themeOverlay) {
            ThemeOverlay.AMOLED -> R.style.MyTransparentDialog_Amoled
            ThemeOverlay.MD1 -> R.style.MyTransparentDialog_MD1
            ThemeOverlay.OFF -> R.style.MyTransparentDialog
            else -> R.style.MyTransparentDialog
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setTranslucent(true)
        }
        window.setBackgroundDrawableResource(R.color.transparent)

        setContentView(R.layout.activity_key_exchange_commit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_root)) { v, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeFixedBottom =
                if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.ime()
                    ).bottom, insets.bottom
                ) else insets.bottom
            v.setPadding(
                insets.left, insets.top,
                insets.right,
                imeFixedBottom
            )
            WindowInsetsCompat.CONSUMED
        }
        val statusBarColor = Color.TRANSPARENT
        val navigationBarColor = Color.TRANSPARENT
        val invertIcons = !Settings.get().ui().isDarkModeEnabled(
            this
        )
        val statusBarStyle = if (invertIcons) SystemBarStyle.light(
            statusBarColor,
            statusBarColor
        ) else SystemBarStyle.dark(statusBarColor)
        val navigationBarStyle = if (invertIcons) SystemBarStyle.light(
            navigationBarColor,
            navigationBarColor
        ) else SystemBarStyle.dark(navigationBarColor)
        for (i in (window.decorView as ViewGroup)) {
            if (i is ProtectionLayout) {
                (window.decorView as ViewGroup).removeView(i)
            }
        }
        enableEdgeToEdge(statusBarStyle, navigationBarStyle)

        val accountId = (intent.extras ?: return).getLong(Extra.ACCOUNT_ID)
        val peerId = (intent.extras ?: return).getLong(Extra.PEER_ID)
        val user: User = intent.getParcelableExtraCompat(Extra.OWNER) ?: return
        val messageId = (intent.extras ?: return).getInt(Extra.MESSAGE_ID)
        val message: ExchangeMessage = intent.getParcelableExtraCompat(Extra.MESSAGE) ?: return
        val avatar = findViewById<ImageView>(R.id.avatar)
        ViewUtils.displayAvatar(
            avatar,
            CurrentTheme.createTransformationForAvatar(),
            user.maxSquareAvatar,
            null
        )
        val userName = findViewById<TextView>(R.id.user_name)
        userName.text = user.fullName
        findViewById<View>(R.id.accept_button).setOnClickListener {
            startService(
                KeyExchangeService.createIntentForApply(
                    this,
                    message,
                    accountId,
                    peerId,
                    messageId
                )
            )
            finish()
        }
        findViewById<View>(R.id.decline_button).setOnClickListener {
            startService(
                KeyExchangeService.createIntentForDecline(
                    this,
                    message,
                    accountId,
                    peerId,
                    messageId
                )
            )
            finish()
        }
    }

    companion object {

        fun createIntent(
            context: Context,
            accountId: Long,
            peerId: Long,
            user: User,
            messageId: Int,
            message: ExchangeMessage
        ): Intent {
            val intent = Intent(context, KeyExchangeCommitActivity::class.java)
            intent.putExtra(Extra.ACCOUNT_ID, accountId)
            intent.putExtra(Extra.OWNER, user)
            intent.putExtra(Extra.PEER_ID, peerId)
            intent.putExtra(Extra.MESSAGE_ID, messageId)
            intent.putExtra(Extra.MESSAGE, message)
            return intent
        }
    }
}
