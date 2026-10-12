# FitnessTracker

Android aplikacija za praćenje fizičkih aktivnosti (trčanje, hodanje, biciklizam) pomoću GPS-a. Aplikacija bilježi rutu, udaljenost, trajanje i brzinu, čuva istoriju aktivnosti lokalno, prikazuje statistiku i omogućava postavljanje ličnih ciljeva.

## Funkcionalnosti

- **Praćenje aktivnosti u realnom vremenu** – GPS praćenje u foreground servisu, tako da radi i dok je aplikacija u pozadini.
- **Tipovi aktivnosti** – trčanje, hodanje, biciklizam i ostalo.
- **Istorija** – lista sačuvanih aktivnosti uz pretragu, filtriranje (tip, minimalno trajanje, period) i pull-to-refresh.
- **Detalji aktivnosti** – ruta prikazana na Google mapi, trajanje, udaljenost, prosječna i maksimalna brzina.
- **Statistika** – grafikon pređene udaljenosti u posljednjih 7 dana i broj aktivnosti po tipu.
- **Ciljevi** – dnevni ili sedmični ciljevi po udaljenosti ili trajanju, za određeni tip aktivnosti ili za sve.
- **Podsjetnici** – notifikacija ako korisnik nije bio aktivan 24 sata.
- **Podešavanja** – jezik (srpski / engleski), jedinice mjere (kilometri / milje) i uključivanje podsjetnika.
- **Prilagođeni layouti** – posebni rasporedi za landscape orijentaciju i tablete.

## Tehnologije

| Oblast | Biblioteka |
|---|---|
| Jezik | Kotlin |
| UI | Fragmenti, ViewBinding, Material Components, Navigation Component |
| Arhitektura | MVVM (ViewModel, LiveData), Repository pattern |
| Baza podataka | Room (SQLite) |
| Asinhronost | Kotlin Coroutines |
| Lokacija i mapa | Google Play Services Location, Google Maps SDK |
| Pozadinski poslovi | WorkManager |
| Grafikoni | MPAndroidChart |
| Podešavanja | AndroidX Preference |
| Serijalizacija | Gson |

## Struktura projekta

```
app/src/main/java/com/example/fitnesstracker/
├── data/
│   ├── local/          # Room entiteti, DAO-ovi, baza, konverteri
│   └── repository/     # Repozitoriji za aktivnosti i ciljeve
├── notifications/      # Notifikacije i ReminderWorker
├── tracking/           # Foreground servis za GPS praćenje
└── ui/
    ├── home/           # Početni ekran (start/stop aktivnosti)
    ├── history/        # Istorija i filteri
    ├── detail/         # Detalji aktivnosti i mapa rute
    ├── stats/          # Statistika i grafikoni
    ├── goals/          # Ciljevi
    └── settings/       # Podešavanja, jezik, jedinice
```

## Pokretanje projekta

### Preduslovi

- Android Studio (novija verzija sa podrškom za AGP 9.x)
- JDK 21 (Gradle ga može preuzeti automatski)
- Android SDK 36
- Uređaj ili emulator sa Android 7.0 (API 24) ili novijim
- **Google Maps API ključ**

### Koraci

1. Kloniraj repozitorijum:
```bash
   git clone https://github.com/DajanaPopovic/FitnessTracker.git
```
2. Otvori projekat u Android Studiju.
3. Nabavi Maps API ključ u [Google Cloud Console](https://console.cloud.google.com/) (uključi **Maps SDK for Android**).
4. U korijenskom folderu projekta napravi fajl `local.properties` (Android Studio ga obično napravi sam) i dodaj ključ:
```properties
   sdk.dir=PUTANJA/DO/ANDROID/SDK
   MAPS_API_KEY=tvoj_api_kljuc
```
   > Fajl `local.properties` je u `.gitignore` i ne smije se objavljivati na GitHub.
5. Sinhronizuj Gradle i pokreni aplikaciju (**Run ▶**).

Bez ispravnog ključa aplikacija radi, ali se mapa rute neće prikazati.

## Dozvole

| Dozvola | Razlog |
|---|---|
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Praćenje rute i udaljenosti |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION` | Praćenje dok je aplikacija u pozadini |
| `POST_NOTIFICATIONS` | Notifikacija servisa i podsjetnici |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Učitavanje Google mape |
| `RECEIVE_BOOT_COMPLETED` | Pouzdano zakazivanje podsjetnika nakon restarta uređaja |

## Podaci i privatnost

Sve aktivnosti i ciljevi čuvaju se isključivo lokalno na uređaju (Room baza). Aplikacija ne šalje podatke na server.

## Autor

**Dajana Popović** – [GitHub profil](https://github.com/DajanaPopovic)
