@file:Suppress("DEPRECATION")

package com.kampus.kampusatikyonetimsistemi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kampus.kampusatikyonetimsistemi.model.BinData
import com.kampus.kampusatikyonetimsistemi.model.Screen
import com.kampus.kampusatikyonetimsistemi.ui.screens.DashboardContent
import com.kampus.kampusatikyonetimsistemi.ui.screens.MapScreen
import com.kampus.kampusatikyonetimsistemi.ui.screens.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        askNotificationPermission()

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Atık Takip Kanalı"
            val channel = NotificationChannel("WASTE_ALERTS", name, NotificationManager.IMPORTANCE_HIGH)
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Dashboard, Screen.Map, Screen.Profile)
    val binList = remember { mutableStateListOf<BinData>() }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance("https://kampusatikyonetimsistemi-dca93-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("bins")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binList.clear()
                for (binSnapshot in snapshot.children) {
                    val bin = binSnapshot.getValue(BinData::class.java)
                    if (bin != null) binList.add(bin)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally { 1000 } + fadeIn() },
            exitTransition = { slideOutHorizontally { -1000 } + fadeOut() }
        ) {
            composable(Screen.Dashboard.route) { DashboardContent(binList = binList) }
            composable(Screen.Map.route) { MapScreen(binList = binList) }
            composable(Screen.Profile.route) { ProfileScreen(binList = binList) }
        }

        // Liquid Glass Bar
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 40.dp, end = 40.dp)) {
            Surface(modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(32.dp), color = Color.White.copy(alpha = 0.95f), shadowElevation = 10.dp) {
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