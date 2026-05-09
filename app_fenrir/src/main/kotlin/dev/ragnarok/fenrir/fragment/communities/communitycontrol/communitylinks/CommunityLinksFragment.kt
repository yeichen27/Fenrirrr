package dev.ragnarok.fenrir.fragment.communities.communitycontrol.communitylinks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme

class CommunityLinksFragment : BaseMvpFragment<CommunityLinksPresenter, ICommunityLinksView>(),
    ICommunityLinksView, CommunityLinksAdapter.ActionListener {
    private var mLinksAdapter: CommunityLinksAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_community_links, container, false)
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        mLinksAdapter = CommunityLinksAdapter(emptyList())
        mLinksAdapter?.setActionListener(this)
        recyclerView.adapter = mLinksAdapter
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = CommunityLinksPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getLong(Extra.GROUP_ID),
        saveInstanceState
    )

    override fun displayRefreshing(loadingNow: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loadingNow
    }

    override fun notifyDataSetChanged() {
        mLinksAdapter?.notifyDataSetChanged()
    }

    override fun displayData(links: List<VKApiCommunity.Link>) {
        mLinksAdapter?.setData(links)
    }

    override fun openLink(link: String?) {
        val intent = Intent(Intent.ACTION_VIEW, link?.toUri())
        startActivity(intent)
    }

    override fun onClick(link: VKApiCommunity.Link) {
        presenter?.fireLinkClick(
            link
        )
    }

    companion object {
        fun newInstance(accountId: Long, groupId: Long): CommunityLinksFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putLong(Extra.GROUP_ID, groupId)
            val fragment = CommunityLinksFragment()
            fragment.arguments = args
            return fragment
        }
    }
}