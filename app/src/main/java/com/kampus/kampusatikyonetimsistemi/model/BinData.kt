package com.kampus.kampusatikyonetimsistemi.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

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