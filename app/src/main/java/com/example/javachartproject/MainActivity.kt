package com.example.javachartproject

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.json.JSONException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mQ: RequestQueue? = null
    private var lineChart: LineChart? = null
    var millis = ArrayList<Float>()
    var dataVals = ArrayList<Entry>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lineChart = findViewById<View>(R.id.line_chart) as LineChart
        mQ = Volley.newRequestQueue(this)
        val url = "https://bemoss-e8288.firebaseio.com/Device1.json"
        val req = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    try {
                        for (i in 0 until response.length()) {
                            val entry = response.getJSONObject(i)
                            val time = entry.getString("time")
                            val power = entry.getDouble("power")
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'")
                            val date = LocalDateTime.parse(time, formatter)
                            millis.add(date.toInstant(ZoneOffset.UTC).toEpochMilli().toFloat())
                            val pw = power.toFloat()
                            dataVals.add(Entry(i.toFloat(), pw))
                        }
                        val lineDataSet1 = LineDataSet(dataVals, "Data Set 1")
                        val dataSets = ArrayList<ILineDataSet>()
                        dataSets.add(lineDataSet1)
                        lineChart!!.setNoDataText("No Data")
                        lineChart!!.setNoDataTextColor(Color.RED)
                        lineChart!!.setDrawGridBackground(true)
                        lineChart!!.setDrawBorders(true)
                        lineChart!!.setBorderColor(Color.BLUE)
                        //lineChart.setBorderWidth(2);
                        lineDataSet1.lineWidth = 4f
                        lineDataSet1.color = Color.BLACK
                        lineDataSet1.setDrawCircles(true)
                        lineDataSet1.setDrawCircleHole(true)
                        lineDataSet1.setCircleColor(Color.GRAY)
                        lineDataSet1.circleHoleColor = Color.BLACK
                        lineDataSet1.circleRadius = 5f
                        lineDataSet1.circleHoleRadius = 4f
                        lineDataSet1.valueTextSize = 15f
                        lineDataSet1.valueTextColor = Color.BLACK
                        //lineDataSet1.enableDashedLine(5,10,0);
                        lineDataSet1.color = Color.GRAY
                        val legend = lineChart!!.legend
                        legend.isEnabled = true
                        legend.textColor = Color.BLACK
                        legend.textSize = 15f
                        legend.form = Legend.LegendForm.LINE //Customizes legend icon
                        legend.formSize = 20f
                        legend.xEntrySpace = 15f
                        legend.formToTextSpace = 10f
                        val legendEntries = arrayOfNulls<LegendEntry>(1)
                        val ent = LegendEntry()
                        ent.formColor = Color.GRAY // Legend Color
                        ent.label = "Power (Watt)"
                        legendEntries[0] = ent
                        legend.setCustom(legendEntries)
                        val xAxis = lineChart!!.xAxis
                        val yAxis = lineChart!!.axisLeft
                        xAxis.position = XAxis.XAxisPosition.BOTTOM //X-Axis to bottom
                        xAxis.valueFormatter = MyXAxisValueFormatter()
                        xAxis.labelRotationAngle = -90f
                        xAxis.textSize = 12f
                        yAxis.textSize = 12f
                        //xAxis.granularity = 1f
                        lineChart!!.extraLeftOffset = 15f
                        lineChart!!.extraRightOffset = 15f
                        if (dataSets.size > 1) {
                            xAxis.setCenterAxisLabels(true)
                        } else {
                            xAxis.setCenterAxisLabels(false)
                        }
                        val description = Description()
                        description.text = "Energy Usage"
                        description.textColor = Color.BLACK
                        description.textSize = 20f
                        lineChart!!.description = description
                        val data = LineData(dataSets)
                        lineChart!!.data = data
                        lineChart!!.notifyDataSetChanged()
                        lineChart!!.invalidate()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error -> error.printStackTrace() })
        mQ?.add(req)
    }

    inner class MyXAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(dateIndex: Float): String {
            return try {
                val sdf: DateFormat = SimpleDateFormat("HH:mm dd/MMM/yyyy", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("GMT")
                val longDateInMiliseconds = millis[dateIndex.toInt()].toLong()
                val result = Date(longDateInMiliseconds)
                sdf.format(result)
            } catch (e: Exception) {
                millis[dateIndex.toInt()].toLong().toString()
            }
        }
    }
}