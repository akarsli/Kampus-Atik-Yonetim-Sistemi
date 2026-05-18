package com.kampus.kampusatikyonetimsistemi.ui.screens

import android.R
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampus.kampusatikyonetimsistemi.model.BinData
import com.kampus.kampusatikyonetimsistemi.model.WorkerData

@Composable
fun ProfileScreen(binList: List<BinData>, userFullName: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE) }

    var isNotificationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true)) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(sharedPrefs.getBoolean("dark_mode", false)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Profil İkonu ve İsim
        Box(modifier = Modifier.size(100.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(35.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color(0xFF2E7D32))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(userFullName, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        // Ayarlar Bölümü
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ayarlar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Bildirimler", fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = {
                        isNotificationEnabled = it
                        sharedPrefs.edit().putBoolean("notifications_enabled", it).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2E7D32),
                        checkedTrackColor = Color(0xFFE8F5E9),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD),
                        checkedBorderColor = Color(0xFF2E7D32)
                    )
                )
            }
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Uygulama Tercihleri", onClick = { showThemeDialog = true })
            ProfileMenuItem(icon = Icons.Default.Info, label = "Sistem Durumu")
            ProfileMenuItem(icon = Icons.Default.ExitToApp, label = "Çıkış Yap", isLast = true, onClick = { onLogout() })
        }

        // --- DEĞİŞİKLİK BURADA: Üst tarafı aşağıdan ayırır ---
        Spacer(modifier = Modifier.weight(1f))

        // Versiyon Bilgileri (Navigasyon barın hemen üstünde duracak)
        Text(
            text = "Versiyon 1.1.0",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = "Kampüs Atık Yönetim Sistemi for MDB308",
            fontSize = 10.sp,
            color = Color.LightGray.copy(alpha = 0.9f)
        )

        // Navigasyon bar ile arasında biraz boşluk bırakmak için (Liquid barın yüksekliğine göre ayarlı)
        Spacer(modifier = Modifier.height(85.dp))
    }

    // Tema Seçim Dialogu
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema Seçimi") },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { isDarkMode = false; sharedPrefs.edit().putBoolean("dark_mode", false).apply(); showThemeDialog = false }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isDarkMode, onClick = null)
                        Text("Açık Tema", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth().clickable { isDarkMode = true; sharedPrefs.edit().putBoolean("dark_mode", true).apply(); showThemeDialog = false }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isDarkMode, onClick = null)
                        Text("Koyu Tema", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Kapat") } }
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, isLast: Boolean = false, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (isLast) Color.Red else Color.DarkGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, color = if (isLast) Color.Red else Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        if (!isLast) Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
    }
}