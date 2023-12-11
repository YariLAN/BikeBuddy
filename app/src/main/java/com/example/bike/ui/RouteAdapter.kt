package com.example.bike.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.bike.R
import com.example.bike.datasources.Route
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.time.LocalDateTime

// Класс для оформления элемента списка
class RouteAdapter(context: Context, private val resource: ArrayList<Route>) :
    ArrayAdapter<Route>(context, R.layout.item_route, resource) {
    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
        val view : View = inflater.inflate(R.layout.item_route, parent, false);

        val title: TextView = view.findViewById<TextView?>(R.id.title_date)
        val text: TextView = view.findViewById<TextView?>(R.id.text_distance)

        val date = org.joda.time.LocalDateTime(resource[position].startTime)

        val format = date.toString(DateTimeFormat.forPattern("MM.dd.yyyy HH:mm:ss"))

        title.text = "Дата: $format";
        text.text  = "Расстояние: ${resource[position].distance} метров";

        return view;
    }
}