# "Minu koolitused": tagasiside väljad registreerumisel

**Teenus:** `GET /api/user/{userId}/registrations` (olemas, muutub)

**Kasutav vaade:** `ParticipantCoursesView.vue` (`/participant-courses`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/participant-courses-view-markmed.md`.

Eeldab taski `feedback-db-changes.md`.

## Sisend

Muutumatu: path variable `userId` (Integer), query parameeter `contentLang` (String).

## Väljund

**Response (200 OK):** `MyRegistrationDto` list, lisanduvad väljad `canGiveFeedback` ja `hasFeedback` (pärast `canCancel`-i):

```json
[
  {
    "courseParticipantId": 2,
    "courseId": 3,
    "trainingTitle": "Java algkursus",
    "startDate": "2026-09-07",
    "endDate": "2026-09-11",
    "isOnSite": true,
    "isOnline": false,
    "courseStatus": "O",
    "status": "R",
    "hasPaid": true,
    "isPast": true,
    "canCancel": false,
    "canGiveFeedback": true,
    "hasFeedback": false
  },
  ...
]
```

## Eesmärk

Vaade näitab real nuppu "Anna tagasisidet" / "Vaata tagasisidet" ainult siis, kui tagasiside andmine on lubatud.

- `canGiveFeedback` = `status = 'R'` **ja** `course.end_date <= täna` **ja** `course.status` ei ole `X` ega `D`. Tasumine (`has_paid`) ei loe. Arvutab `CourseParticipantService` (nagu `canCancel`).
- `hasFeedback` = registreerumisel on `feedback` rida (`feedback.course_participant_id`).
- Sama reegel (`canGiveFeedback`) kontrollitakse ka tagasiside teenustes (`GET`/`POST`/`PUT …/feedback`) — hoia see ühes kohas.
- Ülejäänud käitumine (järjestus, kustutatud toimumiskorrad, tõlge, `isPast`, `canCancel`) ei muutu.

## Seotud andmebaasi tabelid

`course_participant`, `course`, `feedback` (ainult lugemine).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `userId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] Seed'iga (Anna, `userId = 2`): registreerumine 2 → `canGiveFeedback = true`, `hasFeedback = false`; 10 → `true`, `true`; 1 (tulevane) → `false`, `false`
- [ ] Viimasel koolituspäeval (`end_date = täna`) on `canGiveFeedback = true`
- [ ] Loobunud (`C`) ja tühistatud (`X`) toimumiskorra real `canGiveFeedback = false`
- [ ] Olemasolevad testid lähevad läbi; uutel väljadel on automaattestid
