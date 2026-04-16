package com.kampus.kampusatikyonetimsistemi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// --- VERİ VE EKRAN MODELLERİ ---
data class BinData(
    val id: Int = 0,
    val locationName: String = "",
    val fillLevel: Int = 0,
    val type: String = "",
    val building: String = "",
    val floor: String = ""
)

enum class FilterType(val label: String) {
    ALL("Tümü"),
    CRITICAL("%80 Üzeri"),
    WARNING("%50 - %80 Arası"),
    SAFE("%50 Altı")
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val index: Int) {
    object Dashboard : Screen("dashboard", "Panel", Icons.Default.Home, 0)
    object Map : Screen("map", "Harita", Icons.Default.LocationOn, 1)
    object Profile : Screen("profile", "Profil", Icons.Default.Person, 2)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        askNotificationPermission()

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Atık Takip Kanalı"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("WASTE_ALERTS", name, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

// --- ANA EKRAN VE NAVİGASYON ---
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Dashboard, Screen.Map, Screen.Profile)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F9FA))
        .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val targetIdx = navItems.find { it.route == targetState.destination.route }?.index ?: 0
                val initialIdx = navItems.find { it.route == initialState.destination.route }?.index ?: 0
                if (targetIdx > initialIdx) slideInHorizontally { 1000 } + fadeIn() else slideInHorizontally { -1000 } + fadeIn()
            },
            exitTransition = {
                val targetIdx = navItems.find { it.route == targetState.destination.route }?.index ?: 0
                val initialIdx = navItems.find { it.route == initialState.destination.route }?.index ?: 0
                if (targetIdx > initialIdx) slideOutHorizontally { -1000 } + fadeOut() else slideOutHorizontally { 1000 } + fadeOut()
            }
        ) {
            composable(Screen.Dashboard.route) { DashboardContent() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }

        // --- Liquid Glass Bar ---
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 40.dp, end = 40.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 10.dp
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                if (!isSelected) navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true
                                }
                            }
                        ) {
                            Icon(screen.icon, null, tint = if (isSelected) Color(0xFF2E7D32) else Color(0xFFBDBDBD), modifier = Modifier.size(22.dp))
                            Text(screen.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFBDBDBD))
                        }
                    }
                }
            }
        }
    }
}

// --- DASHBOARD İÇERİĞİ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent() {
    val currentContext = LocalContext.current

    val binList = remember { mutableStateListOf<BinData>() }

    var selectedStatusFilter by remember { mutableStateOf(FilterType.CRITICAL) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val buildingList = listOf("Tümü", "Kütüphane", "Yemekhane", "B Blok", "C Blok")
    var selectedBuilding by remember { mutableStateOf("Tümü") }

    val notifiedBins = rememberSaveable { mutableStateOf(setOf<String>()) }
    
    // Firebase bağlantısı (Bu kısım "Sihirli" kısımdır)
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance("https://kampusatikyonetimsistemi-dca93-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("bins")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binList.clear()
                val currentNotified = notifiedBins.value.toMutableSet()

                for (binSnapshot in snapshot.children) {
                    val bin = binSnapshot.getValue(BinData::class.java)
                    if (bin != null) {
                        binList.add(bin)

                        // Bildirim Mantığı:
                        if (bin.fillLevel > 80) {
                            // Eğer bu ID için daha önce bildirim gönderilmediyse gönder
                            if (!currentNotified.contains(bin.id.toString())) {
                                sendNotification(currentContext, "Kritik Doluluk!", "${bin.locationName} boşaltılmalı.")
                                currentNotified.add(bin.id.toString())
                            }
                        } else {
                            // Eğer kutu boşaltıldıysa (%80 altına düştüyse) listeden çıkar
                            // Böylece tekrar dolduğunda yeni bildirim gidebilir.
                            currentNotified.remove(bin.id.toString())
                        }
                    }
                }
                // Güncel listeyi kaydet
                notifiedBins.value = currentNotified
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Filtreleme Mantığı (Netleştirildi)
    val filteredBins = binList.filter { bin ->
        val statusMatch = when (selectedStatusFilter) {
            FilterType.ALL -> true
            FilterType.CRITICAL -> bin.fillLevel > 80
            FilterType.WARNING -> bin.fillLevel in 50..80
            FilterType.SAFE -> bin.fillLevel < 50
        }
        val buildingMatch = if (selectedBuilding == "Tümü") true else bin.building == selectedBuilding
        statusMatch && buildingMatch
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("CampusFlow", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32)) },
            actions = {
                IconButton(onClick = {
                    val fullBins = binList.filter { it.fillLevel > 80 }
                    if (fullBins.isNotEmpty()) {
                        sendNotification(currentContext, "Kritik Doluluk!", "${fullBins.size} adet kutu %80'i geçti.")
                    }
                }) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFF2E7D32))
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {
            item { SummaryCard(fullCount = binList.count { it.fillLevel > 80 }) }

            // --- BİNA SEÇİCİ (Liquid Scroll Uygulandı) ---
            item {
                Text("Bölgeler", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    items(buildingList) { buildingName ->
                        val isSelected = selectedBuilding == buildingName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBuilding = buildingName },
                            label = { Text(buildingName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // --- ANLIK DURUM BAŞLIĞI VE DROPDOWN ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Anlık Durum", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Box {
                        Surface(
                            onClick = { isMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = selectedStatusFilter.label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                        ) {
                            FilterType.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            filter.label,
                                            color = if (selectedStatusFilter == filter) Color(0xFF2E7D32) else Color.Black,
                                            fontWeight = if (selectedStatusFilter == filter) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedStatusFilter = filter
                                        isMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedStatusFilter == filter) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- LİSTE VE BOŞ DURUM ---
            if (filteredBins.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedStatusFilter == FilterType.CRITICAL) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (selectedStatusFilter == FilterType.CRITICAL) Color(0xFF2E7D32) else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedStatusFilter) {
                                    FilterType.CRITICAL -> "Harika! Şu an boşaltılması gereken \nkritik düzeyde dolu kutu yok."
                                    else -> "Bu filtreye uygun kutu bulunamadı."
                                },
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredBins) { bin -> WasteBinListItem(bin) }
            }
        }
    }


}

@Composable
fun SummaryCard(fullCount: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kritik Durum", color = Color.Gray, fontSize = 13.sp)
                Text(if (fullCount > 0) "$fullCount Kutu Dolu!" else "Her Şey Temiz", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (fullCount > 0) Color(0xFFEF5350) else Color(0xFF2E7D32))
            }
            Button(onClick = {}, enabled = fullCount > 0, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("Rota Çiz") }
        }
    }
}

@Composable
fun WasteBinListItem(bin: BinData) {
    val (statusColor, bgColor) = when {
        bin.fillLevel > 80 -> Color(0xFFEF5350) to Color(0xFFFFEBEE)
        bin.fillLevel >= 50 -> Color(0xFFFFB300) to Color(0xFFFFF8E1)
        else -> Color(0xFF66BB6A) to Color(0xFFE8F5E9)
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).background(bgColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Delete, null, tint = statusColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bin.locationName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if(bin.floor == "0") {
                    Text("${bin.building} - Zemin Kat | ${bin.type}", color = Color.Gray, fontSize = 12.sp)
                } else {
                    Text("${bin.building} - ${bin.floor}. Kat | ${bin.type}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("%${bin.fillLevel}", fontWeight = FontWeight.Bold, color = statusColor, fontSize = 14.sp)
                LinearProgressIndicator(progress = { bin.fillLevel / 100f }, modifier = Modifier.width(50.dp).height(5.dp), color = statusColor, trackColor = Color(0xFFF0F0F0), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
        }
    }
}

// --- BİLDİRİM MANTIĞI ---
fun sendNotification(context: Context, title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

    // 1. Bildirime tıklandığında hangi aktivitenin açılacağını belirliyoruz
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // 2. Android sistemine bu intent'i kullanma yetkisi veriyoruz
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE // Modern Android sürümleri için gerekli güvenlik bayrağı
    )

    val builder = NotificationCompat.Builder(context, "WASTE_ALERTS")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true) // Kullanıcı tıklayınca bildirim otomatik silinsin
        .setContentIntent(pendingIntent) // İşte tıklama aksiyonu burada bağlanıyor

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(System.currentTimeMillis().hashCode(), builder.build())
}

// --- DİĞER EKRANLAR ---
@Composable fun MapScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Harita Paneli Yakında")
        }
    }
}
@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Orta kısımdaki içerik (Şimdilik boş duruyor)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Profil Paneli Yakında", color = Color.Gray)
        }

        // --- VERSİYON YAZISI (En Alt Orta) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 85.dp), // Navigasyon barın üstünde kalması için pay bıraktık
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Versiyon 1.0.0-beta",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                text = "Kampüs Atık Yönetim Sistemi for MDB308",
                fontSize = 10.sp,
                color = Color.LightGray.copy(alpha = 0.9f)
            )
        }
    }
}