# Vaadetevaheline liikumine ja tagasitee — kaardistus

## Eesmärk ja kokkulepe

Detaili või vormi avamisel antakse `returnTo` väärtuseks lähtevaate täielik
rakendusesisene URL (`$route.fullPath`). Vastuvõttev vaade kuvab lihtsa lingi
„← Tagasi“. Query väärtuse kodeerib Vue Router; käsitsi kodeerimist frontendis ei tehta.
Näiteks `/course?courseId=10` →
`/lecturer?lecturerId=8&returnTo=%2Fcourse%3FcourseId%3D10`.

Kui lähte-URL sisaldab enda `returnTo` väärtust, säilib see järgmise tagasitee
sees: kalender → toimumiskord → koolitaja → tagasi toimumiskorda → tagasi kalendrisse.
Tagasilingile vajutamine kasutab valmis lähte-URL-i ega lisa sellele uut tagasiteed.
Brauseri ajaloo asemel kasutatakse päris linki, mis toimib ka uues vahekaardis.

## Kaardistus: lingid, ikoonid ja nupud

| Lähtekoht ja avamise viis | Sihtvaade | Otsus ja põhjus |
|---|---|---|
| `/trainings`: kaardi „Vaata lähemalt“ | `/training` | Lisa tagasitee; sama detail avaneb ka mujalt |
| `/trainings`, `/training`, admini koolituste tabel: muutmise ikoonid/tõlkelipud (`EditTrainingLink`) | `/training-form` | Lisa tagasitee vastavasse nimekirja või detaili |
| `/admin-trainings`: „Lisa uus“, silm ja kalender | `/training-form`, `/training`, `/admin-training-courses` | Lisa tagasitee |
| `/lecturers`: koolitaja kaart (`LecturerTile`) | `/lecturer` | Lisa tagasitee; varusiht on sama nimekiri |
| `/training`, `/course`: koolitaja kaart (`LecturerCard`) | `/lecturer` | Juba olemas; ühtlusta sihtvaate tagasilink ja valideerimine |
| `/lecturer`: koolituse nimelink | `/training` | Lisa tagasitee koolitaja detaili |
| Avalehe ja `/courses` kaart: detailnupp ja muutmise ikoon (`CourseCard`) | `/course`, `/course-form` | Lisa tagasitee; jagatud kaardi mõlemad kasutuskohad |
| `/participant-courses`: registreerumise koolituse nimelink (`ParticipantRegistrationItem`) | `/course` | Lisa tagasitee kasutaja koolituste nimekirja |
| `/course`: koolituse link, muu toimumiskorra link ja muutmise ikoon | `/training`, `/course`, `/course-form` | Lisa tagasitee praeguse toimumiskorra juurde |
| `/course`: „Registreeru“ | `/course-registration` | Lisa tagasitee; sisse logimata kasutajal säilib registreerumise URL koos tagasiteega `redirect` sees |
| `/admin-all-courses`: koolituse nimi/kalender, silm, muutmise ikoon | `/admin-training-courses`, `/admin-course`, `/course-form` | Lisa tagasitee kõigi toimumiskordade nimekirja |
| `/admin-training-courses`: „Lisa uus“, muutmine, koolituse eelvaade ja muutmine | `/course-form`, `/training`, `/training-form` | Lisa tagasitee ühe koolituse kalendrisse |
| `/training-form`: „Vaata“ ja „Koolituse kalender“ | `/training`, `/admin-training-courses` | Lisa tagasitee avatud vormi koos tõlkeparameetritega |
| `/admin-course`: muutmine, koolituse kalender, koolituse nimelink, avaliku lehe link | `/course-form`, `/admin-training-courses`, `/training`, `/course` | Lisa tagasitee admini detaili |
| `/admin-course`: osaleja silm (`CourseParticipantsTable`) | `/admin-registration` | Olemasolev käsitsi koostatud lähterada asenda täieliku URL-iga, et ahel säiliks |
| `/admin-course`: päringu silm (`CourseEnquiriesTable`) | `/admin-enquiry` | Lisa tagasitee; praegu viis tagasi ainult päringute nimekirja |
| `/admin-enquiries`: silm | `/admin-enquiry` | Lisa tagasitee nimekirja |
| `/admin-enquiry`: koolituse nimelink | `/training` | Lisa tagasitee päringu detaili |
| `/admin-registrations`: toimumisaja link ja silm | `/admin-course`, `/admin-registration` | Lisa tagasitee registreerumiste nimekirja |
| `/admin-registration`: „Ava toimumiskord“ | `/admin-course` | Lisa tagasitee registreerumise detaili |
| `/admin-users`: silm | `/admin-user` | Lisa tagasitee kontode nimekirja |
| `/admin-user`: toimumisaja link ja registreerumise silm | `/admin-course`, `/admin-registration` | Lisa täielik tagasitee konto detaili |
| `/admin-lecturers`: „Lisa uus“ ja muutmise ikoon | `/lecturer-form` | Lisa tagasitee; vormi tõlkevahetus peab selle säilitama |
| `/admin-rooms`: „Lisa uus“ ja muutmise ikoon | `/room-form` | Lisa tagasitee järjepideva vormikäitumise jaoks |

`/admin-training-courses` praeguses tabelis pole eraldi silmaikooni admini
toimumiskorra detaili; kaardistus ei lisa uusi avamisnuppe.

## Kohad, kus uut tagasiteed ei lisata

- `App.vue` peamenüü, logo ja profiilimenüü; `NavTabs`, `TrainingsTabs`,
  admini vahelehed ja `ProfileMenu`: need valivad iseseisva jaotise.
- Avalehe „Meie koolitused“, „Koolituste kalender“, üldotsing ja „Kõik
  toimumiskorrad“; kasutaja tühja nimekirja kalendrilink: iseseisva nimekirja avamine.
- Detailide ja vormide selge nimega üldnimekirja-/kalendrinupud jäävad
  sihtlinkideks. Eraldi „← Tagasi“ kasutab lähtekohta.
- „Tagasi“, „Tühista“, registreerumise lõpetamine ning vormi salvestamise
  järel tagasiminek ei loo uut `returnTo` väärtust.
- Sisselogimine/konto loomine kasutab olemasolevat `redirect` lepingut;
  registreerumise tagasitee säilib selle sees. Vea-, õiguse- ja väljalogimise
  suunamised, PDF/pildi allalaadimine, välislingid, modaalid ning kohapealsed
  oleku-/filtri-/leheküljevahetused ei ole uue tagasitee avamiskohad.

## Varusihtkohad ja vormid

| Vastuvõttev vaade | Siht otselingi või sobimatu `returnTo` korral |
|---|---|
| `/training` | `/trainings` |
| `/course` | `/courses` |
| `/lecturer` | `/lecturers` |
| `/training-form` | `/admin-trainings` |
| `/lecturer-form` | `/admin-lecturers` |
| `/room-form` | `/admin-rooms` |
| `/course-form`, `/admin-course` | `/admin-training-courses?trainingId={id}` |
| `/admin-training-courses` | `/admin-trainings` |
| `/admin-enquiry` | `/admin-enquiries` |
| `/admin-registration` | `/admin-registrations` |
| `/admin-user` | `/admin-users` |
| `/course-registration` | `/course?courseId={id}` |

Koolituse ja koolitaja vormid jäävad salvestamisel vormi; nende `router.replace`
oleku- ja tõlkevahetused säilitavad algse tagasitee. Toimumiskorra vormi
salvestamine ja alumine „Tagasi“ kasutavad lähtekohta (varusiht on koolituse
kalender). Kustutamise järel ei pöörduta kustutatud toimumiskorra avalikku või
admini detaili; sel juhul minnakse koolituse kalendrisse. Registreerumise
„Tühista“ ja edukas lõpetamine taastavad lähte-URL-i, sh selle enda tagasitee.
Ruumivorm jätkab pärast salvestamist olemasolevat liikumist ruumide nimekirja.

## Teostus ja piirid

Ühine `BackLink.vue` ja `NavigationService` kontrollivad, et tagasitee oleks
olemasolev rakendusesisene route. Välisaadress, `//`, kaldkriipsu tagurpidi
variant, juhtmärgid, query massiiv ja iseendale osutav tagasitee ei sobi.
Õigusi kontrollivad endiselt sihtvaadete olemasolevad kontrollid.

Täielik URL säilitab URL-is olevad parameetrid. Nimekirjade praegused ainult
komponendi mälus olevad filtrid, sorteerimine ja lehekülg ei taastu pelgalt
`returnTo` kaudu; nende URL-i või store'i viimine on eraldi muudatus.

Skeem: [tagasitee navigatsioon](../../mock-wireframe/loo-mock-vaade/return-to-navigation-skeemid.md).

## Kontrollitud

Muudetud frontendi failide ESLint ja frontendi tootmisbuild läbivad kontrolli.
Tegelikku Vue Routerit kasutav kontroll läbib mitmeastmelise edasi-tagasi
liikumise, vormi/tõlkevahetused, sisselogimise ja registreerumise ahela,
salvestamise ning kustutamise varusihtkohad. `BackLink` on kontrollitud
Vue serverirenderdusega. Avamislinkide katvus ning mockide tagasitee ja
JavaScripti süntaks on kontrollitud. Visuaalset brauserikontrolli ei tehtud.
