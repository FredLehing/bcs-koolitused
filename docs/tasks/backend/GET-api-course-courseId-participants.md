# Toimumiskorra osalejad (admin)

**Teenus:** `GET /api/course/{courseId}/participants`

**Kasutav vaade:** `AdminCourseView.vue`, tabel "Osalejad"

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/admin-all-courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md`.

## Sisend

**Path variable:** `courseId` (Integer).

## Väljund

**Response (200 OK):** `CourseParticipantDto` list

```json
[
  {
    "courseParticipantId": 1, "participantName": "Anna Saar",
    "email": "anna.saar@example.com", "phone": "+37256789012",
    "registeredAt": "2026-09-10T09:00:00Z",
    "hasPaid": true, "requiresLaptop": true, "status": "R",
    "notes": "Registreerus veebilehe kaudu."
  }
]
```

## Eesmärk

Admin näeb toimumiskorra osalejaid (ka loobunuid); otsing ja filtrid on frontendis. Tabel on ainult lugemiseks.

## Seotud andmebaasi tabelid

`course_participant` + `participant` (`name`) + `profile` (`email`, `phone`); järjestus `created_at`. Tagastatakse ka `C` read.

Näidisandmed: toimumiskord 1 → 4 rida (neist 1 `C`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Olematu courseId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'courseId' väärtusega: 123" |
| Kustutatud toimumiskord | 404 | sama mis olematu |

## Vastuvõtu kriteeriumid

- [ ] Kõik osalejad registreerumise järjekorras, kontaktandmed profiilist
- [ ] Tühja nimekirja korral `[]`
- [ ] Teenusel on automaattestid
