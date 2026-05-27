package com.yourcompany.mememongo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.math.*

data class Meme(
    val id: Int,
    val name: String,
    val rarity: Rarity,
    val position: Point
)

enum class Rarity(val weight: Double, val displayName: String) {
    COMMON(0.50, "Обычный"),
    RARE(0.30, "Редкий"),
    EPIC(0.15, "Эпический"),
    LEGENDARY(0.05, "Легендарный")
}

val allMemes = listOf(
    Meme(1, "Дрожащий кот", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(2, "Wojak", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(3, "Троллинг", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(4, "Успешный мальчик", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(5, "Пафосный кот", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(6, "Собака Доге", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(7, "Гарольд с бровями", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(8, "Торговец мемами", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(9, "Упоротый лис", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(10, "Ой да ладно", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(11, "Марио-разочарование", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(12, "Кот-булочка", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(13, "Панда с подносом", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(14, "Злющая лягушка", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(15, "Ну такое", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(16, "Хомяк Шлёпа", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(17, "Бегемот Филя", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(18, "Скелет-спорщик", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(19, "Грустный жираф", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(20, "Собака-музыкант", Rarity.COMMON, Point(0.0, 0.0)),
    Meme(21, "Можно, а зачем?", Rarity.RARE, Point(0.0, 0.0)),
    Meme(22, "Дрысясися", Rarity.RARE, Point(0.0, 0.0)),
    Meme(23, "Ждун", Rarity.RARE, Point(0.0, 0.0)),
    Meme(24, "Обычный парень (в костюме)", Rarity.RARE, Point(0.0, 0.0)),
    Meme(25, "Кот с помидорами", Rarity.RARE, Point(0.0, 0.0)),
    Meme(26, "Ну и ладно", Rarity.RARE, Point(0.0, 0.0)),
    Meme(27, "Бабайка", Rarity.RARE, Point(0.0, 0.0)),
    Meme(28, "Кукусики", Rarity.RARE, Point(0.0, 0.0)),
    Meme(29, "Кот в астрале", Rarity.EPIC, Point(0.0, 0.0)),
    Meme(30, "Chill Guy", Rarity.EPIC, Point(0.0, 0.0)),
    Meme(31, "Сеньор Чили", Rarity.EPIC, Point(0.0, 0.0)),
    Meme(32, "Духовный дядя", Rarity.EPIC, Point(0.0, 0.0)),
    Meme(33, "Сигма", Rarity.LEGENDARY, Point(0.0, 0.0)),
    Meme(34, "Глеб (философ)", Rarity.LEGENDARY, Point(0.0, 0.0))
)

val memesByRarity = allMemes.groupBy { it.rarity }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("a17943d6-3b10-46e0-948f-1c657165407c")
        MapKitFactory.initialize(this)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MemeGoMapScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}

@Composable
fun MemeGoMapScreen() {
    val context = LocalContext.current

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            android.widget.Toast.makeText(context, "Без геолокации игра не работает", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var currentLocation by remember { mutableStateOf<Point?>(null) }
    var activeMemes by remember { mutableStateOf<List<Meme>>(emptyList()) }
    var caughtCount by remember { mutableStateOf(0) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapObjects by remember { mutableStateOf<MapObjectCollection?>(null) }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val point = Point(it.latitude, it.longitude)
                    currentLocation = point
                    mapView?.map?.move(
                        CameraPosition(point, 17.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 1f),
                        null
                    )
                }
            }
        }
    }

    fun updateMarkers(memes: List<Meme>) {
        mapObjects?.clear()
        memes.forEach { meme ->
            val marker = mapObjects?.addPlacemark(meme.position)
            marker?.setIcon(ImageProvider.fromResource(context, android.R.drawable.star_big_on))
            marker?.setText(meme.name)
            marker?.userData = meme
            marker?.addTapListener { _, _ ->
                activeMemes = activeMemes.filter { it.id != meme.id }
                caughtCount++
                updateMarkers(activeMemes)
                true
            }
        }
    }

    fun spawnMemesAround(center: Point, count: Int = 5): List<Meme> {
        val newMemes = mutableListOf<Meme>()
        repeat(count) {
            val rarity = pickRarity()
            val possibleMemes = memesByRarity[rarity] ?: return@repeat
            val template = possibleMemes.random()
            val randomPos = randomLocationNear(center, radiusMeters = 200.0)
            newMemes.add(template.copy(id = generateUniqueId(), position = randomPos))
        }
        return newMemes
    }

    LaunchedEffect(currentLocation) {
        val location = currentLocation
        if (location != null && mapObjects != null) {
            activeMemes = spawnMemesAround(location)
            updateMarkers(activeMemes)
        }
    }

    LaunchedEffect(mapView) {
        mapView?.let {
            mapObjects = it.map.getMapObjects()
            currentLocation?.let { loc ->
                activeMemes = spawnMemesAround(loc)
                updateMarkers(activeMemes)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            Card {
                Text(
                    text = "Поймано: $caughtCount",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val location = currentLocation
                if (location != null && mapObjects != null) {
                    activeMemes = spawnMemesAround(location)
                    updateMarkers(activeMemes)
                }
            }) {
                Text("Найти мемов")
            }
        }
    }
}

fun pickRarity(): Rarity {
    val rand = Math.random()
    var cumulative = 0.0
    for (rarity in Rarity.values()) {
        cumulative += rarity.weight
        if (rand < cumulative) return rarity
    }
    return Rarity.COMMON
}

fun randomLocationNear(center: Point, radiusMeters: Double): Point {
    val earthRadius = 6371000.0
    val latRad = Math.toRadians(center.latitude)
    val lonRad = Math.toRadians(center.longitude)

    val randomDistance = radiusMeters * sqrt(Math.random())
    val randomAngle = Math.random() * 2 * Math.PI

    val deltaLat = randomDistance * cos(randomAngle) / earthRadius
    val deltaLon = randomDistance * sin(randomAngle) / (earthRadius * cos(latRad))

    val newLat = latRad + deltaLat
    val newLon = lonRad + deltaLon

    return Point(Math.toDegrees(newLat), Math.toDegrees(newLon))
}

fun generateUniqueId(): Int = (1000..Int.MAX_VALUE).random()