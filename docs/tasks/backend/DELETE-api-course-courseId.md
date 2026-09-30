# Toimumiskorra kustutamine (soft delete)

**Teenus:** `DELETE /api/course/{courseId}`

**Kasutavad vaated:** `AdminTrainingCoursesView.vue` (prügikast tabelis), `CourseFormView.vue` (prügikast muutmisel) — komponent `CourseDeleteButton.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-training-courses-view/admin-training-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-training-courses-view-markmed.md` (JSON-näited).

Eeldab taski `course-db-changes.md`.

## Sisend

`courseId` (path).

## Väljund

**Response (200 OK):** tühi. `course.status = "D"` (`CourseStatus.DELETED`), `updated_at` uueneb. Osalejaid ja koolitajaid ei kustutata. Kustutatud toimumiskord kaob kalendrist (taastamist praegu pole). Kustutada saab ka osalejatega ja möödunud toimumiskorda — frontend hoiatab osalejate korral ja soovitab tühistamist. Juba kustutatud → 200, midagi ei muutu.

## Eesmärk

Admin eemaldab ekslikult lisatud toimumiskorra.

## Seotud andmebaasi tabelid

`course`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Toimumiskorda pole | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveripoolne viga | 500 Internal Server Error | Standardne vea response body (vastavalt projekti globaalsele error handler'ile) |

## Vastuvõtu kriteeriumid

- [ ] `status = "D"`, idempotentne
- [ ] Olematu → 404
- [ ] Teenusel on automaattestid
