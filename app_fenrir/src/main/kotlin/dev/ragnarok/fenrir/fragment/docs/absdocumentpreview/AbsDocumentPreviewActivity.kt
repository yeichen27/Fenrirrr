package dev.ragnarok.fenrir.fragment.docs.absdocumentpreview

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.BaseMvpActivity
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.dialog.PostShareDialog.Methods
import dev.ragnarok.fenrir.fragment.base.MenuAdapter
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.EditingPostType
import dev.ragnarok.fenrir.model.Text
import dev.ragnarok.fenrir.model.menu.Item
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostCreation
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.Utils.shareLink

abstract class AbsDocumentPreviewActivity<P : BaseDocumentPresenter<V>, V : IBasicDocumentView> :
    BaseMvpActivity<P, V>(), IBasicDocumentView {

    abstract val requestWritePermission: AppPerms.DoRequestPermissions

    override fun requestWriteExternalStoragePermission() {
        requestWritePermission.launch()
    }

    override fun shareDocument(accountId: Long, document: Document) {
        val items: MutableList<Item> = ArrayList()
        items.add(Item(Methods.SHARE_LINK, Text(R.string.share_link)).setIcon(R.drawable.web))
        items.add(
            Item(
                Methods.SEND_MESSAGE,
                Text(R.string.repost_send_message)
            ).setIcon(R.drawable.share)
        )
        items.add(
            Item(
                Methods.REPOST_YOURSELF,
                Text(R.string.repost_to_wall)
            ).setIcon(R.drawable.ic_outline_share)
        )

        val mAdapter = MenuAdapter(this, items, true)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.repost_document_title)
            .setAdapter(mAdapter) { _, which ->
                when (items[which].key) {
                    Methods.SHARE_LINK -> shareLink(
                        this,
                        document.generateWebLink(),
                        document.title
                    )

                    Methods.SEND_MESSAGE -> startForSendAttachments(this, accountId, document)

                    Methods.REPOST_YOURSELF -> goToPostCreation(
                        this,
                        accountId,
                        accountId,
                        EditingPostType.TEMP,
                        listOf(document)
                    )
                }
            }
            .setNegativeButton(R.string.button_cancel, null).show()
    }
}
