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


// Класс для оформления элемента списка
class RouteAdapter(context: Context, private val resource: ArrayList<Route>) :
    ArrayAdapter<Route>(context, R.layout.item_route, resource) {
    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // создание экземпляра пользовательского интерфейса из файлов разметки XML в Android
        val inflater : LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
        val view : View = inflater.inflate(R.layout.item_route, parent, false);

        // Заголовок для элемента списка
        val title: TextView = view.findViewById<TextView?>(R.id.title_date)

        // Основная часть для элемента списка
        val text: TextView = view.findViewById<TextView?>(R.id.text_distance)

        // приведение типа строки в дату
        val date = org.joda.time.LocalDateTime(resource[position].startTime)

        // форматирование даты в вид "dd.MM.yyyy HH:mm:ss"
        val format = date.toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss"))

        // запись даты и дистанции к заголовку и части элемента списка
        title.text = "Дата: $format";
        text.text  = "Расстояние: ${resource[position].distance} метров";

        return view;
    }
}