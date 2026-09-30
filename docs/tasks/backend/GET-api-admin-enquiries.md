# Admini päringute nimekiri

**Teenus:** `GET /api/admin-enquiries`

**Kasutav vaade:** `AdminEnquiriesView.vue` (`/admin-enquiries`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-enquiries-view-markmed.md`.

Eeldab taski `enquiry-db-changes.md`.

## Sisend

**Query parameetrid:**

| Nimi | Tüüp | Kirjeldus |
|---|---|---|
| `contentLang` | String | koolituse ja vormi nime keel (`et` / `en`) |
| `includeHandled` | Boolean | `true` = ka käsitletud päringud (valikuline, vaikimisi `false`) |

## Väljund

**Response (200 OK):** `AdminEnquirySummaryDto` list

```json
[
  {
    "enquiryId": 4,
    "createdAt": "2026-09-28T13:40:00Z",
    "fullName": "Martin Kask",
    "email": "martin.kask@example.com",
    "companyName": null,
    "trainingTitle": "Java algkursus",
    "courseStartDate": "2026-11-16",
    "courseEndDate": "2026-11-20",
    "optionName": "Veebipõhine",
    "status": "U"
  },
  ...
]
```

## Eesmärk

Admin näeb huviliste päringuid, uusimad eespool. Vaikimisi ainult uued (`status = "U"`), lülitiga ka käsitletud. Otsing ja sorteerimine on frontendis.

## Seotud andmebaasi tabelid

View `admin_enquiry_summary` (`content_language_code = contentLang`), järjestus `created_at DESC, enquiry_id DESC`. Sõnumit ja telefoni nimekirjas ei tagastata. Tundmatu `contentLang` → tühi list.

Näidisandmed: vaikimisi 3 päringut (4, 2, 1), `includeHandled=true` → 4.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveripoolne viga | 500 | Standardne vea response body |

## Vastuvõtu kriteeriumid

- [ ] Endpoint olemas, `includeHandled` vaikimisi `false`
- [ ] Uusimad eespool; nimed `contentLang` keeles, puudumisel põhikeeles
- [ ] Teenusel on automaattestid
