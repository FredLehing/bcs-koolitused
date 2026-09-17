# Backendi kaustade struktuur

```
backend/
├── CLAUDE.md                                       # Backendi juhised Claude Code'ile (Spring Boot, Java)
├── build.gradle                                    # Gradle build-konfiguratsioon ja sõltuvused
├── settings.gradle                                 # Gradle projekti seadistus
├── gradlew, gradlew.bat                            # Gradle wrapper käivitusskriptid
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ee/minuprojekt/                          # Baaspakett
│   │   │       ├── MinuprojektApplication.java         # Rakenduse põhiklass (entry point)
│   │   │       ├── Error.java                           # Veakoodide enum
│   │   │       ├── Status.java                           # Staatuste enum
│   │   │       ├── controller/                          # REST kontrollerid
│   │   │       │   ├── common/                          # Mitme ressursi vahel jagatud kontrollerikihi kood
│   │   │       │   │   └── dto/                         # Mitme ressursipaketi vahel jagatud DTO-d
│   │   │       │   │       └── SomeSharedDto.java        # Jagatud DTO klass (nt SharedEntityDto.java)
│   │   │       │   └── ressursipakett/                  # Kontrolleri alampakett (nt üks domeenala kohta)
│   │   │       │       ├── SomeController.java          # REST kontrolleri klass (nt EntityController.java)
│   │   │       │       └── dto/                         # Selle ressursi enda DTO-d
│   │   │       │           └── SomeDto.java              # DTO klass (nt EntityDto.java)
│   │   │       ├── infrastructure/                      # Ühine infrastruktuur (veavastused, erandid, abiklassid)
│   │   │       │   ├── RestExceptionHandler.java         # Globaalne API veakäsitleja
│   │   │       │   ├── error/                            # Veavastuse mudel
│   │   │       │   │   └── ApiError.java                 # Standardne API veavastuse objekt
│   │   │       │   ├── exception/                        # Kohandatud erindiklassid
│   │   │       │   │   ├── DataNotFoundException.java    # 404 – andmeid ei leitud
│   │   │       │   │   ├── ForbiddenException.java       # 403 – juurdepääs keelatud
│   │   │       │   │   └── PrimaryKeyNotFoundException.java # Primaarvõtit ei leitud
│   │   │       │   └── util/                             # Üldotstarbelised abiklassid
│   │   │       │       └── StringBytesConverter.java     # Baidimassiivi/stringi teisendaja
│   │   │       ├── persistence/                          # Andmebaasi entiteedid, mapperid, repositooriumid
│   │   │       │   └── entitypakett/                     # Entiteedi alampakett (nt üks domeenala kohta)
│   │   │       │       ├── Entity.java                   # Entiteedi klass
│   │   │       │       ├── EntityMapper.java              # MapStruct mapperi liides
│   │   │       │       └── EntityRepository.java          # Spring Data repositooriumi liides
│   │   │       └── service/                              # Äriloogika teenused
│   │   │           └── SomeService.java                  # Teenuse klass (nt EntityService.java)
│   │   ├── generated/                                    # MapStruct genereeritud mapper-implementatsioonid (build-ajal, ei muudeta käsitsi)
│   │   │   └── ee/minuprojekt/persistence/...
│   │   └── resources/
│   │       └── application.properties                    # Rakenduse konfiguratsioon (server, andmebaas, logimine)
│   └── test/
│       └── java/
│           └── ee/minuprojekt/                          # Ühik- ja integratsioonitestid
└── docs/database/                                        # SQL skriptid skeemi loomiseks ja andmete importimiseks (projekti juurkaustas)
```

## Lühikirjeldused

| Kaust/fail | Eesmärk |
|------------|---------|
| `CLAUDE.md` | Backendi juhised Claude Code'ile — Spring Boot, Java konventsioonid |
| `build.gradle` / `settings.gradle` | Gradle build ja sõltuvuste haldus |
| `gradle/`, `gradlew` | Gradle wrapper — projekti käivitamiseks ilma eraldi Gradle'i paigalduseta |
| `src/main/java/.../MinuprojektApplication.java` | Spring Boot rakenduse käivitusklass |
| `src/main/java/.../Error.java`, `Status.java` | Üldised enum-tüübid, mida kasutatakse läbi rakenduse |
| `src/main/java/.../controller/` | REST kontrollerid — võtavad HTTP päringud vastu ja tagastavad vastused |
| `src/main/java/.../controller/common/dto/` | Mitme ressursipaketi vahel jagatud DTO klassid (nt kasutusel mitmes kontrolleris/mapperis) |
| `src/main/java/.../controller/.../dto/` | Konkreetse ressursi enda DTO klassid — andmekuju päringute ja vastuste jaoks |
| `src/main/java/.../infrastructure/` | Globaalne veahaldus, kohandatud erindid, abiklassid |
| `src/main/java/.../infrastructure/error/` | Standardse API veavastuse struktuur |
| `src/main/java/.../infrastructure/exception/` | Kohandatud erindiklassid (404, 403 jms) |
| `src/main/java/.../infrastructure/util/` | Üldotstarbelised abiklassid (nt tüübiteisendused) |
| `src/main/java/.../persistence/` | JPA entiteedid, MapStruct mapperid ja Spring Data repositooriumid |
| `src/main/java/.../service/` | Äriloogika — töötleb andmeid kontrolleri ja andmebaasi vahel |
| `src/main/generated/` | Build-ajal automaatselt genereeritud MapStruct mapper-implementatsioonid — ei muudeta käsitsi |
| `src/main/resources/application.properties` | Rakenduse seadistused (port, andmebaas, logimine) |
| `src/test/` | Ühik- ja integratsioonitestid |
| `docs/database/` | SQL skriptid andmebaasi skeemi loomiseks ja andmete importimiseks (vt projekti juur-CLAUDE.md) |
