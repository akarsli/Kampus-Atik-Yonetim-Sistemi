package com.kampus.kampusatikyonetimsistemi.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kampus.kampusatikyonetimsistemi.util.sendNotification

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Buluttan bir mesaj geldiğinde burası tetiklenir (Uygulama kapalı olsa bile)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            // Daha önce yazdığımız sendNotification fonksiyonunu çağırıyoruz
            // Bu fonksiyon util/NotificationUtils.kt içinde olduğu için otomatik tanınacaktır
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
        // Bu token'ı veritabanına kaydederek belirli kullanıcılara hedefli bildirimler atabilirsin
    }
}