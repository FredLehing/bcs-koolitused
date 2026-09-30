# AdminLecturersView.vue — märkmed

Admini koolitajate nimekiri. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminLecturersView.vue
Frontend rada: /admin-lecturers

Vaatega seotud lisainfo:
Avaneb navbari menüüst "Admin" → "Koolitajad". Pealkirja real nupp "+ Lisa uus koolitaja" → /lecturer-form. Tabelis Nimi | Ametinimetus | Tõlked | Koolitusi | Tulevasi toimumiskordi | Uuendatud | Tegevused, pilte ei kuvata. Tõlked: ✓ kui tõlge on kõigis tõlkekeeltes, muidu ✗ ja tooltip "Puudub: en". Järjestus nime järgi; otsinguväli "Otsi nime järgi…" filtreerib frontendis (API kutset ei tehta); all "Kokku N koolitajat". Keele vahetusel laaditakse nimekiri uuesti.
"Muuda" → /lecturer-form?lecturerId={id}&lecturerTranslationId={id}. Prügikast (LecturerDeleteButton.vue) küsib kinnitust ja teeb DELETE (soft delete); kui koolitajal on tulevasi toimumiskordi, on prügikast keelatud ja tooltip selgitab põhjust (backend: 403 LECTURER_HAS_UPCOMING_COURSES → backendi message, nimekiri uuesti).
Lüliti "Näita kustutatud" (vaikimisi väljas) → includeDeleted=true: kustutatud read on tuhmimad, märgisega "Kustutatud" ja ainult nupuga "Taasta" (kinnitus → PUT /api/lecturer/{lecturerId}/restore).
```

## API märkmed — GET /api/admin-lecturers

```text
API: GET /api/admin-lecturers

Query parameetrid:
contentLang: String — ametinimetuse ja tõlke ID keel ("et"/"en")
includeDeleted: Boolean — true = ka kustutatud koolitajad (valikuline, vaikimisi false)

Response (200):
AdminLecturerSummaryDto.java
[
  {
    "lecturerId": 3,
    "lecturerTranslationId": 5,
    "fullName": "Kersti Laidvee",
    "title": "Lektor/konsultant",
    "status": "A",
    "hasAllTranslations": false,
    "missingTranslationLanguageCodes": [
      "en",
      ...
    ],
    "trainingCount": 1,
    "upcomingCourseCount": 0,
    "updatedAt": "2026-09-20T07:00:00Z"
  },
  ...
]

API teenuse lisainfo:
Andmed tulevad view'st admin_lecturer_summary, sorteeritud fullName järgi. Pilte ei tagastata. status: "A" = aktiivne, "D" = kustutatud; ilma includeDeleted=true tagastatakse ainult aktiivsed. title ja lecturerTranslationId on contentLang keeles, puudumisel põhikeeles ("Muuda" link). missingTranslationLanguageCodes = tõlkekeeled (requiresTranslation = true), milles tõlge puudub; tühi list, kui kõik on olemas. trainingCount = aktiivsed koolitused (status ≠ "D"), mille koolitajate hulgas ta on (training_lecturer). upcomingCourseCount = toimumiskorrad, mille koolitajate hulgas ta on (course_lecturer), end_date ≥ täna ja status ei ole "D" ega "X" — kui > 0, on prügikast keelatud. updatedAt = hiliseim lecturer, lecturer_translation ja lecturer_photo updated_at. Otsing nime järgi toimub frontendis.

Veateated: —
```

## API märkmed — DELETE /api/lecturer/{lecturerId}

```text
API: DELETE /api/lecturer/{lecturerId}

Response (200): NONE

API teenuse lisainfo:
Soft delete: määrab lecturer.status = "D" (LecturerStatus.DELETED) ja uuendab updated_at. Tõlkeid, pilti ega seoseid (training_lecturer, course_lecturer) ei kustutata. Keeldub, kui koolitajal on tulevasi toimumiskordi (end_date ≥ täna, status ei ole "D" ega "X"). Juba kustutatud koolitaja korral midagi ei muutu. Kustutatud koolitajat ei pakuta "Vali koolitaja" modalis ja tema kaarti ei kuvata; nimekirjas näeb teda lülitiga "Näita kustutatud" ja saab taastada (PUT /api/lecturer/{lecturerId}/restore). Frontendis teeb kutse LecturerDeleteButton.vue.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"

HTTP: 403
errorCode: LECTURER_HAS_UPCOMING_COURSES
message: "Koolitajal on tulevasi toimumiskordi, vali neile enne teine koolitaja"
```

## API märkmed — PUT /api/lecturer/{lecturerId}/restore

```text
API: PUT /api/lecturer/{lecturerId}/restore

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus (erand URL-ide kokkuleppest): taastab kustutatud koolitaja — määrab lecturer.status = "A" ja uuendab updated_at. Request body puudub. Kutse tehakse nupust "Taasta" (ainult status "D" real) pärast kinnituse modalit; AdminLecturersView laadib seejärel nimekirja uuesti.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'lecturerId' väärtusega: 123"
```
