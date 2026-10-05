# Osaleja tagasiside lugemine

**Teenus:** `GET /api/user/{userId}/registration/{courseParticipantId}/feedback`

**Kasutav vaade:** `ParticipantFeedbackFormView.vue` (`/participant-feedback-form?courseParticipantId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md`.

Eeldab taske `feedback-db-changes.md` ja `registration-feedback-flags.md` (reegel `canGiveFeedback`).

## Sisend

**Path variable'id:** `userId` (Integer), `courseParticipantId` (Integer).

**Query parameeter:** `contentLang` (String) — kriteeriumide ja koolituse nime keel (`et` / `en`).

## Väljund

**Response (200 OK):** `ParticipantFeedbackDto`

```json
{
  "courseParticipantId": 10,
  "trainingTitle": "Java algkursus",
  "startDate": "2026-06-08",
  "endDate": "2026-06-12",
  "hasFeedback": true,
  "createdAt": "2026-06-12T16:40:00",
  "updatedAt": "2026-06-12T16:40:00",
  "criteria": [
    {
      "feedbackCriteriaId": 1,
      "title": "Koolitus vastas ootustele",
      "description": "Koolituse sisu, tase ja maht vastasid koolituse kirjelduse põhjal tekkinud ootustele.",
      "score": 9,
      "feedbackText": null
    },
    ...
  ]
}
```

Lihtväljad enne massiivi (`backend/CLAUDE.md`).

## Eesmärk

Üks päring annab vormile kõik vajaliku mõlemas olekus:

- **Tagasisidet pole** (`hasFeedback = false`): `criteria` = aktiivsed (`A`) kriteeriumid; `score`, `feedbackText`, `createdAt`, `updatedAt` on `null`.
- **Tagasiside on** (`hasFeedback = true`): `criteria` = aktiivsed kriteeriumid **ja** kustutatud (`D`), millele on vastatud. Pärast tagasiside andmist lisatud kriteeriumi `score` ja `feedbackText` on `null`.
- Järjestus `sequence`, võrdsuse korral `id`.
- `title`, `description` ja `trainingTitle` `contentLang` keeles, tõlke puudumisel põhikeeles.
- `createdAt` = `feedback.created_at`; `updatedAt` = vastuste `MAX(course_participant_feedback.updated_at)` (mitte `feedback.updated_at` — vt skeemide "Tehniline märkus").

Kontrollid (järjekorras, ühine POST/PUT-iga):

1. `userId` olemas (`userService.getValidUserBy`).
2. Registreerumine olemas (`getValidCourseParticipantBy`).
3. Registreerumine kuulub kasutaja osalejale (nagu `cancelMyRegistration`).
4. `canGiveFeedback` (`status = 'R'`, `end_date <= täna`, toimumiskord pole `X` ega `D`).

## Seotud andmebaasi tabelid

`course_participant`, `course`, `training_translation`, `feedback`, `course_participant_feedback`, `feedback_criteria`, `feedback_criteria_translation`, `language`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `userId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `courseParticipantId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Registreerumine pole selle kasutaja oma | 404 Not Found | `{ "message": "Registreerumist ei leitud", "errorCode": "REGISTRATION_NOT_FOUND" }` |
| Loobunud, toimumiskord pole lõppenud või on tühistatud/kustutatud | 403 Forbidden | `{ "message": "Sellele koolitusele ei saa tagasisidet anda", "errorCode": "FEEDBACK_NOT_ALLOWED" }` |

## Vastuvõtu kriteeriumid

- [ ] Seed: registreerumine 2 → `hasFeedback = false`, 5 kriteeriumi `score = null`; 10 → `hasFeedback = true`, hinded 9, 10, 8, 7, 9
- [ ] `contentLang=en` → ingliskeelsed tekstid; tõlke puudumisel (nt `ru`) eestikeelsed
- [ ] Kustutatud kriteerium, millele on vastatud, on vastuses; vastamata kustutatud pole
- [ ] Neli veaolukorda nagu tabelis
- [ ] Teenusel on automaattestid
