package com.example.bike.ui

// библиотека с первоначальной конфигурацией приложения
import android.Manifest

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bike.BuildConfig
import com.example.bike.R
import com.example.bike.databinding.FragmentLocationBinding
import com.example.bike.datasources.Route
import com.example.bike.repository.RouteRepository
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import org.joda.time.LocalDateTime
import java.util.*


// Статический класс с указанием пройденного расстояния
object DistanceTracker {
    var totalDistance: Long = 0L
}

@Suppress("DEPRECATION")

// Фпагмент для основного взаимодействия с картой
class LocationFragment: Fragment(), OnMapReadyCallback {

    // биндинг для данного фрагмента
    private lateinit var locBinding: FragmentLocationBinding;

    // получение карты GoogleMap
    private lateinit var map: GoogleMap

    // получение базового фрагмента из библиотеки Map SDK
    private lateinit var mapFragment: SupportMapFragment

    // объект для получения местоположения устройства
    private lateinit var client: FusedLocationProviderClient

    // интерфейс в библиотеке Google Play Services,
    // используемый для обработки результатов
    // запросов местоположения устройства
    private lateinit var locationCallback: LocationCallback

    // переменная для учета последнего местоположения
    private var lastLocation: Location? = null

    // дата и локация начала поездки
    private lateinit var startDateTime: LocalDateTime
    private var startLocation: Location? = null

    // Метод создания фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // получение биндинга
        locBinding = FragmentLocationBinding.inflate(inflater, container, false)

        // получение базового фрагмента для карты
        mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Инициализация провайдера для работы с локацией
        client = LocationServices.getFusedLocationProviderClient(requireContext())

        // Список из вариантов темы для карты
        setHasOptionsMenu(true)

        // Логика для кнопки "Старт"
        locBinding.startLoc.setOnClickListener {
            startLocationTracking()

            it.visibility = View.INVISIBLE
            locBinding.locationInfo.visibility = View.VISIBLE
            locBinding.stopLoc.visibility = View.VISIBLE
        }

        // Логика для кнопки "Стоп"
        locBinding.stopLoc.setOnClickListener {
            stopLocationTracking()

            it.visibility = View.INVISIBLE
            locBinding.locationInfo.visibility = View.INVISIBLE
            locBinding.startLoc.visibility = View.VISIBLE
        }

        return locBinding.root
    }

    // Создание меню для фрагмента
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.types_menu, menu)
    }

    // выбор темы карты из меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        map.mapType = when (item.itemId) {
            // Нормальная
            R.id.normal -> GoogleMap.MAP_TYPE_NORMAL;
            // Рельеф
            R.id.terrain -> GoogleMap.MAP_TYPE_TERRAIN;
            // Спутник
            R.id.satelite -> GoogleMap.MAP_TYPE_SATELLITE;
            // Гибрид (спутник + данные по карте)
            R.id.hybrid -> GoogleMap.MAP_TYPE_HYBRID;

            // карты нет
            else -> GoogleMap.MAP_TYPE_NONE
        }

        return super.onOptionsItemSelected(item)
    }

    // уникальный код запроса разрешений на доступ к местоположению
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    // роверяется наличие разрешения на доступ к точному местоположению устройства,
    // и, если разрешение отсутствует, запрашивается у пользователя данное
    // разрешение с кодом запроса
    private fun checkPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true;
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            false;
        }
    }

    // было ли предоставлено разрешение на доступ к точному местоположению
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }

    // Статический компаньон-объект (companion object) внутри класса
    // или интерфейса, который содержит фабричный метод
    // для создания экземпляра фрагмента
    companion object {
        @JvmStatic
        fun newInstance() =
            LocationFragment().apply { arguments = Bundle().apply {} }
    }


    // вызов метода при открытие карты
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        // есть ли разрешение отслеживать геопозицию
        if (checkPermission()) {
            this.map.isMyLocationEnabled = true;

            // установка значения местоположения
            client.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastLocation = task.result

                    // если точка найдена - сместить камеру карты к точке
                    if (lastLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            com.google.android.gms.maps.model.LatLng(
                                lastLocation.latitude,
                                lastLocation.longitude),
                            15f))
                    }
                }
            }
        }
    }

    // Отслеживание метки местоположения
    private fun startLocationTracking() {

        // установка точности для отслеживнаия
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            smallestDisplacement = 10.0F
        }

        // инициализируем время начало отслеживания
        startDateTime = LocalDateTime.now()

        // алгоритм обработки обновления координат устройства
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(loc: LocationResult) {
                loc?.let {
                    // если точка начальная
                    if(lastLocation == null){
                        lastLocation  = it.lastLocation
                        startLocation = it.lastLocation
                        return@let
                    }

                    it.lastLocation?.let { its_last ->

                        // вычисление дистанции
                        val distanceInMeters = its_last.distanceTo(lastLocation!!)
                        DistanceTracker.totalDistance += distanceInMeters.toLong()

                        val msg = "Completed: ${DistanceTracker.totalDistance} meters"

                        if(BuildConfig.DEBUG){
                            // журналирование пройденного расстояния
                            Log.d("TRACKER", "$msg, (added $distanceInMeters)")
                        }
                        locBinding.locationInfo.text = msg
                    }
                    lastLocation = it.lastLocation
                }
                super.onLocationResult(loc)
            }
        }

        // получение клиента - метки устройства
        client = LocationServices.getFusedLocationProviderClient(requireContext())

        // есть разрешение - вызывай обработку обновлений локации
        if (checkPermission()) {
            client.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    // запись данных о пройденном маршруте в базу
    private fun postRouteCommand() {
        // объект класса Маршрут
        val route: Route = Route(
            DistanceTracker.totalDistance.toString(),
            LocalDateTime.now().toString(),
            UUID.randomUUID().toString(),
            lastLocation!!.latitude.toString(),
            lastLocation!!.longitude.toString(),
            startLocation!!.latitude.toString(),
            startLocation!!.longitude.toString(),
            startDateTime.toString(),
            FirebaseAuth.getInstance().currentUser!!.uid,
        )

        // Вызов репозитория на добавление записи
        RouteRepository.addItem(route)
    }

    // Остановить отслеживание
    private fun stopLocationTracking() {
        // Остановка обработки обновления локации
        client.removeLocationUpdates(locationCallback)

        // Запись маршрута в базу
        postRouteCommand()

        // Вывод сообщения на экран
        makeText(context, "Прошли ${DistanceTracker.totalDistance} метров", LENGTH_SHORT).show()

        // Обнуление необходимых для отслеживания данных
        locBinding.locationInfo.text = ""
        DistanceTracker.totalDistance = 0L
        startLocation = null
        lastLocation = null
    }
}