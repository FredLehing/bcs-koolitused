# Admin-menüü ümberkorraldus

**Komponent:** `App.vue` (navbar, menüü "Admin" — nähtav ainult adminile)

**Roll:** Admin

**Vaste mockupis:** prototüübi kest `docs/mock-wireframe/loo-mock-vaade/index.html` (https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY, roll "Admin") ja kõigi admini läbimängude navbar.

Asendab varasemate taskide (`admin-trainings-view.md`, `admin-lecturers-view.md`, `admin-all-courses-view.md` jt) menüüd puudutavad osad. Kokkulepe 2026-10-01.

## Eesmärk

Menüü on lühem ja töö järgi järjestatud: ülal igapäevane töö (vastust ootavad päringud, registreerumised), siis koolitused, lõpus püsiandmed. "Lisa uus" lingid eemaldatakse — samad nupud on nimekirja vaadetes.

## Menüü

| # | Link | Rada | i18n võti | et | en |
|---|---|---|---|---|---|
| 1 | Koolituste päringud | `/admin-enquiries` | `navbar.manageEnquiries` | Koolituste päringud | Training enquiries |
| 2 | Registreerumised | `/admin-registrations` | `navbar.manageRegistrations` | Registreerumised | Registrations |
| — | eraldaja | | | | |
| 3 | Koolitused | `/admin-trainings` | `navbar.manageTrainings` | **Koolitused** (enne "Koolituste haldus") | **Trainings** (enne "Manage trainings") |
| 4 | Koolituste kalender | `/admin-all-courses` | `navbar.manageCourses` | Koolituste kalender | Course calendar |
| — | eraldaja | | | | |
| 5 | Koolitajad | `/admin-lecturers` | `navbar.manageLecturers` | Koolitajad | Trainers |
| 6 | Koolitusruumid | `/admin-rooms` | `navbar.manageRooms` | Koolitusruumid | Training rooms |

Eemaldatakse: "Lisa uus koolitus" (`/training-form`) ja "Lisa uus koolitaja" (`/lecturer-form`). i18n võtmed `navbar.addTraining` ja `navbar.addLecturer` jäävad — neid kasutavad `AdminTrainingsView` ja `AdminLecturersView` päise nupud.

## Otsused

- **"Koolitused", mitte "Meie koolitused"** — avalikus menüüs "Koolitused ▾" on juba "Meie koolitused" (`/trainings`); admini nimekirjal on oma nimi.
- **Päringud esimesena** — päring ootab admini vastust (staatus "Uus"), registreerumine toimib ilma adminita.
- **Vaate pealkiri ühtib menüüga:** `/admin-trainings` pealkiri on "Koolitused" (`adminTrainings.title`). Sama võtit `navbar.manageTrainings` kasutavad kiirnupud `TrainingFormView` ja `AdminTrainingCoursesView` vaates — need muutuvad automaatselt.
- **Prototüübi kest:** admini vaikimisi vaade pärast sisselogimist on `/admin-enquiries` (menüü esimene punkt); külgriba admini vaated on menüü järjekorras.

## Komponendid ja failistruktuur

- `App.vue`, `locales/et.json`, `locales/en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [x] Menüü "Admin" järjekord ja eraldajad nagu tabelis
- [x] "Lisa uus koolitus" ja "Lisa uus koolitaja" menüüs puuduvad, nimekirja nupud töötavad
- [x] `/admin-trainings` pealkiri ja kiirnupud "Koolitused" / "Trainings"
- [x] Läbimängude navbarid, prototüübi kest, skeemid ja märkmed vastavad uuele menüüle
