package com.kampus.kampusatikyonetimsistemi.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kampus.kampusatikyonetimsistemi.model.BinData
import com.kampus.kampusatikyonetimsistemi.model.FilterType
import com.kampus.kampusatikyonetimsistemi.ui.components.SummaryCard
import com.kampus.kampusatikyonetimsistemi.ui.components.WasteBinListItem
import com.kampus.kampusatikyonetimsistemi.util.sendNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(binList: List<BinData>) {
    val currentContext = LocalContext.current
    val internalBinList = remember { mutableStateListOf<BinData>() }

    var selectedStatusFilter by remember { mutableStateOf(FilterType.CRITICAL) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val buildingList = listOf("Tümü", "Kütüphane", "Yemekhane", "B Blok", "C Blok")
    var selectedBuilding by remember { mutableStateOf("Tümü") }

    val sharedPrefs = remember {
        currentContext.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
    }

    var lastNotificationTime by remember {
        mutableLongStateOf(sharedPrefs.getLong("last_time", 0L))
    }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance("https://kampusatikyonetimsistemi-dca93-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("bins")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                internalBinList.clear()
                var hasCriticalBin = false

                for (binSnapshot in snapshot.children) {
                    val bin = binSnapshot.getValue(BinData::class.java)
                    if (bin != null) {
                        internalBinList.add(bin)
                        if (bin.fillLevel >= 80) hasCriticalBin = true
                    }
                }

                if (hasCriticalBin) {
                    val currentTime = System.currentTimeMillis()
                    val fiveMinutesInMillis = 5 * 60 * 1000
                    val isNotificationEnabled = sharedPrefs.getBoolean("notifications_enabled", true)

                    if (currentTime - lastNotificationTime > fiveMinutesInMillis) {
                        if (isNotificationEnabled) {
                            sendNotification(currentContext, "Kampüs Akıllı Takip", "Bazı çöp kutuları doldu! Lütfen kontrol edin.")
                            lastNotificationTime = currentTime
                            sharedPrefs.edit().putLong("last_time", currentTime).apply()
                        }
                    }
                } else {
                    lastNotificationTime = 0L
                    sharedPrefs.edit().putLong("last_time", 0L).apply()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filteredBins = internalBinList.filter { bin ->
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
                    val fullBins = internalBinList.filter { it.fillLevel > 80 }
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
            item { SummaryCard(fullCount = internalBinList.count { it.fillLevel > 80 }) }

            item {
                Text("Bölgeler", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(buildingList) { buildingName ->
                        FilterChip(
                            selected = selectedBuilding == buildingName,
                            onClick = { selectedBuilding = buildingName },
                            label = { Text(buildingName) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Anlık Durum", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Box {
                        Surface(onClick = { isMenuExpanded = true }, shape = RoundedCornerShape(12.dp), color = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = selectedStatusFilter.label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Icon(if (isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                            FilterType.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.label) },
                                    onClick = { selectedStatusFilter = filter; isMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredBins.isEmpty()) {
                item {
                    Text("Uygun kutu bulunamadı.", modifier = Modifier.padding(vertical = 40.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                }
            } else {
                items(filteredBins) { bin -> WasteBinListItem(bin) }
            }
        }
    }
}