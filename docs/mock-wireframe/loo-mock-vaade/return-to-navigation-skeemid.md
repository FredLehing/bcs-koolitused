# Tagasitee navigatsioon — skeemid

Kõigi avamiskohtade ja erandite [kaardistus](../../tasks/frontend/return-to-navigation.md).
Ühine frontendi komponent on `BackLink.vue`; läbimängudes sama lepingut kirjeldav
`return-navigation.js`. Peamenüü ja vahelehed ei loo tagasiteed. `/training` vaate „Järgmised koolitused“
kaardi lingid avavad `/course?courseId={id}` ilma `returnTo`-ta; sealne „Tagasi“
kasutab varusihti `/courses`.

```mermaid
sequenceDiagram
    actor Kasutaja
    participant Kalender as /courses
    participant Course as /course
    participant Lecturer as /lecturer
    Kasutaja->>Kalender: Vaata lähemalt
    Kalender->>Course: courseId=10, returnTo=/courses
    Kasutaja->>Course: Vajuta koolitaja kaardile
    Course->>Lecturer: lecturerId=8, returnTo=course.fullPath
    Kasutaja->>Lecturer: Tagasi
    Lecturer->>Course: Taasta kogu course.fullPath
    Kasutaja->>Course: Tagasi
    Course->>Kalender: /courses
```

```mermaid
flowchart TD
    A[Detaili või vormi avamine] --> B[returnTo = lähtevaate fullPath]
    B --> C[Router kodeerib query väärtuse]
    C --> D[Sihtvaates link: Tagasi]
    D --> E{Sisemine olemasolev rada ja erinev praegusest?}
    E -->|Jah| F[Link returnTo väärtusele]
    E -->|Ei või puudub| G[Vaate varusihtkoht]
    H[Vormi oleku või tõlke vahetus] --> I[Säilita senine returnTo router.replace ajal]
    J[Toimumiskorra salvestamine] --> K[Tagasi lähtevaatesse koos eduteatega]
    L[Toimumiskorra kustutamine] --> M{Kas tagasitee on kustutatud detail?}
    M -->|Jah| N[Koolituse kalender]
    M -->|Ei| K
```

```mermaid
sequenceDiagram
    participant Course as Toimumiskord
    participant Login as Sisselogimine / konto loomine
    participant Form as Registreerumisvorm
    Course->>Login: redirect = registreerumise URL koos returnTo väärtusega
    Login->>Form: Taasta redirect
    Form->>Course: Tühista või edukas registreerumine: taasta returnTo
    Note over Course: Säilib ka toimumiskorra enda tagasitee
```

Näited avamise kohta:

- `/admin-all-courses` → `/course-form?courseId=10&returnTo=...` → tagasi kõigi toimumiskordade nimekirja.
- `/admin-course?courseId=10` → `/admin-enquiry?enquiryId=4&returnTo=...` → tagasi toimumiskorra juurde.
- `/admin-user?userId=2` → `/admin-registration?courseParticipantId=5&returnTo=...` → tagasi konto juurde.
- `/training-form?...&returnTo=...` → tõlkevahetus → sama tagasitee; „Vaata“ loob tagasitee avatud vormile.

`returnTo` ei salvesta lähtevaate ainult komponendi mälus olevaid filtreid,
lehekülge ega salvestamata vormiteksti. Sihtvaated laevad andmed tavapäraselt.

## Toimumiskorra vahetus samas vaates

`/course` kaardi „Toimumiskorrad“ lingid kasutavad `router.replace` ja annavad
kaasa ainult uue `courseId`. `returnTo` eemaldatakse; URL ei pikene ega teki
uut ajalookirjet. Vaade laadib koondandmed ja vajadusel osalemise oleku uuesti.
„← Tagasi“ kasutab seejärel varusihti `/courses`.
