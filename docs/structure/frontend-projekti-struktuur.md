# Projekti kaustade struktuur

```
projekti-nimi/
├── CLAUDE.md                           # Üldised juhised Claude Code'ile (projekti tase)
├── database/                           # SQL skriptid
│   ├── 1_reset_database.sql            # Skeemi kustutamine ja taasloomine
│   ├── 2_create.sql                    # Tabelite ja seoste loomine
│   └── 3_import.sql                    # Algsete andmete import
├── backend/                            # Serveri lähtekood (Spring Boot)
│   ├── CLAUDE.md                       # Backendi juhised Claude Code'ile (Spring Boot, Java)
│   ├── gradle/                         # Gradle wrapper failid
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ee.minuprojekt/                  # Baaspakett
│   │   │   │       ├── controller/                 # REST kontrollerid
│   │   │   │       │   └── ressursipakett/         # Kontrolleri alampakett (nt user)
│   │   │   │       │       ├── dto/                # Andmeedastuse objektid (DTO-d)
│   │   │   │       │       └── SomeController.java # REST kontroller (nt UserController.java)
│   │   │   │       ├── infrastructure/             # Ühine infrastruktuur
│   │   │   │       │   ├── error/                  # Veavastuse mudel
│   │   │   │       │   └── exception/              # Kohandatud erindiklassid
│   │   │   │       ├── persistence/                # Andmebaasi entiteedid ja repositooriumid
│   │   │   │       │   └── ressursipakett/         # Entiteedi alampakett (nt user)
│   │   │   │       │       ├── Entity.java         # Entiteedi klass (nt User.java)
│   │   │   │       │       ├── EntityMapper.java   # Mapperi liides
│   │   │   │       │       └── EntityRepository.java # Repositooriumi liides
│   │   │   │       └── service/                    # Äriloogika teenused
│   │   │   └── resources/                          # Rakenduse konfiguratsioon
│   │   └── test/                                   # Ühik- ja integratsioonitestid
│   └── [konfiguratsioonifailid]                    # build.gradle, settings.gradle, gradlew jms
│
├── docs/                               # Dokumentatsioon ja õppematerjalid
│   └── tasks/                          # Ülesannete kirjeldused
│
└── frontend/                   # Kliendipoolne rakendus
    ├── CLAUDE.md               # Frontendi juhised Claude Code'ile (Vue 3, Vite)
    ├── public/                 # Avalikud staatilised failid (kopeeritakse buildi)
    └── src/                    # Rakenduse lähtekood
        ├── api-services/       # Axios API päringute teenused
        ├── assets/             # Staatilised ressursid (pildid, fondid jms)
        ├── auth/               # Autentimise loogika ja abifunktsioonid
        ├── components/         # Korduvkasutatavad Vue komponendid
        │   ├── common/         # Üldkasutatavad elemendid (nupud, sildid, laadijad)
        │   ├── forms/          # Vormi komponendid (sisendid, validatsioon)
        │   ├── modals/         # Modaalakende komponendid
        │   └── tables/         # Tabelite komponendid
        ├── navigation/         # Navigatsiooni komponendid
        ├── router/             # Vue Router marsruutide konfiguratsioon
        └── views/              # Lehekülgede komponendid (marsruutidega seotud)
```

## Lühikirjeldused

| Kaust | Eesmärk |
|-------|---------|
| `CLAUDE.md` | Üldised juhised Claude Code'ile — projekti struktuur, reeglid, käsud |
| `backend/` | Spring Boot rakendus — REST API, äriloogika, andmebaas |
| `backend/CLAUDE.md` | Backendi juhised Claude Code'ile — Spring Boot, Java konventsioonid |
| `backend/src/main/java/.../controller/` | REST kontrollerid — võtavad päringud vastu ja tagastavad vastused |
| `backend/src/main/java/.../controller/.../dto/` | DTO klassid — andmekuju päringute ja vastuste jaoks |
| `backend/src/main/java/.../infrastructure/` | Globaalne veahaldus, erindid, API veavormingud |
| `backend/src/main/java/.../persistence/` | Entiteedid, mapperid ja repositooriumid andmebaasiga suhtlemiseks |
| `backend/src/main/java/.../service/` | Äriloogika — töötleb andmeid kontrolleri ja andmebaasi vahel |
| `backend/src/main/resources/` | Rakenduse seadistused (port, andmebaas, logimine) |
| `backend/src/test/` | Ühik- ja integratsioonitestid |
| `database/` | SQL skriptid skeemi loomiseks ja andmete importimiseks |
| `docs/` | Kõik õppe- ja projektidokumendid — ainult eesti keeles |
| `docs/tasks/` | Ülesannete kirjeldused õpilastele |
| `frontend/` | Vue 3 rakendus, mida kasutaja brauseris näeb |
| `frontend/CLAUDE.md` | Frontendi juhised Claude Code'ile — Vue 3, Vite, koodistiil |
| `frontend/src/api-services/` | Kõik HTTP päringud backendiga — üks fail ressursi kohta |
| `frontend/src/auth/` | Sisselogimise kontroll, token'i haldus, route-kaitse |
| `frontend/src/components/` | Korduvkasutatavad Vue komponendid, jaotatud alltüüpide kaupa |
| `frontend/src/components/common/` | Üldised UI-elemendid mida kasutatakse kogu rakenduses |
| `frontend/src/components/forms/` | Vormi sisend- ja validatsiooniloogikaga seotud komponendid |
| `frontend/src/components/modals/` | Modaalakende komponendid |
| `frontend/src/components/tables/` | Tabelite kuvamiseks mõeldud komponendid |
| `frontend/src/navigation/` | Navigatsiooniriba ja menüü komponendid |
| `frontend/src/router/` | URL-ide ja vaadete vahelised seosed |
| `frontend/src/views/` | Täislehed, mida router kuvab — kasutavad komponente |
