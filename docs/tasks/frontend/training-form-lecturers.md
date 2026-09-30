# Koolituse vorm: mitu koolitajat

**Vaade:** `TrainingFormView.vue`, route `/training-form` (kõik olekud)

**Roll:** Admin

**Vaste mockupis:** läbimäng `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (artifact https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b), väli "Koolitajad". Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY).

Muudab olemasolevat vaadet. Eeldab backend taski `training-lecturers-multiple.md`.

## Kasutajavoog

Välja "Vaikimisi koolitaja" asemel on "Koolitajad": admin lisab "+ Lisa koolitaja" nupuga ühe või mitu koolitajat, muudab järjekorda ↑ ↓ ja eemaldab ×-ga. Esimene kuvatakse esimesena (koolituse lehel ja kalendris).

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Koolitajad" | `LecturersPicker.vue` (vt `course-form-view.md`) | nimekiri "1. Rain Tüür ↑ ↓ ×"; tühjana "— koolitajaid pole —" |
| "+ Lisa koolitaja" | nupp → `LecturerSelectModal.vue` | juba valitud koolitajaid nimekirjas ei pakuta; valik lisab lõppu; nupp "Koolitaja puudub" eemaldatakse modalist |

## Käitumine ja valideerimine

1. Laadimisel `TrainingDto.lecturers` → `lecturerIds` (järjekorras).
2. `POST /api/training` ja `PUT /api/training/{trainingId}` saadavad `lecturerIds` (järjekord = `sort_order`) `defaultLecturerId` asemel.
3. Olekus `new-translation` on nimekiri kirjutuskaitstud (nagu teised koolituse väljad).
4. 404 `'lecturerId'` (vahepeal kustutatud koolitaja) → backendi `message`.

## API kutsed

`GET /api/training/{trainingId}`, `POST /api/training`, `PUT /api/training/{trainingId}` — muudatused: `training-lecturers-multiple.md`. `GET /api/lecturers` — olemas.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `components/forms/TrainingDataForm.vue` / `views/TrainingFormView.vue` | muudetakse | lektori väli → `LecturersPicker` |
| `components/modals/LecturerSelectModal.vue` | muudetakse | prop `excludedLecturerIds`; "Koolitaja puudub" nupp eemaldatakse |
| `locales/*.json` | muudetakse | `trainingForm.data.lecturers` ("Koolitajad"), `addLecturer` ("+ Lisa koolitaja") |

## Vastuvõtu kriteeriumid

- [ ] Mitu koolitajat, järjekord ja eemaldamine töötavad
- [ ] `lecturerIds` saadetakse POST / PUT-is; laadimisel järjekord säilib
- [ ] Modal ei paku juba valitud koolitajaid
- [ ] Vormi muu käitumine ei muutu
