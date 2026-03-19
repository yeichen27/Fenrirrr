package dev.ragnarok.fenrir.fragment.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.fragment.search.artistsearch.ArtistSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.audioplaylistsearch.AudioPlaylistSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.audiossearch.AudiosSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.audiossearch.AudiosSearchFragment.Companion.newInstanceSelect
import dev.ragnarok.fenrir.fragment.search.communitiessearch.CommunitiesSearchFragment
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.DialogsSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.DocumentSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.GroupSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.MessageSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.PhotoSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria
import dev.ragnarok.fenrir.fragment.search.criteria.WallSearchCriteria
import dev.ragnarok.fenrir.fragment.search.dialogssearch.DialogsSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.docssearch.DocsSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.messagessearch.MessagesSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.newsfeedsearch.NewsFeedSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.peoplesearch.PeopleSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.photosearch.PhotoSearchFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.search.videosearch.VideoSearchFragment
import dev.ragnarok.fenrir.fragment.search.wallsearch.WallSearchFragment
import dev.ragnarok.fenrir.getParcelableCompat

object SearchFragmentFactory {
    fun buildArgs(
        accountId: Long,
        @SearchContentType contentType: Int,
        criteria: BaseSearchCriteria?,
        hideToolbar: Boolean
    ): Bundle {
        val args = Bundle()
        args.putInt(Extra.TYPE, contentType)
        args.putLong(Extra.ACCOUNT_ID, accountId)
        args.putParcelable(Extra.CRITERIA, criteria)
        args.putBoolean(Extra.IN_TABS_CONTAINER, hideToolbar)
        return args
    }

    fun create(args: Bundle?): Fragment {
        if (args == null) {
            throw UnsupportedOperationException()
        }
        return create(
            args.getLong(Extra.ACCOUNT_ID),
            args.getInt(Extra.TYPE),
            args.getParcelableCompat(Extra.CRITERIA),
            args.getBoolean(Extra.IN_TABS_CONTAINER)
        )
    }

    fun create(
        accountId: Long,
        @SearchContentType type: Int,
        criteria: BaseSearchCriteria? = null,
        hideToolbar: Boolean = true
    ): Fragment {
        return when (type) {
            SearchContentType.PEOPLE -> newInstance(
                accountId,
                criteria as? PeopleSearchCriteria,
                hideToolbar
            )

            SearchContentType.COMMUNITIES -> CommunitiesSearchFragment.newInstance(
                accountId,
                criteria as? GroupSearchCriteria,
                hideToolbar
            )

            SearchContentType.VIDEOS -> VideoSearchFragment.newInstance(
                accountId,
                criteria as? VideoSearchCriteria,
                hideToolbar
            )

            SearchContentType.AUDIOS -> newInstance(
                accountId,
                criteria as? AudioSearchCriteria,
                hideToolbar
            )

            SearchContentType.ARTISTS -> newInstance(
                accountId,
                criteria as? ArtistSearchCriteria,
                hideToolbar
            )

            SearchContentType.AUDIOS_SELECT -> newInstanceSelect(
                accountId,
                criteria as? AudioSearchCriteria,
                hideToolbar
            )

            SearchContentType.AUDIO_PLAYLISTS -> newInstance(
                accountId,
                criteria as? AudioPlaylistSearchCriteria,
                hideToolbar
            )

            SearchContentType.DOCUMENTS -> newInstance(
                accountId,
                criteria as? DocumentSearchCriteria,
                hideToolbar
            )

            SearchContentType.NEWS -> newInstance(
                accountId,
                criteria as? NewsFeedCriteria,
                hideToolbar
            )

            SearchContentType.MESSAGES -> newInstance(
                accountId,
                criteria as? MessageSearchCriteria,
                hideToolbar
            )

            SearchContentType.WALL -> WallSearchFragment.newInstance(
                accountId,
                criteria as? WallSearchCriteria,
                hideToolbar
            )

            SearchContentType.DIALOGS -> newInstance(
                accountId,
                criteria as? DialogsSearchCriteria,
                hideToolbar
            )

            SearchContentType.PHOTOS -> newInstance(
                accountId,
                criteria as? PhotoSearchCriteria,
                hideToolbar
            )

            else -> throw UnsupportedOperationException()
        }
    }
}