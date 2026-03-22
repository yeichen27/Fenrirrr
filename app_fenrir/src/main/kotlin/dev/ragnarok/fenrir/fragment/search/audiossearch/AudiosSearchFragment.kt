package dev.ragnarok.fenrir.fragment.search.audiossearch

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.MainActivity
import dev.ragnarok.fenrir.fragment.audio.audios.AudioRecyclerAdapter
import dev.ragnarok.fenrir.fragment.search.abssearch.AbsSearchFragment
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.Utils
import kotlin.math.max

class AudiosSearchFragment :
    AbsSearchFragment<AudiosSearchPresenter, IAudioSearchView, Audio, AudioRecyclerAdapter>(),
    IAudioSearchView {
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        customToast?.showToast(R.string.permission_all_granted_text)
    }

    override fun onCreateInsetListener(root: View) {
        val goto: FloatingActionButton = root.findViewById(R.id.goto_button)
        if (requireActivity() is MainActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                val imeFixedBottom =
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                        windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                        ).bottom, insets.bottom
                    ) else insets.bottom
                root.findViewById<View>(R.id.actionbar)
                    ?.setPadding(insets.left, 0, insets.right, 0)
                root.findViewById<View>(R.id.toolbar)
                    ?.setPadding(0, insets.top, 0, 0)
                recyclerView?.setPadding(insets.left, 0, insets.right, imeFixedBottom)
                (goto.layoutParams as? CoordinatorLayout.LayoutParams)?.bottomMargin =
                    imeFixedBottom + Utils.dp(16f)
                (goto.layoutParams as? CoordinatorLayout.LayoutParams)?.rightMargin =
                    insets.right + Utils.dp(16f)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private var isSelectMode = false
    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemBindableRangeInserted(position, count)
    }

    override fun setAdapterData(adapter: AudioRecyclerAdapter, data: MutableList<Audio>) {
        adapter.setData(data)
    }

    override fun createViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_search_audio, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT)
    }

    override fun postCreate(root: View) {
        val goto: FloatingActionButton = root.findViewById(R.id.goto_button)
        if (isSelectMode) goto.setImageResource(R.drawable.check) else goto.setImageResource(R.drawable.audio_player)
        if (!isSelectMode) {
            goto.setOnLongClickListener {
                val curr = currentAudio
                if (curr != null) {
                    getPlayerPlace(Settings.get().accounts().current).tryOpenWith(requireActivity())
                } else customToast?.showToastError(R.string.null_audio)
                false
            }
        }
        goto.setOnClickListener {
            if (isSelectMode) {
                val intent = Intent()
                intent.putParcelableArrayListExtra(
                    Extra.ATTACHMENTS,
                    presenter?.selected ?: ArrayList()
                )
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            } else {
                val curr = currentAudio
                if (curr != null) {
                    val index = presenter?.getAudioPos(curr) ?: -1
                    if (index >= 0) {
                        recyclerView?.scrollToPosition(index + mAdapter?.headersCount.orZero())
                    } else customToast?.showToast(R.string.audio_not_found)
                } else customToast?.showToastError(R.string.null_audio)
            }
        }
    }

    override fun createAdapter(data: MutableList<Audio>): AudioRecyclerAdapter {
        val adapter =
            AudioRecyclerAdapter(requireActivity(), mutableListOf(), false, isSelectMode, null)
        adapter.setClickListener(object : AudioRecyclerAdapter.ClickListener {
            override fun onClick(position: Int, audio: Audio) {
                presenter?.playAudio(
                    requireActivity(),
                    position
                )
            }

            override fun onEdit(position: Int, audio: Audio) {}
            override fun onDelete(position: Int) {}
            override fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String) {
                getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity())
            }

            override fun onRequestWritePermissions() {
                requestWritePermission.launch()
            }
        })
        return adapter
    }

    override fun notifyAudioChanged(index: Int) {
        mAdapter?.notifyItemBindableChanged(index)
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = AudiosSearchPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getParcelableCompat(Extra.CRITERIA),
        saveInstanceState
    )

    companion object {
        const val ACTION_SELECT = "AudiosSearchFragment.ACTION_SELECT"


        fun newInstance(
            accountId: Long,
            criteria: AudioSearchCriteria?,
            hideToolbar: Boolean
        ): AudiosSearchFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putParcelable(Extra.CRITERIA, criteria)
            args.putBoolean(Extra.IN_TABS_CONTAINER, hideToolbar)
            val fragment = AudiosSearchFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstanceSelect(
            accountId: Long,
            criteria: AudioSearchCriteria?,
            hideToolbar: Boolean
        ): AudiosSearchFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putParcelable(Extra.CRITERIA, criteria)
            args.putBoolean(ACTION_SELECT, true)
            args.putBoolean(Extra.IN_TABS_CONTAINER, hideToolbar)
            val fragment = AudiosSearchFragment()
            fragment.arguments = args
            return fragment
        }
    }
}