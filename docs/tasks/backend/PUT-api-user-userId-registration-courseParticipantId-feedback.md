# Osaleja tagasiside muutmine

**Teenus:** `PUT /api/user/{userId}/registration/{courseParticipantId}/feedback`

**Kasutav vaade:** `ParticipantFeedbackFormView.vue` (`/participant-feedback-form?courseParticipantId={id}`), olek "muutmine" ("Muuda" → "Salvesta")

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/participant-feedback-form-view-markmed.md`.

Eeldab taske `feedback-db-changes.md`, `GET-api-user-userId-registration-courseParticipantId-feedback.md` ja `POST-api-user-userId-registration-courseParticipantId-feedback.md` (ühised kontrollid, `FeedbackRequestDto`).

## Sisend

**Path variable'id:** `userId` (Integer), `courseParticipantId` (Integer).

**Request body:** `FeedbackRequestDto` (sama mis POST-il)

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

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

Osaleja muudab olemasolevat tagasisidet. Ühes transaktsioonis:

1. Kontrollid nagu GET-il (kasutaja, registreerumine, omanik, `canGiveFeedback`). Muutmisel tähtaega pole.
2. `feedback` rida puudub → `FEEDBACK_NOT_FOUND`.
3. `answers` peab sisaldama **täpselt** aktiivsed kriteeriumid **ja** kustutatud (`D`), millele on vastatud — igaüht üks kord (muidu `FEEDBACK_CRITERIA_CHANGED`).
4. Olemasolevad vastused uuendatakse; pärast tagasiside andmist lisatud kriteeriumi vastus lisatakse uue reana.
5. Staatus: `H` → `U`; `N` ja `U` jäävad samaks.
6. Tühi või ainult tühikutest `feedbackText` salvestatakse `NULL`-ina.

`updated_at` täidab auditeerimine — käsitsi ei seata (`backend/CLAUDE.md`). Osalejale näidatav "Muudetud" tuleb vastuste `updated_at`-ist (GET).

## Seotud andmebaasi tabelid

`feedback`, `course_participant_feedback`, `feedback_criteria`, `course_participant`, `course`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `userId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| `courseParticipantId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Registreerumine pole selle kasutaja oma | 404 Not Found | `{ "message": "Registreerumist ei leitud", "errorCode": "REGISTRATION_NOT_FOUND" }` |
| Loobunud, toimumiskord pole lõppenud või on tühistatud/kustutatud | 403 Forbidden | `{ "message": "Sellele koolitusele ei saa tagasisidet anda", "errorCode": "FEEDBACK_NOT_ALLOWED" }` |
| Tagasisidet pole | 404 Not Found | `{ "message": "Tagasisidet ei leitud", "errorCode": "FEEDBACK_NOT_FOUND" }` |
| `answers` ei vasta vormi kriteeriumidele | 403 Forbidden | `{ "message": "Tagasiside küsimused on vahepeal muutunud, laadi leht uuesti", "errorCode": "FEEDBACK_CRITERIA_CHANGED" }` |
| Hinne puudub / väljaspool 1–10, kommentaar > 255, `answers` tühi | 400 Bad Request | `INCORRECT_INPUT` |

## Vastuvõtu kriteeriumid

- [ ] Seed: PUT registreerumisele 10 muudetud hindega → vastus uueneb, staatus `H` → `U`
- [ ] `N` staatusega tagasiside muutmisel jääb `N`
- [ ] Kustutatud kriteerium, millele on vastatud, peab body's olema ja seda saab muuta
- [ ] Uus kriteerium lisatakse vastusena; selle puudumine → 403 `FEEDBACK_CRITERIA_CHANGED`
- [ ] PUT registreerumisele, millel tagasisidet pole → 404 `FEEDBACK_NOT_FOUND`
- [ ] Teenusel on automaattestid
