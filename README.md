# 🍃 Akıllı Kampüs Atık Yönetim Sistemi

CampusFlow, üniversite kampüslerindeki katı atık yönetim süreçlerini optimize etmek, iş gücü verimliliğini artırmak ve karbon emisyonunu azaltmak amacıyla geliştirilmiş **uçtan uca (End-to-End) bir IoT ve Mobil/Web ekosistemidir.**

Proje; çöplerin doluluk oranını anlık ölçen bir **IoT donanım düğümü**, verileri asenkron işleyen bir **bulut veritabanı**, saha personeli için geliştirilmiş modern bir **Android mobil uygulaması** ve idari yönetim için bir **Web paneli** içermektedir.

---

## 🚀 Öne Çıkan Özellikler

* 🔄 **Gerçek Zamanlı Veri Senkronizasyonu:** Donanımdan gelen doluluk verileri sayfayı yenilemeye gerek kalmadan (Live Update) anlık olarak arayüze yansır.
* 🗺️ **Google Maps Entegrasyonu:** Atık kutularının kampüs içindeki konumları harita üzerinde dinamik pinlerle gösterilir.
* 🚦 **Akıllı Filtreleme Mekanizması:** Kutular doluluk oranlarına göre `%80 Üzeri (Kritik)`, `%50 - %80 Arası (Uyarı)` ve `%50 Altı (Güvenli)` olarak filtrelenerek operasyon rotası optimize edilir.
* 🔒 **Rol Tabanlı Giriş Sistemi:** Sadece sistemde tanımlı operasyon personellerinin (`workers`) giriş yapabileceği güvenli yetkilendirme altyapısı.
* 🌗 **Modern UI & Karanlık Mod:** Jetpack Compose ile geliştirilmiş, sistem tercihlerine duyarlı Açık/Koyu tema desteği.

---

## 🛠️ Kullanılan Teknolojiler & Bileşenler

### Mobil & Web Yazılım Katmanı

* **Dil:** Kotlin, PHP, JavaScript
* **UI Framework:** Jetpack Compose (Modern Declarative UI)
* **Mimari:** Layered Architecture (Katmanlı Mimari) & State Management
* **Veritabanı (Bağlantı):** Firebase Realtime Database (NoSQL)
* **Harita Servisi:** Google Maps API

### Donanım Katmanı (IoT)

* **Mikrodenetleyici:** ESP32 (Wi-Fi Entegreli)
* **Sensör:** HC-SR04 Ultrasonik Mesafe Sensörü
* **Bağlantı Protokolü:** HTTP PATCH (Veri trafiğini minimize etmek amacıyla sadece değişen alanı günceller)
* **Simülasyon Ortamı:** Wokwi

---

## 📁 Proje Klasör Yapısı (Mobil)

Mobil uygulama, kurumsal standartlara uygun olarak paket bazlı (feature/package-based) mimariyle tasarlanmıştır:

```text
app/src/main/java/com/kampus/kampusatikyonetimsistemi/
│
├── model/          # Veri modelleri (BinData, WorkerData, vb.)
├── service/        # Firebase Messaging ve arka plan servisleri
└── ui/
    ├── components/ # Tekrar kullanılabilir arayüz elemanları (StatCard, vb.)
    └── screens/    # Uygulama ekranları (Login, Dashboard, Map, Profile)
```
---

### 🔧 Kurulum ve Çalıştırma

#### 1.Donanım(Simülasyon) Kurulumu
1. `diagram.json` ve ESP32 kaynak kodlarını **Wokwi** üzerinden açın.
2. Firebase Realtime Database URL'nizi koda entegre edin:
```text
````cpp`
String firebaseUrl = "https://kampusatikyonetimsistemi-dca93-default-rtdb.europe-west1.firebasedatabase.app/bins/bin_1.json";
```
3. Simülasyonu başlatın. Sensör üzerindeki mesafe çubuğunu oynatarak veri akışını test edin.

#### 2.Mobil Uygulama Kurulumu
1. Bu repoyu bilgisayarınıza klonlayın:
```text
````bash`
     git clone [https://github.com/akarsli/Kampus-Atik-Yonetim-Sistemi.git](https://github.com/akarsli/Kampus-Atik-Yonetim-Sistemi.git)
```

2. Projeyi **Android Studio** ile açın
3. Firebase konsolunuzdan indirdiğiniz `google-services.json` dosyasını `app/` dizini altına yerleştirin.
4. Projeyi derleyin **(Build -> Rebuild Project)** ve bir emülatör veya fiziksel cihaz üzerinden çalıştırın.
---
### 📊 Mühendislik ve Küresel Etkiler

* **Ekonomik:** Çöp toplama araçlarının gereksiz istasyonları ziyaret etmesini engelleyerek yakıt maliyetlerinde minimum %35 tasarruf sağlar.
* **Çevresel (Sürdürülebilirlik):** Optimize edilen rotalar sayesinde karbon emisyonunu ($CO_2$ salınımı) düşürür ve taşmaları engelleyerek çevre sağlığını korur.
* **Sosyal & Sağlık:** Kampüs sınırları içinde koku, bakteri ve haşere oluşumunu engelleyerek toplum sağlığına doğrudan katkıda bulunur.
* **👷‍♂️ Geliştiriciler:** Abdulkadir Karslı, Serkan Lafçı
* **🎓 Ders:** MDB308 - Çok Disiplinli Takım Projesi