# Osaleja tagasiside lisamine

**Teenus:** `POST /api/user/{userId}/registration/{courseParticipantId}/feedback`

**Kasutav vaade:** `ParticipantFeedbackFormView.vue` (`/participant-feedback-form?courseParticipantId={id}`), olek "uus" (nupp "Lisa tagasiside")

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md`.

Eeldab taske `feedback-db-changes.md` ja `GET-api-user-userId-registration-courseParticipantId-feedback.md` (ühised kontrollid).

## Sisend

**Path variable'id:** `userId` (Integer), `courseParticipantId` (Integer).

**Request body:** `FeedbackRequestDto`

```json
{
  "answers": [
    {
      "feedbackCriteriaId": 1,
      "score": 9,
      "feedbackText": "Praktilisi näiteid oleks võinud olla rohkem"
    },
    ...
  ]
}
```

- `answers` — kohustuslik (`@NotEmpty`, `@Valid`).
- `feedbackCriteriaId` — kohustuslik.
- `score` — kohustuslik, `@Min(1) @Max(10)`.
- `feedbackText` — valikuline, `@Size(max = 10000)`.

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Osaleja annab toimunud koolitusele esimest korda tagasiside. Ühes transaktsioonis:

1. Kontrollid nagu GET-il (kasutaja, registreerumine, omanik, `canGiveFeedback`).
2. `feedback` rida juba olemas → `FEEDBACK_ALREADY_EXISTS`.
3. `answers` peab sisaldama **täpselt kõiki aktiivseid** kriteeriume, igaüht üks kord (puudub, üleliigne, tundmatu või kordus → `FEEDBACK_CRITERIA_CHANGED`).
4. Luuakse `feedback` (`status = 'N'`) ja iga vastuse kohta `course_participant_feedback` rida.
5. Tühi või ainult tühikutest `feedbackText` salvestatakse `NULL`-ina.

## Seotud andmebaasi tabelid

`feedback`, `course_participant_feedback`, `feedback_criteria`, `course_participant`, `course`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `userId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `courseParticipantId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Registreerumine pole selle kasutaja oma | 404 Not Found | `{ "message": "Registreerumist ei leitud", "errorCode": "REGISTRATION_NOT_FOUND" }` |
| Loobunud, toimumiskord pole lõppenud või on tühistatud/kustutatud | 403 Forbidden | `{ "message": "Sellele koolitusele ei saa tagasisidet anda", "errorCode": "FEEDBACK_NOT_ALLOWED" }` |
| Tagasiside on juba antud | 403 Forbidden | `{ "message": "Tagasiside on juba antud", "errorCode": "FEEDBACK_ALREADY_EXISTS" }` |
| `answers` ei vasta aktiivsetele kriteeriumidele | 403 Forbidden | `{ "message": "Tagasiside küsimused on vahepeal muutunud, laadi leht uuesti", "errorCode": "FEEDBACK_CRITERIA_CHANGED" }` |
| Hinne puudub / väljaspool 1–10, kommentaar > 10000, `answers` tühi | 400 Bad Request | `INCORRECT_INPUT` |

## Vastuvõtu kriteeriumid

- [ ] Seed: POST registreerumisele 2 viie hindega → `feedback` (`N`) + 5 vastust; GET näitab `hasFeedback = true`
- [ ] Teine POST samale registreerumisele → 403 `FEEDBACK_ALREADY_EXISTS`
- [ ] Puuduv või üleliigne kriteerium → 403 `FEEDBACK_CRITERIA_CHANGED`
- [ ] Tühi kommentaar salvestub `NULL`-ina
- [ ] Teenusel on automaattestid
