# Päringu saatmine ("Küsi lisainfot")

**Teenus:** `POST /api/enquiry`

**Kasutav vaade:** `CourseView.vue` → `EnquiryModal.vue`

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/course-view-markmed.md` (API märge on sama sisuga).

Eeldab taski `courses-calendar-db-changes.md` (ja juba tehtud päringute töid).

## Sisend

**Request body:** `EnquiryCreateRequest`

```json
{
  "trainingId": 1, "courseId": 1,
  "firstName": "Kati", "lastName": "Karu", "email": "kati.karu@example.com", "phone": "+37255512300",
  "companyName": null, "message": "Kas kursusele saab tulla ka ilma eelteadmisteta?"
}
```

Kohustuslikud: `trainingId`, `firstName`, `lastName`, `email` (`@Email`), `phone`, `message` (`@Size(max = 255)`). `courseId` ja `companyName` valikulised (tühi `companyName` → `null`).

## Väljund

**Response (200 OK):** NONE

## Eesmärk

Külastaja (sisselogimiseta) küsib toimumiskorra kohta lisainfot; päring ilmub adminile `/admin-enquiries` ja `/admin-course` "Huvilised" tabelisse.

## Seotud andmebaasi tabelid

Iga päring loob **uue** `profile` rea (olemasolevat e-posti järgi ei otsita) ja `enquiry` rea (`status = 'U'`). Koolitus peab olema publitseeritud; `courseId` korral selle koolituse `O`/`F` toimumiskord.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Kohustuslik väli puudu / vigane | 400 | valideerimise viga |
| Olematu trainingId | 404 | `PRIMARY_KEY_NOT_FOUND` — "Ei leidnud primary keyd 'trainingId' väärtusega: 123" |
| Olematu või mitteavalik `courseId` | 404 | `PRIMARY_KEY_NOT_FOUND` ('courseId') |

## Vastuvõtu kriteeriumid

- [ ] Profiil + päring salvestuvad, päring on `U`
- [ ] Valideerimine ja 404-d töötavad
- [ ] Teenusel on automaattestid
