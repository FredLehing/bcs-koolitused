# Ühe päringu admini vaade

**Teenus:** `GET /api/admin-enquiry/{enquiryId}`

**Kasutav vaade:** `AdminEnquiryView.vue` (`/admin-enquiry?enquiryId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-enquiry-view-markmed.md`.

Eeldab taski `enquiry-db-changes.md`.

## Sisend

**Path variable:** `enquiryId` (Integer). **Query parameeter:** `contentLang` (String).

## Väljund

**Response (200 OK):** `AdminEnquiryDto`

```json
{
  "enquiryId": 4,
  "createdAt": "2026-09-28T13:40:00Z",
  "status": "U",
  "trainingId": 1,
  "trainingTranslationId": 1,
  "trainingTitle": "Java algkursus",
  "courseId": 5,
  "courseStartDate": "2026-11-16",
  "courseEndDate": "2026-11-20",
  "optionName": "Veebipõhine",
  "companyName": null,
  "message": "Kas veebis osalejad saavad hiljem ka salvestust vaadata?",
  "fullName": "Martin Kask",
  "email": "martin.kask@example.com",
  "phone": "+37253344556"
}
```

## Eesmärk

Päringu kõik andmed ja kontakt ühe päringuga. `trainingTranslationId` on kuvatud tõlke ID (link avalikule koolituse lehele).

## Seotud andmebaasi tabelid

View `admin_enquiry_summary` (`enquiry_id` + `content_language_code`). Olematu päring kontrollitakse `EnquiryService.getValidEnquiryBy(enquiryId)` kaudu.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `enquiryId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'enquiryId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] Endpoint tagastab `AdminEnquiryDto`
- [ ] Olematu päring → 404
- [ ] Teenusel on automaattestid
