package dev.ragnarok.fenrir.link.internal

class UrlLink(start: Int, end: Int, link: String, name: String) : AbsInternalLink() {
    val url: String

    init {
        this.start = start
        this.end = end
        targetLine = name
        url = link
    }
}