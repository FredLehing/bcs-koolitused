# Frontendi Tailwindi stiilijuhend (prototüüp)

Haru `alternative-frontend-design`: frontendi uus kujundus Tailwind CSS v4-ga, ilma Bootstrapita.
Kujunduse eeskuju on lõuend „BCS Koolitus uus kujundus“ (https://claude.ai/artifact/C1sfDJHmJ9sJUriG4yc4oa).

## Seis (02.10.2026)

Kõik vaated on Tailwindis, Bootstrapi pakett (`bootstrap`, `@popperjs/core`) on eemaldatud.
Bootstrapi JS-i asendused: karussell, päise rippmenüüd ja mobiilimenüü on Vue olekuga, tooltip'id
`HelpTip.vue`.

| Rühm | Vaated | Paigutus |
|---|---|---|
| Avalikud | `HomeView`, `TrainingsView`, `TrainingView`, `CoursesView`, `CourseView`, `CourseRegistrationView`, `LecturersView`, `LecturerView`, `LoginView`, `SignupView`, `ErrorView`, `NotAuthorizedView` | mobile first |
| Osaleja | `ParticipantDetailsView`, `ParticipantCoursesView`, `ParticipantCertificatesView`, `ParticipantFeedbackFormView`, `ChangePasswordView` | mobile first |
| Admini nimekirjad | `AdminEnquiriesView`, `AdminRegistrationsView`, `AdminFeedbacksView`, `AdminTrainingsView`, `AdminAllCoursesView`, `AdminLecturersView`, `AdminRoomsView`, `AdminUsersView` | desktop |
| Admini detailid | `AdminCourseView`, `AdminEnquiryView`, `AdminRegistrationView`, `AdminUserView`, `AdminTrainingCoursesView` | desktop |
| Admini vormid | `TrainingFormView`, `CourseFormView`, `LecturerFormView`, `RoomFormView` | desktop |

`TestView` / `TestComponent` on õppenäide ja jäi muutmata.

**Käivitamine:** haru vahetuse järel `npm install` (Tailwind lisandus, Bootstrap eemaldati), siis `npm run dev`.

## Seadistus

- `vite.config.js` — plugin `@tailwindcss/vite`.
- `src/assets/main.css` — `@import 'tailwindcss'`, teema (`@theme`), baasstiilid ja põhielemendid.
- `index.html` — fondid Google Fontsist.

## Värvid ja fondid

Värvid tulevad logost. Kasuta teema nimesid, mitte hex-koode.

| Klassi osa | Väärtus | Kasutus |
|---|---|---|
| `brand-600` | `#0b6bb0` | põhinupud, lingid, aktiivne olek |
| `brand-700` | `#08548c` | hover |
| `brand-300` | `#96b3df` | logo helesinine: aktsendid, ikoonid |
| `brand-200` | `#c9d7ea` | väljade ja esile tõstetud kaartide ääris |
| `brand-100`, `brand-50` | `#eaf2fb`, `#f2f7fc` | kategooria märgis, esile tõstetud kaardi ja hover'i taust |
| `navy` | `#0a2540` | pealkirjad, tume üleskutse plokk |
| `ink` | `#13233a` | põhitekst |
| `muted` | `#4a5b70` | teisene tekst |
| `line` | `#e1e8f1` | äärised ja eraldusjooned |
| `surface` | `#f6f8fb` | lehe taust, tabeli päis |

Olekud kasutavad Tailwindi vaikimisi värve: edu `emerald`, hoiatus `amber`, viga `red`.

Fondid: pealkirjad `font-display` (Plus Jakarta Sans), tekst `font-sans` (Source Sans 3).
`h1`–`h4` saavad `font-display` ja `text-navy` automaatselt.

## Põhielemendid (`main.css`, `@layer components`)

Nimed on samad mis Bootstrapis, et ka ümber kujundamata vaated saaksid uue stiili.

| Klass | Tähendus |
|---|---|
| `btn` + `btn-primary` / `btn-outline-primary` / `btn-outline-secondary` / `btn-outline-danger` / `btn-outline-success` / `btn-success` / `btn-danger` / `btn-white` / `btn-link` | nupp, kõrgus 44 px |
| `btn-sm`, `btn-lg`, `btn-icon` | väiksem (36 px), suurem (52 px), ainult ikooniga nupp |
| `form-label`, `form-control`, `form-select`, `form-text` | vormiväljad |
| `form-check`, `form-check-input`, `form-check-label`, `form-switch` | märkeruut, raadionupp, lüliti |
| `card`, `card-body` | valge ümarate nurkadega kast |
| `badge` + `text-bg-primary` / `-success` / `-warning` / `-danger` / `-secondary` / `-light` | märgis |
| `alert` + `alert-success` / `alert-danger` / `alert-info` | teade |
| `table`, `table-hover` | tabel |
| `visually-hidden` | ainult ekraanilugejale |

Paigutus, vahed ja tüpograafia tehakse Tailwindi utiliitidega, mitte uute CSS-klassidega.
`<style scoped>` plokis ei kasutata `@apply`-d.

## Mustrid

- **Lehe raam:** avalikud vaated `mx-auto w-full max-w-6xl px-4 sm:px-6`, admini vaated `mx-auto w-full max-w-7xl px-6`.
- **Kaart:** `rounded-2xl border border-line bg-white p-5 sm:p-6`.
- **Esile tõstetud:** `border-brand-200 bg-brand-50` ja märgis `badge text-bg-primary` tähega (mitte kollane taust).
- **Kategooria märgis:** `rounded-md bg-brand-100 px-2.5 py-0.5 font-semibold text-brand-700`.
- **Otsinguväli:** valge kast ikooni ja `focus-within:ring-3 focus-within:ring-brand-600/15`-ga, Enter = `<form @submit.prevent>`.
- **Admini vorm:** osad kaartidena (`section` + `h2`), väljad `grid gap-4 md:grid-cols-2`, salvestusnupurida kleepub alla (`sticky bottom-0`).
- **Admini tabel:** tabel kastis `overflow-hidden rounded-2xl border border-line bg-white`, päis `bg-surface`, numbrid `tabular-nums`, tegevusnupud `btn btn-outline-secondary btn-sm btn-icon` paremas servas.
- **Modaal:** `BaseModal.vue` (Teleport `body`-sse, telefonis alt üles avanev leht).
- **Abitekst:** `HelpTip.vue` (Bootstrapi tooltip'i asemel; töötab ka puutega). Ikoon rea alguses → `align="left"`.
- **Lipp:** `FlagIcon.vue`, suurus teksti suuruse klassiga (nt `class="text-xl"`).

## Mobile first

Avalikud ja osaleja vaated kirjutatakse kõigepealt kitsale ekraanile: ilma eesliiteta klassid kehtivad
telefonis, `sm:` / `md:` / `lg:` lisavad laiema ekraani muudatused. Puutealad vähemalt 44 px
(`min-h-11`). Admini vaated on desktopile; laiad tabelid kerivad külgsuunas (`overflow-x-auto`).
