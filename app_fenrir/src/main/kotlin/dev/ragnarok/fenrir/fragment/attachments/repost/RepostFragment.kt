package dev.ragnarok.fenrir.fragment.attachments.repost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.fragment.attachments.absattachmentsedit.AbsAttachmentsEditFragment
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.model.Post
import kotlin.math.max

class RepostFragment : AbsAttachmentsEditFragment<RepostPresenter, IRepostView>(), IRepostView,
    MenuProvider {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeFixedBottom =
                if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.ime()
                    ).bottom, insets.bottom
                ) else insets.bottom
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            root.setPadding(0, 0, 0, imeFixedBottom)
            WindowInsetsCompat.CONSUMED
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onResult() {
        presenter?.fireReadyClick()
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.share)
            actionBar.subtitle = null
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): RepostPresenter {
        val post: Post = requireArguments().getParcelableCompat(EXTRA_POST)!!
        val groupId =
            if (requireArguments().containsKey(EXTRA_GROUP_ID)) requireArguments().getLong(
                EXTRA_GROUP_ID
            ) else null
        val accountId = requireArguments().getLong(Extra.ACCOUNT_ID)
        return RepostPresenter(accountId, post, groupId, saveInstanceState)
    }

    companion object {
        private const val EXTRA_POST = "post"
        private const val EXTRA_GROUP_ID = "group_id"
        fun newInstance(args: Bundle?): RepostFragment {
            val fragment = RepostFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(accountId: Long, gid: Long?, post: Post?): RepostFragment {
            val fragment = RepostFragment()
            fragment.arguments = buildArgs(accountId, gid, post)
            return fragment
        }

        fun buildArgs(accountId: Long, groupId: Long?, post: Post?): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_POST, post)
            bundle.putLong(Extra.ACCOUNT_ID, accountId)
            if (groupId != null) {
                bundle.putLong(EXTRA_GROUP_ID, groupId)
            }
            return bundle
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_attchments, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.ready) {
            presenter?.fireReadyClick()
            return true
        }
        return false
    }
}