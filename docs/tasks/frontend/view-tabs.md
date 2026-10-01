# Vahelehed sama rühma vaadete vahel

**Komponendid:** `components/common/NavTabs.vue` (üldine), `AdminTabs.vue`, `TrainingsTabs.vue`

**Roll:** Admin (admini vahelehed), kõik rollid (koolituste vahelehed)

**Vaste mockupis:** admini läbimängud ning `trainings-view/trainings-view-labimang.html` ja `courses-view/courses-view-labimang.html` (prototüübi kestas https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY)

Kokkulepe 2026-10-01, katsetatud päris koodis enne dokumentatsiooni (vahelehed vs nupugrupp → vahelehed).

## Eesmärk

Sama navbari menüü vaadete vahel saab liikuda ilma menüüd avamata ja on kohe näha, mis vaates ollakse.

## Vahelehed

| Komponent | Vahelehed (järjekord nagu menüüs) | Vaated |
|---|---|---|
| `AdminTabs.vue` | Koolituste päringud \| Registreerumised \| Koolitused \| Koolituste kalender \| Koolitajad \| Koolitusruumid | `AdminEnquiriesView`, `AdminRegistrationsView`, `AdminTrainingsView`, `AdminAllCoursesView`, `AdminLecturersView`, `AdminRoomsView` |
| `TrainingsTabs.vue` | Meie koolitused \| Koolituste kalender | `TrainingsView` (`/trainings`), `CoursesView` (`/courses`) |

- Tekstid on navbari i18n võtmed (`navbar.manageEnquiries` … `navbar.manageRooms`, `navbar.ourTrainings`, `navbar.coursesCalendar`) — uusi tõlkeid pole.
- Vahelehed on vaate ülaosas, pealkirja kohal (`/trainings` vaates filtrite ja otsingu kohal).
- Detaili- ja vormivaadetes (nt `/admin-enquiry`, `/training-form`, `/course`) vahelehti pole — seal on oma tagasilingid.

## `NavTabs.vue`

- Prop `tabs: [{ routeName, label }]`; iga vaheleht on `RouterLink` (Bootstrap `nav nav-tabs`), aktiivne (`active`, `aria-current="page"`) tuleb `$route.name` järgi.
- Kõik vahelehed on ühel real; kui ekraanile ei mahu, keritakse rida külgsuunas (ei murra kahele reale). Kerimine lõikaks aktiivse vahelehe alumise serva ära, seepärast on rea alumine joon `box-shadow`, mille aktiivne vaheleht katab.
- Vaate avamisel (`mounted`) keritakse rida nii, et aktiivne vaheleht on keskel — kitsal ekraanil on näha ka naabrid.
- Uue rühma jaoks piisab väikesest komponendist, mis annab `NavTabs`-ile lingiloendi (nagu `AdminTabs`, `TrainingsTabs`).

## Komponendid ja failistruktuur

- `components/common/NavTabs.vue`, `AdminTabs.vue`, `TrainingsTabs.vue` (uued)
- `views/AdminEnquiriesView.vue`, `AdminRegistrationsView.vue`, `AdminTrainingsView.vue`, `AdminAllCoursesView.vue`, `AdminLecturersView.vue`, `AdminRoomsView.vue`, `TrainingsView.vue`, `CoursesView.vue` (muudetakse)

## Vastuvõtu kriteeriumid

- [x] Admini kuues nimekirjavaates vahelehed kõigi admin-menüü linkidega, aktiivne esile tõstetud
- [x] `/trainings` ja `/courses` vaates vahelehed "Meie koolitused | Koolituste kalender"
- [x] Kitsal ekraanil üks keritav rida, aktiivne vaheleht keritakse keskele
- [x] Tekstid et/en (navbari võtmed)
- [x] Läbimängud, skeemid ja märkmed kirjeldavad vahelehti
