# Termin "lektor" → "koolitaja" kasutajaliideses

**Vaated:** `App.vue` (navbar), `TrainingFormView.vue` (`/training-form`, "Vali koolitaja" modal), `TrainingView.vue` (`/training`)

**Roll:** Kõik rollid

**Vaste mockupis:** läbimängud `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-labimang.html` ja `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html`

> Mockupi pilti pole vaja — muutuvad ainult tekstid.

Otsus (`admin-lecturers-view-skeemid.md`, "Üldine"): kasutajaliideses on kõikjal **"koolitaja"**, inglise keeles **"Trainer"**. Koodis, i18n võtmete nimedes ja andmebaasis jääb `lecturer`.

## Kasutajavoog

Kasutaja näeb kogu rakenduses ühte terminit: navbari link "Koolitajad", koolituse vormil "Vaikimisi koolitaja" ja "Vali koolitaja", koolituse lehel "Koolitaja".

## Kasutajaliidese elemendid

Muutuvad ainult tekstid (`locales/et.json`, `locales/en.json`); võtmed jäävad samaks.

| Võti | et praegu → uus | en praegu → uus |
|---|---|---|
| `navbar.lecturers` | "Lektorid" → "Koolitajad" | "Lecturers" → "Trainers" |
| `trainingForm.data.defaultLecturer` | "Vaikimisi lektor" → "Vaikimisi koolitaja" | "Default lecturer" → "Default trainer" |
| `trainingForm.data.noLecturer` | "— lektor puudub —" → "— koolitaja puudub —" | "— no lecturer —" → "— no trainer —" |
| `trainingForm.data.selectLecturer` | "Vali lektor" → "Vali koolitaja" | "Select lecturer" → "Select trainer" |
| `trainingForm.lecturerModal.title` | "Vali lektor" → "Vali koolitaja" | "Select lecturer" → "Select trainer" |
| `trainingForm.lecturerModal.notFound` | "Ühtegi lektorit ei leitud." → "Ühtegi koolitajat ei leitud." | "No lecturers found." → "No trainers found." |
| `trainingForm.lecturerModal.noLecturer` | "Lektor puudub" → "Koolitaja puudub" | "No lecturer" → "No trainer" |
| `trainingView.sidebar.lecturer` | "Koolitaja" (juba õige) | "Trainer" (juba õige) |

Kontrolli ka teisi esinemisi: `rg -i "lektor|lecturer" frontend/src/locales frontend/src --glob '!**/api-services/**'` (koodi muutujanimesid ei muudeta, ainult kasutajale nähtavaid tekste). Kui `LecturerSelectModal.vue`-s või mujal on kõvakodeeritud tekste, vii need i18n-i.

## Käitumine ja valideerimine

Käitumine ei muutu.

## API kutsed

Puuduvad.

## Komponendid ja failistruktuur

| Fail | Muudetakse |
|---|---|
| `locales/et.json`, `locales/en.json` | tekstid (NB: `en.json`-is on sektsioonide vahel tühjad read — muuda tekstina, mitte JSON-i ümber vormindades) |

## Vastuvõtu kriteeriumid

- [ ] Kasutajaliideses (et) pole sõna "lektor"; en-is pole "lecturer"
- [ ] Navbaris "Koolitajad" / "Trainers"
- [ ] Koolituse vormil "Vaikimisi koolitaja" ja modal "Vali koolitaja"
- [ ] i18n võtmete nimed ja kood ei muutunud; `en.json` vorming (tühjad read) säilis
