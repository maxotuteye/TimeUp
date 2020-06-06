package com.pyro.timeup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


internal class UsageAdapter(private val context1: Context, private var stringList: List<String>) : RecyclerView.Adapter<UsageAdapter.ViewHolder>() {
    private val HOURS = "hours"
    private var preferences: SharedPreferences = context1.getSharedPreferences(HOURS, Context.MODE_PRIVATE)
    private val usageStatsManager: UsageStatsManager = context1.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val usageStatsList: MutableList<UsageStats> = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
    private var usedApps: HashMap<String, Long> = HashMap()
    private var timedApps: HashMap<String, Float> = preferences.all as HashMap<String, Float>
    private var timeSum: Long = 0

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cardView: CardView = view.findViewById(R.id.usage_card_view)
        var imageView: ImageView = view.findViewById(R.id.usageImageview)
        var textviewAppName: TextView = view.findViewById(R.id.usageApk_Name)
        var textviewAppPackageName: TextView = view.findViewById(R.id.usageApk_Package_Name)
        var percentIndicator: TextView = view.findViewById(R.id.percentIndicator)
        var usedTime: TextView = view.findViewById(R.id.used)
        var totalTime: TextView = view.findViewById(R.id.total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view2 = LayoutInflater.from(context1).inflate(R.layout.usage_card_layout, parent, false)
        preferences = context1.getSharedPreferences(HOURS, Context.MODE_PRIVATE)
        timeSum = 0
        for (stats: UsageStats in usageStatsList) {
            if (stats.totalTimeInForeground.toInt() != 0 && timedApps.containsKey(stats.packageName)) {
                usedApps[stats.packageName] = stats.totalTimeInForeground
                timeSum += stats.totalTimeInForeground
            }
        }
        return ViewHolder(view2)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val apkInfoExtractor = ApkInfoExtractor(context1)
        val applicationPackageName = stringList[position]
        val applicationLabelName = apkInfoExtractor.getAppName(applicationPackageName)
        val drawable = apkInfoExtractor.getAppIconByPackageName(applicationPackageName)
        viewHolder.textviewAppName.text = applicationLabelName
        viewHolder.textviewAppPackageName.text = applicationPackageName
        viewHolder.imageView.setImageDrawable(drawable)
        viewHolder.cardView.setOnClickListener {
            val builder = AlertDialog.Builder(context1) //alert for confirm to delete
            builder.setMessage("Remove app's timer?") //set message
            builder.setPositiveButton("REMOVE", DialogInterface.OnClickListener { _, _ ->
                //when REMOVE is clicked
                preferences = context1.getSharedPreferences(HOURS, Context.MODE_PRIVATE)
                preferences.edit().remove(applicationPackageName).apply()
                viewHolder.cardView.visibility = View.INVISIBLE
                notifyDataSetChanged()
                return@OnClickListener
            }).setNegativeButton("CANCEL", DialogInterface.OnClickListener { _, _ ->
                //not removing items if cancel is selected
                return@OnClickListener
            }).show() //show alert dialog
        }
        viewHolder.percentIndicator.text = getUsagePercent(applicationPackageName)
        viewHolder.usedTime.text = formatTime(usedApps[applicationPackageName])
        viewHolder.totalTime.text = formatTime(timedApps[applicationPackageName]!!.toLong())
    }

    override fun getItemCount(): Int = stringList.size

    private fun getUsagePercent(sample: String): String {
        val used = usedApps[sample]
        val timed = timedApps[sample]
        return if (used == null || timed == null)   "0%"
            else  (used.toFloat() / timed.toFloat() * 100).toInt().toString() + "%"

    }
    private fun formatTime(l: Long?): String{
        if (l == null) return "Not Used"
        val minutes = (l / (1000 * 60) % 60).toInt()
        val seconds = (l / 1000).toInt() % 60
        val hours = (l / (1000 * 60 * 60) % 24).toInt()
        return "${hours}h ${minutes}m ${seconds}s"
    }
}