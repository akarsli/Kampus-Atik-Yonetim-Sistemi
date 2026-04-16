package com.kampus.kampusatikyonetimsistemi

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Buluttan bir mesaj geldiğinde burası tetiklenir (Uygulama kapalı olsa bile)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            // Daha önce yazdığımız sendNotification fonksiyonunu çağırıyoruz
            sendNotification(
                applicationContext,
                it.title ?: "Kritik Durum",
                it.body ?: "Bir kutu doluluk limitini aştı!"
            )
        }
    }

    // Cihaza özel yeni bir token oluştuğunda (Firebase ile cihazı eşlemek için)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Bu token'ı ileride belirli bir çalışana bildirim atmak için kullanabiliriz
    }
}