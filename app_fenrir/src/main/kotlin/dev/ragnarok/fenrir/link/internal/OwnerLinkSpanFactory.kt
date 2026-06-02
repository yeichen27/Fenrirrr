package dev.ragnarok.fenrir.link.internal

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple
import kotlin.math.abs

object OwnerLinkSpanFactory {
    private val LINK_COMPARATOR =
        Comparator { link1: AbsInternalLink, link2: AbsInternalLink -> link1.start - link2.start }
    private val ownerPattern: Regex = Regex("\\[(id|club)(\\d+)\\|([^]]+)]")
    private val topicCommentPattern: Regex =
        Regex("\\[(id|club)(\\d*):bp(-\\d*)_(\\d*)\\|([^]]+)]")

    fun findPatterns(
        input: String?,
        owners: Boolean,
        topics: Boolean
    ): List<AbsInternalLink>? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val ownerLinks = if (owners) findOwnersLinks(input) else null
        val topicLinks = if (topics) findTopicLinks(input) else null
        val count = safeCountOfMultiple(ownerLinks, topicLinks)
        if (count > 0) {
            val all: MutableList<AbsInternalLink> = ArrayList(count)
            if (ownerLinks.nonNullNoEmpty()) {
                all.addAll(ownerLinks)
            }
            if (topicLinks.nonNullNoEmpty()) {
                all.addAll(topicLinks)
            }
            all.sortWith(LINK_COMPARATOR)
            return all
        }
        return null
    }

    fun withSpans(
        input: CharSequence?,
        owners: Boolean,
        topics: Boolean,
        listener: ActionListener?
    ): CharSequence? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val ownerLinks = if (owners) findOwnersLinks(input) else null
        val topicLinks = if (topics) findTopicLinks(input) else null
        val count = safeCountOfMultiple(ownerLinks, topicLinks)
        if (count > 0) {
            val all: MutableList<AbsInternalLink> = ArrayList(count)
            if (ownerLinks.nonNullNoEmpty()) {
                all.addAll(ownerLinks)
            }
            if (topicLinks.nonNullNoEmpty()) {
                all.addAll(topicLinks)
            }
            all.sortWith(LINK_COMPARATOR)
            val result = SpannableStringBuilder(replace(input, all))
            for (link in all) {
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (listener != null) {
                            if (link is TopicLink) {
                                listener.onTopicLinkClicked(link)
                            }
                            if (link is OwnerLink) {
                                listener.onOwnerClick(link.ownerId)
                            }
                        }
                    }
                }
                result.setSpan(
                    clickableSpan,
                    link.start,
                    link.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return result
        }
        return input
    }

    private fun toLong(str: String?, pow_n: Int): Long {
        if (str.isNullOrEmpty()) {
            return Settings.get().accounts().current
        }
        try {
            return str.toLong() * pow_n
        } catch (_: NumberFormatException) {
        }
        return Settings.get().accounts().current
    }

    private fun findTopicLinks(input: CharSequence): List<TopicLink>? {
        return try {
            val res = topicCommentPattern.findAll(input)
            val links: MutableList<TopicLink> = ArrayList(res.count())
            for (i in res) {
                val link = TopicLink()
                val club = "club" == i.groupValues.getOrNull(1)
                link.start = i.range.first
                link.end = i.range.last + 1
                link.replyToOwner = toLong(i.groupValues.getOrNull(2), if (club) -1 else 1)
                link.topicOwnerId = toLong(i.groupValues.getOrNull(3), 1)
                link.replyToCommentId = i.groupValues.getOrNull(4)?.toInt().orZero()
                link.targetLine = i.groupValues.getOrNull(5)
                links.add(link)
            }
            links
        } catch (_: Exception) {
            null
        }
    }

    private fun findOwnersLinks(input: CharSequence): List<OwnerLink>? {
        return try {
            val res = ownerPattern.findAll(input)
            val links: MutableList<OwnerLink> = ArrayList(res.count())
            for (i in res) {
                val club = "club" == i.groupValues.getOrNull(1)
                val ownerId = toLong(i.groupValues.getOrNull(2), if (club) -1 else 1)
                val name = i.groupValues.getOrNull(3)

                links.add(OwnerLink(i.range.first, i.range.last + 1, ownerId, name.orEmpty()))
            }
            links
        } catch (_: Exception) {
            null
        }
    }

    fun getTextWithCollapseOwnerLinks(input: CharSequence?): CharSequence? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val links = findOwnersLinks(input)
        return replace(input, links)
    }

    private fun replace(input: CharSequence, links: List<AbsInternalLink>?): CharSequence {
        if (links.isNullOrEmpty()) {
            return input
        }
        val result = StringBuilder(input)
        for (y in links.indices) {
            val link = links[y]
            if (link.targetLine.isNullOrEmpty()) {
                continue
            }
            val origLenght = link.end - link.start
            val newLenght = link.targetLine?.length.orZero()
            shiftLinks(links, link, origLenght - newLenght)
            link.targetLine?.let { result.replace(link.start, link.end, it) }
            link.end -= (origLenght - newLenght)
        }
        return result.toString()
    }

    private fun shiftLinks(links: List<AbsInternalLink>?, after: AbsInternalLink?, count: Int) {
        links ?: return
        var shiftAllowed = false
        for (link in links) {
            if (shiftAllowed) {
                link.start -= count
                link.end -= count
            }
            if (link === after) {
                shiftAllowed = true
            }
        }
    }

    fun genOwnerLink(ownerId: Long, title: String?): String {
        return "[" + (if (ownerId > 0) "id" else "club") + abs(ownerId) + "|" + title + "]"
    }

    open class ActionListener {
        open fun onTopicLinkClicked(link: TopicLink) {}
        open fun onOwnerClick(ownerId: Long) {}
    }
}