# "Minu koolitused": tagasiside nupp

**Vaade:** `ParticipantCoursesView.vue` (`/participant-courses`)

**Roll:** Kasutaja (osaleja)

**Vaste mockupis:** interaktiivne läbimäng `docs/mock-wireframe/loo-mock-vaade/participant-feedback-form-view/participant-feedback-form-view-labimang.html` (algvaade "Minu koolitused")

> Mockupi pilt lisatakse hiljem (Balsamiq AI käsk: `participant-feedback-form-view-skeemid.md`, jaotis 9, "ParticipantCoursesView — tagasiside nupud").

Taustaks: märkmed `docs/mock-wireframe/markmed/participant-courses-view-markmed.md`, skeemid `participant-feedback-form-view-skeemid.md` ("Avamine"). Eeldab backend taski `registration-feedback-flags.md`.

## Kasutajavoog

Osaleja avab "Minu koolitused" ja näeb toimunud koolituse real nuppu "Anna tagasisidet" või "Vaata tagasisidet", mis viib tagasiside vormile.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Anna tagasisidet" | nupp | kui `canGiveFeedback` ja `!hasFeedback`; → `/participant-feedback-form?courseParticipantId={id}` |
| "Vaata tagasisidet" | nupp | kui `canGiveFeedback` ja `hasFeedback`; → sama rada |

## Käitumine

- Nupp on `ParticipantRegistrationItem.vue` rea paremas servas, samas kohas kui "Loobu" (mõlemat korraga ei esine).
- Nupp kuvatakse ka plokis "Tulevased", kui rida seal on (viimane koolituspäev, `end_date = täna`).
- Loobunud, tühistatud ja tulevase koolituse real nuppu pole (`canGiveFeedback = false`).

## API kutsed

- `GET /api/user/{userId}/registrations?contentLang=` → `MyRegistrationDto[]`, uued väljad `canGiveFeedback`, `hasFeedback`

## Komponendid ja failistruktuur

- `components/profile/ParticipantRegistrationItem.vue` (muudetakse); locale'id `et.json`, `en.json` (muudetakse)

## Vastuvõtu kriteeriumid

- [ ] Seed'iga (Anna): registreerumine 2 → "Anna tagasisidet", 10 → "Vaata tagasisidet", 1 → nuppu pole
- [ ] Nupp viib `/participant-feedback-form?courseParticipantId={id}`
- [ ] Tekstid et/en
