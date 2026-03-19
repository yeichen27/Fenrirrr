package dev.ragnarok.fenrir.fragment.proxymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.place.PlaceFactory.proxyAddPlace
import kotlin.math.max

class ProxyManagerFrgament : BaseMvpFragment<ProxyManagerPresenter, IProxyManagerView>(),
    IProxyManagerView, ProxiesAdapter.ActionListener, MenuProvider {
    private var mProxiesAdapter: ProxiesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_proxy_manager, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeFixedBottom =
                if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.ime()
                    ).bottom, insets.bottom
                ) else insets.bottom
            v.setPadding(
                insets.left,
                insets.top,
                insets.right,
                imeFixedBottom
            )
            WindowInsetsCompat.CONSUMED
        }

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        mProxiesAdapter = ProxiesAdapter(mutableListOf(), this)
        recyclerView.adapter = mProxiesAdapter
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.proxies, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_add) {
            presenter?.fireAddClick()
            return true
        }
        return false
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        ProxyManagerPresenter(saveInstanceState)

    override fun displayData(configs: MutableList<ProxyConfig>, active: ProxyConfig?) {
        mProxiesAdapter?.setData(configs, active)
    }

    override fun notifyItemAdded(position: Int) {
        mProxiesAdapter?.notifyItemBindableRangeInserted(position, 1)
    }

    override fun notifyItemRemoved(position: Int) {
        mProxiesAdapter?.notifyItemBindableRemoved(position)
    }

    override fun setActiveAndNotifyDataSetChanged(config: ProxyConfig?) {
        mProxiesAdapter?.setActive(config)
    }

    override fun goToAddingScreen() {
        proxyAddPlace.tryOpenWith(requireActivity())
    }

    override fun onDeleteClick(config: ProxyConfig) {
        presenter?.fireDeleteClick(
            config
        )
    }

    override fun onSetAtiveClick(config: ProxyConfig) {
        presenter?.fireActivateClick(
            config
        )
    }

    override fun onDisableClick(config: ProxyConfig) {
        presenter?.fireDisableClick()
    }

    companion object {
        fun newInstance(): ProxyManagerFrgament {
            val args = Bundle()
            val fragment = ProxyManagerFrgament()
            fragment.arguments = args
            return fragment
        }
    }
}