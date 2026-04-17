package com.kampus.kampusatikyonetimsistemi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kampus.kampusatikyonetimsistemi.model.BinData
import com.kampus.kampusatikyonetimsistemi.ui.components.WasteBinListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(binList: List<BinData>) {
    val campusCenter = LatLng(40.9953019, 29.0692195)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusCenter, 17f)
    }

    var selectedBuildingName by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val buildingLocations = mapOf(
        "Kütüphane" to LatLng(40.995030, 29.070114),
        "B Blok" to LatLng(40.995764, 29.069776),
        "Yemekhane" to LatLng(40.995278, 29.068100),
        "C Blok" to LatLng(40.995733, 29.070779)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            buildingLocations.forEach { (name, coords) ->
                val binsInThisBuilding = binList.filter { it.building == name }
                val markerColor = if (binsInThisBuilding.any { it.fillLevel > 80 }) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN

                Marker(
                    state = rememberMarkerState(position = coords),
                    title = name,
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                    onClick = {
                        selectedBuildingName = name
                        showSheet = true
                        true
                    }
                )
            }
        }

        if (showSheet && selectedBuildingName != null) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState, containerColor = Color.White) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp, start = 20.dp, end = 20.dp)) {
                    Text(text = "$selectedBuildingName Detayları", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(16.dp))
                    binList.filter { it.building == selectedBuildingName }.forEach { bin ->
                        WasteBinListItem(bin)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}