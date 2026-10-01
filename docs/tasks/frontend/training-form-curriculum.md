# Koolituse vorm: õppekava (PDF)

**Vaade:** `TrainingFormView.vue`, route `/training-form` (kõik olekud)

**Roll:** Admin

**Vaste mockupis:** läbimäng `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (artifact https://claude.ai/artifact/3VjRgmnQK7Ck7qWVHuRK9b), tõlke väli "Õppekava (PDF)". Kõiki vaateid saab koos läbi mängida prototüübi kestas `docs/mock-wireframe/loo-mock-vaade/index.html` (artifact https://claude.ai/artifact/SqxcgWP1BoxUGZqFfSZCPY). Otsused: `docs/mock-wireframe/loo-mock-vaade/form-view/training-curriculum-plaan.md`, skeemid `training-form-view-skeemid.md` (jaotis 10), märkmed `docs/mock-wireframe/markmed/training-form-view-state-*-markmed.md`.

> Mockupi pilt lisatakse hiljem.

Muudab olemasolevat vaadet. Backend taskid: `training-curriculum-upload.md` (salvestamine), `GET-api-training-translation-trainingTranslationId-curriculum.md` (failinimi + allalaadimine). Mockidega saab alustada enne neid.

## Kasutajavoog

Igal tõlkel (lipukesel) on oma õppekava. Admin valib tõlke väljade all PDF-faili; fail salvestub koos vormiga ("Lisa", "Lisa tõlge", "Salvesta"). Salvestatud faili nimi on lingina näha (klikk laadib alla), seda saab vahetada või eemaldada. Failinime teeb backend pealkirjast ja sõnast "Õppekava" tõlke keeles, nt `tehisaru-toovahendid-arendajale-oppekava.pdf`.

## Kasutajaliidese elemendid

Uus komponent `components/forms/CurriculumUpload.vue` (muster nagu `PhotoUpload.vue`), `TrainingTranslationForm.vue`-s pärast "Kirjeldus" välja. Neli olekut:

| Olek | Kuva | Nupud |
|---|---|---|
| Puudub (`curriculumFileName = null`, faili pole valitud) | failivalija + vihje "PDF, kuni 10 MB" | "Vali fail" (`<input type="file" accept="application/pdf">` label-nupu sees) |
| Valitud, salvestamata | valitud faili algne nimi + suurus, märge "salvestatakse koos vormiga" | "Tühista" |
| Olemas (`curriculumFileName ≠ null`) | failinimi lingina (allalaadimine) + suurus "(1,2 MB)" | "Vaheta" (avab failivalija), "Eemalda" |
| Eemaldatakse | failinimi läbikriipsutatuna, märge "eemaldatakse salvestamisel" | "Tühista" |

- "Eemalda" **kinnitust ei küsi** — eemaldus jõustub alles salvestamisel, "Tühista" võtab tagasi.
- Valitud faili või olemasoleva faili korral vihje "Nimi pärast salvestamist: `<failinimi>`" pole kohustuslik (läbimängus on see näitlik); failinime reegel on backendis.
- Suurus: alla 100 KB → "KB", muidu "MB" ühe komakohaga eesti formaadis ("1,2 MB").
- Olekus `new-translation` on väli tühi (põhikeele faili ei kopeerita); "Tee AI tõlge" välja ei puuduta.

## Käitumine ja valideerimine

1. **Laadimine:** `GET /api/training-translation/{trainingTranslationId}` vastusest `curriculumFileName`, `curriculumFileSize`.
2. **Faili valimine:** kontroll `file.type === 'application/pdf'` ja `file.size ≤ 10 MB`; muidu veateade välja all "Lubatud on PDF-fail kuni 10 MB" (fail jääb valimata). Sobiv fail loetakse `FileReader.readAsDataURL`-iga, Base64 = osa pärast koma (nagu `PhotoUpload.handleFileRead`).
3. **Salvestamine** — request body'sse lisanduvad:
   - `curriculum`: valitud faili Base64 või `null`;
   - `isCurriculumRemoved`: ainult `PUT /api/training/{trainingId}`;
   - `curriculumLabel`: **alati**, sõna "Õppekava" avatud tõlke keeles, mitte kasutajaliidese keeles:
     ```js
     curriculumLabel: this.$t('trainingForm.translation.curriculum', {}, { locale: this.translation.languageCode })
     ```
     Kui tõlke keelt locale'ides pole, annab vue-i18n fallback-keele sõna.
4. **Pärast "Salvesta"** laaditakse avatud tõlge uuesti (`GET /api/training-translation/{id}`), et näidata uut failinime (muutub ka siis, kui muudeti ainult pealkirja); valitud fail ja eemaldus lähtestatakse. "Lisa" / "Lisa tõlge" teevad niikuinii `router.replace` + laadimise.
5. **Allalaadimine:** link `href` = `TrainingService.getCurriculumUrl(trainingTranslationId)` → `/api/training-translation/{id}/curriculum` (nagu `LecturerService.getLecturerPhotoUrl`); backend saadab `Content-Disposition: attachment`, seega brauser salvestab faili õige nimega.
6. **Vead:** 403 `CURRICULUM_TYPE_NOT_ALLOWED` / `CURRICULUM_TOO_LARGE` → backendi `message` vormi veateatena (nagu teised 403 vead).

## API kutsed

| Päring | Muudatus |
|---|---|
| `GET /api/training-translation/{trainingTranslationId}` | vastuses `curriculumFileName`, `curriculumFileSize` |
| `POST /api/training` | body: `curriculum`, `curriculumLabel` |
| `PUT /api/training/{trainingId}` | body: `curriculum`, `isCurriculumRemoved`, `curriculumLabel` |
| `POST /api/training/{trainingId}/training-translation` | body: `curriculum`, `curriculumLabel` |
| `GET /api/training-translation/{trainingTranslationId}/curriculum` | uus — ainult lingi URL, axios'iga ei kutsuta |

JSON näidised: märkmete failid ja backend taskid. Kuni backend pole valmis, lisa `MockDatabase`-sse tõlkele 1 "Java algkursus" `curriculumFileName: "java-algkursus-oppekava.pdf"`, `curriculumFileSize: 846213`.

## Komponendid ja failistruktuur

| Fail | Uus / muudetakse | Sisu |
|---|---|---|
| `components/forms/CurriculumUpload.vue` | uus | 4 olekut, failivalija, kontroll, Base64; emits nagu `PhotoUpload` (`event-curriculum-selected`, `event-curriculum-removed`, `event-curriculum-error`) |
| `components/forms/TrainingTranslationForm.vue` | muudetakse | `CurriculumUpload` pärast "Kirjeldus" välja |
| `views/TrainingFormView.vue` | muudetakse | olek `newCurriculum`, `isCurriculumRemoved`; request body väljad; tõlke uuesti laadimine pärast salvestamist |
| `api-services/TrainingService.js` | muudetakse | `getCurriculumUrl(trainingTranslationId)` |
| `api-services/mock/MockDatabase.js` | muudetakse | näidis-õppekava tõlkele 1 |
| `locales/et.json`, `locales/en.json` | muudetakse | vt allpool |

**i18n võtmed** (`trainingForm.translation.*`):

| Võti | et | en |
|---|---|---|
| `curriculum` | Õppekava | Curriculum |
| `curriculumField` | Õppekava (PDF) | Curriculum (PDF) |
| `curriculumSelect` | Vali fail | Choose file |
| `curriculumChange` | Vaheta | Replace |
| `curriculumRemove` | Eemalda | Remove |
| `curriculumUndo` | Tühista | Undo |
| `curriculumHint` | PDF, kuni 10 MB | PDF, up to 10 MB |
| `curriculumPending` | salvestatakse koos vormiga | will be saved with the form |
| `curriculumRemoving` | eemaldatakse salvestamisel | will be removed on save |
| `curriculumInvalid` | Lubatud on PDF-fail kuni 10 MB | Only a PDF file up to 10 MB is allowed |

`curriculum` on failinime sõna — **ära muuda seda hiljem kergekäeliselt**, see muudab järgmisel salvestamisel failinimesid.

## Vastuvõtu kriteeriumid

- [ ] Väli "Õppekava (PDF)" kõigis kolmes olekus; olekus `new-translation` tühi
- [ ] Neli olekut ja nupud töötavad; "Eemalda" ilma kinnituseta, "Tühista" taastab
- [ ] Mitte-PDF või > 10 MB → veateade, faili ei valita
- [ ] POST / PUT saadavad `curriculum` (Base64 / `null`), PUT ka `isCurriculumRemoved`; `curriculumLabel` alati tõlke keeles (`et` tõlge inglise kasutajaliidesega → "Õppekava")
- [ ] Pärast "Salvesta" on näha uus failinimi
- [ ] Failinime link laadib faili alla
- [ ] Tekstid et/en; `npm run lint` ja build lähevad läbi


## Teostuse seis (2026-10-01)

Taski kood on valmis harus `RAIN-training-curriculum-frontend` (`RAIN-training-curriculum` põhjal). Lint ja build läbisid kontrolli `/tmp` koopias. Ajutises DOM-/teenusekontrollis kontrolliti faili nelja olekut, tühistamist, valideerimist (ka täpselt 10 MB ja üks bait üle piiri), päringute sisu, tõlkekeelset `curriculumLabel`-i ning salvestamisjärgset laadimist. Päris backendiga brauseri vastuvõtukontroll jääb teha, mistõttu ülalolevad vastuvõtukriteeriumid on veel märkimata.

Vorm kasutab juba päris backendit, seetõttu `MockDatabase` näidisfaili ei lisatud. Faili lugemise ja salvestamise ajal on salvestusnupud blokeeritud; lugemisvea jaoks lisati et/en võti `trainingForm.translation.curriculumReadError`.
