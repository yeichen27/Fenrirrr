package dev.ragnarok.fenrir.fragment.photos.localimagealbums

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.LocalImageAlbum
import dev.ragnarok.fenrir.picasso.Content_Local
import dev.ragnarok.fenrir.place.PlaceFactory.getLocalImageAlbumPlace
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.view.MySearchView
import kotlin.math.max

class LocalImageAlbumsFragment :
    BaseMvpFragment<LocalPhotoAlbumsPresenter, ILocalPhotoAlbumsView>(),
    LocalPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener,
    ILocalPhotoAlbumsView {
    private val requestReadPermission =
        requestPermissionsAbs(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            lazyPresenter {
                fireReadExternalStoregePermissionResolved()
            }
        }
    private var mRecyclerView: RecyclerView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mEmptyTextView: TextView? = null
    private var mAlbumsAdapter: LocalPhotoAlbumsAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_local_albums_gallery, container, false)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        val columnCount = resources.getInteger(R.integer.photos_albums_column_count)
        val manager: RecyclerView.LayoutManager =
            StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL)
        mRecyclerView = root.findViewById(R.id.list)
        mRecyclerView?.layoutManager = manager
        if (!hasHideToolbarExtra()) {
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

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
                    ?.setPadding(insets.left, insets.top, insets.right, 0)
                mRecyclerView?.setPadding(insets.left, 0, insets.right, imeFixedBottom)
                WindowInsetsCompat.CONSUMED
            }
        } else {
            toolbar.visibility = View.GONE
        }
        val mySearchView: MySearchView = root.findViewById(R.id.searchview)
        mySearchView.setRightButtonVisibility(false)
        mySearchView.setLeftIcon(R.drawable.magnify)
        mySearchView.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.fireSearchRequestChanged(
                    query,
                    false
                )
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.fireSearchRequestChanged(
                    newText,
                    false
                )
                return false
            }
        })
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

        PicassoPauseOnScrollListener.addListener(mRecyclerView, LocalPhotoAlbumsAdapter.PICASSO_TAG)
        mAlbumsAdapter =
            LocalPhotoAlbumsAdapter(requireActivity(), emptyList(), Content_Local.PHOTO)
        mAlbumsAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAlbumsAdapter
        mEmptyTextView = root.findViewById(R.id.empty)
        return root
    }

    override fun onClick(album: LocalImageAlbum) {
        presenter?.fireAlbumClick(
            album
        )
    }

    override fun onRefresh() {
        presenter?.fireRefresh()
    }

    override fun displayData(data: List<LocalImageAlbum>) {
        mAlbumsAdapter?.setData(data)
    }

    override fun setEmptyTextVisible(visible: Boolean) {
        mEmptyTextView?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun displayProgress(loading: Boolean) {
        mSwipeRefreshLayout?.post { mSwipeRefreshLayout?.isRefreshing = loading }
    }

    override fun openAlbum(album: LocalImageAlbum) {
        getLocalImageAlbumPlace(album).tryOpenWith(requireActivity())
    }

    override fun notifyDataChanged() {
        mAlbumsAdapter?.notifyDataSetChanged()
    }

    override fun requestReadExternalStoragePermission() {
        requestReadPermission.launch()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = LocalPhotoAlbumsPresenter(
        saveInstanceState
    )
}