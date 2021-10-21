package com.pyro.timeup

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pyro.timeup.dummy.DummyContent.DummyItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [UsageFragment.OnListFragmentInteractionListener] interface.
 */
class UsageFragment : Fragment() {

    private var extractor: ApkInfoExtractor? = null
    private var apkInfoList: List<String> = emptyList()
    private var appLabels: ArrayList<String>? = null
    private var appIcons: ArrayList<Drawable>? = null
    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private var timedApps: HashMap<String, Long> = HashMap()
    var adapterList: ArrayList<String> = ArrayList()

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshList()

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)
        Thread().run { refreshList() }
        Thread().start()
        val simpleCallback: SimpleCallback = object : SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition //get position which is swipe
                if (direction == ItemTouchHelper.LEFT) { //if swipe left
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context) //alert for confirm to delete
                    builder.setMessage("Remove app's timer?") //set message
                    builder.setPositiveButton("REMOVE", DialogInterface.OnClickListener { _, _ ->
                        //when click on DELETE
                        adapterList.removeAt(position) //then remove item
                        editor = preferences!!.edit()
                        editor!!.remove(preferences!!.all.keys.elementAt(position))
                        editor!!.apply()
                        userVisibleHint = true
                        return@OnClickListener
                    }).setNegativeButton("CANCEL", DialogInterface.OnClickListener { _, _ ->
                        //not removing items if cancel is done
                        userVisibleHint = true
                        return@OnClickListener
                    }).show() //show alert dialog
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        if (view is RecyclerView) {
            itemTouchHelper.attachToRecyclerView(view)
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = UsageAdapter(context, adapterList as List<String>)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: DummyItem?)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
                UsageFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }

    // TODO: this operation should be done on launch
    private fun getAppData() {
        for (app in apkInfoList) {
            if (timedApps.containsKey(app)) {
                appLabels!!.add(extractor!!.getAppName(app))
                appIcons!!.add(extractor!!.getAppIconByPackageName(app))
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view is RecyclerView) {
                (view as RecyclerView).layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                (view as RecyclerView).adapter = UsageAdapter(context!!, adapterList as List<String>)
            }
            Thread().run { refreshList() }
            Thread().start()
            fragmentManager!!.beginTransaction().detach(this).attach(this).commitNowAllowingStateLoss()
        }
        else {
        }
    }

    private fun refreshList() {
        // Refresh your fragment here and reload the list
        adapterList = ArrayList()
        appLabels = ArrayList()
        appIcons = ArrayList()
        extractor = ApkInfoExtractor(context)
        apkInfoList = extractor!!.allInstalledApkInfo
        preferences = context!!.getSharedPreferences(getString(R.string.hours), Context.MODE_MULTI_PROCESS)
        timedApps = preferences!!.all as HashMap<String, Long>
        getAppData()
        for (apk in apkInfoList) {
            if (timedApps.containsKey(apk) && !adapterList.contains(apk)) {
                adapterList.add(apk)
            }
        }
    }
}