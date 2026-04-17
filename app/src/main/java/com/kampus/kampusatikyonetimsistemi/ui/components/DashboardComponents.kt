package com.kampus.kampusatikyonetimsistemi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampus.kampusatikyonetimsistemi.model.BinData

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
                LinearProgressIndicator(progress = { bin.fillLevel / 100f }, modifier = Modifier.width(50.dp).height(5.dp), color = statusColor, trackColor = Color(0xFFF0F0F0), strokeCap = StrokeCap.Round)
            }
        }
    }
}