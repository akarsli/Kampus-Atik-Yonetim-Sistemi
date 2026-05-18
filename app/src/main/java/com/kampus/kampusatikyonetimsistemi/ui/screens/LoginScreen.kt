package com.kampus.kampusatikyonetimsistemi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kampus.kampusatikyonetimsistemi.model.WorkerData

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, isDarkMode: Boolean) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- LOGO PANELİ ---
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE8F5E9),
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(45.dp),
                    tint = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Kampüs Atık Yönetim Sistemi'ne Giriş Yap",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Operasyon personeli giriş paneli",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- KULLANICI ADI GİRİŞİ ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = null },
                label = { Text("Kullanıcı Adı") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- ŞİFRE GİRİŞİ ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Şifre") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32)
                )
            )

            // --- HATA MESAJI ---
            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- GİRİŞ YAP BUTONU ---
            Button(
                onClick = {
                    if (username.isEmpty() || password.isEmpty()) {
                        errorMessage = "Lütfen tüm alanları doldurun."
                        return@Button
                    }

                    isLoading = true

                    // Doğrudan Realtime Database üzerindeki workers düğümüne gidiyoruz
                    val database = FirebaseDatabase.getInstance("https://kampusatikyonetimsistemi-dca93-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("workers")

                    // Database'de girilen kullanıcı adına eşit olan personeli aratıyoruz
                    database.orderByChild("username").equalTo(username)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                isLoading = false
                                if (snapshot.exists()) {
                                    var isPasswordCorrect = false
                                    var loggedInWorkerName = ""

                                    // Eşleşen kullanıcıları dönüyoruz (Zaten bir tane olmalı)
                                    for (workerSnapshot in snapshot.children) {
                                        val worker = workerSnapshot.getValue(WorkerData::class.java)
                                        if (worker != null && worker.password == password) {
                                            isPasswordCorrect = true
                                            loggedInWorkerName = worker.fullName // İsmini aldık
                                            break
                                        }
                                    }

                                    if (isPasswordCorrect) {
                                        onLoginSuccess(loggedInWorkerName) // İsmi MainActivity'ye paslar ve içeri alır
                                    } else {
                                        errorMessage = "Hatalı şifre girdiniz."
                                    }
                                } else {
                                    errorMessage = "Böyle bir çalışan bulunamadı."
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                isLoading = false
                                errorMessage = "Veritabanı hatası: ${error.message}"
                            }
                        })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}