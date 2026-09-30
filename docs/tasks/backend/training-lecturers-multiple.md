# Mitu koolitajat koolitusel ja toimumiskorral (`training_lecturer`, `course_lecturer`)

**Teenused (olemasolevad, muudetakse):** `GET /api/training/{trainingId}`, `POST /api/training`, `PUT /api/training/{trainingId}`

**Kasutavad vaated:** `TrainingFormView.vue` (`/training-form`), hiljem `CourseFormView.vue`, `AdminTrainingCoursesView.vue`, `TrainingView.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` ja otsuseid `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md` (jaotis "Mitu koolitajat" ja 1).

Eeldab taski `lecturer-db-changes.md`. **Muudab olemasolevat, töötavat koodi** (TrainingService, DTO-d, testid).

## Sisend

`TrainingCreateRequestDto` ja `TrainingUpdateRequestDto`: väli `defaultLecturerId` asendub väljaga **`lecturerIds: List<Integer>`** (kohustuslik, võib olla tühi list; järjekord = `sort_order`, 1 = esimene; korduvad ID-d on keelatud → 400).

```json
{
  "lecturerIds": [1, 8]
}
```

## Väljund

`TrainingDto` (`GET /api/training/{trainingId}`): `defaultLecturerId` ja `defaultLecturerName` asenduvad väljaga **`lecturers`** (`sort_order` järjekorras, tühi list, kui koolitajaid pole):

```json
{
  "lecturers": [
    { "lecturerId": 1, "lecturerName": "Rain Tüür" },
    { "lecturerId": 8, "lecturerName": "Meelis Teern" }
  ]
}
```

`POST` / `PUT`: `training_lecturer` read kirjutatakse `lecturerIds` järgi üle (sama transaktsiooni sees). Uus ID peab olema **aktiivne** koolitaja (`getValidActiveLecturerBy`); juba seotud kustutatud koolitaja võib `PUT`-is jääda (vt `lecturer-deleted-status.md`).

## Eesmärk

Koolitust ja toimumiskorda võib läbi viia mitu koolitajat. Koolituse koolitajad on "vaikimisi meeskond", millega eeltäidetakse uus toimumiskord; toimumiskorra koolitajad on edasi sõltumatud.

## Seotud andmebaasi tabelid

`docs/database/2_create.sql` muudatused (SQL: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`, jaotis 1):
- uued tabelid `training_lecturer` (`training_id`, `lecturer_id`, `sort_order`, unikaalne `training_id + lecturer_id`) ja `course_lecturer` (`course_id`, `lecturer_id`, `sort_order`, unikaalne `course_id + lecturer_id`) koos FK-dega;
- eemaldatakse `training.default_lecturer_id` ja `course.lecturer_id` (ja nende FK-d).

`docs/database/3_import.sql`: `training.default_lecturer_id` väärtused kolivad `training_lecturer`-isse (SQL ja jaotus: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`, jaotis 1 "Seed-andmed"; kahe koolitajaga koolitused 1, 9, 11). `course_lecturer` read olemasolevatele toimumiskordadele: `(1, 1, 1, 1), (2, 1, 8, 2), (3, 2, 2, 1)`; ülejäänud lisab `course-db-changes.md`.

Backend: entity'd `TrainingLecturer` ja `CourseLecturer` (+ repositooriumid), `Training.defaultLecturer` ja `Course.lecturer` väljad kaovad. `admin_training_summary` ja `training_summary` view'd `default_lecturer_id`-d ei kasuta (kontrolli).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Mõni uus `lecturerIds` ID ei leidu või on kustutatud koolitaja | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'lecturerId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `lecturerIds` puudub või sisaldab korduvat ID-d | 400 Bad Request | `{ "message": "lecturerIds: …", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] `training_lecturer` ja `course_lecturer` tabelid ja seed; `default_lecturer_id` / `course.lecturer_id` eemaldatud
- [ ] `GET /api/training/{trainingId}` tagastab `lecturers` järjekorras
- [ ] `POST` / `PUT` võtavad `lecturerIds`, kirjutavad seosed üle, järjekord = `sort_order`
- [ ] Kustutatud / olematu uus koolitaja → 404; juba seotud kustutatud koolitaja `PUT`-is lubatud
- [ ] Olemasolevad testid uuendatud, uued testid lisatud
- [ ] Frontend: vt `docs/tasks/frontend/training-form-lecturers.md`
