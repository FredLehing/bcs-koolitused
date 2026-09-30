# Päringu märkimine käsitletuks / uueks

**Teenus:** `PUT /api/enquiry/{enquiryId}/handle` ja `PUT /api/enquiry/{enquiryId}/reopen`

**Kasutav vaade:** `AdminEnquiryView.vue` (`/admin-enquiry?enquiryId={id}`)

> Mockupi pilt lisatakse hiljem. Seni vt läbimängu `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-labimang.html` ja märkmeid `docs/mock-wireframe/markmed/admin-enquiry-view-markmed.md`.

Eeldab taski `enquiry-db-changes.md`. Kaks väikest tegevusteenust ühes taskis (sama muster nagu koolituse `publish` / `unpublish`).

## Sisend

**Path variable:** `enquiryId` (Integer). Body't pole.

## Väljund

**Response (200 OK):** tühi vastus.

## Eesmärk

- `handle` → `enquiry.status = "H"` (`EnquiryStatus.HANDLED`) — nupp "Märgi käsitletuks".
- `reopen` → `enquiry.status = "U"` (`EnquiryStatus.NEW`) — nupp "Märgi uueks".
- `updated_at` uueneb auditeerimisega. Sama staatus → midagi ei muutu (idempotentne).

## Seotud andmebaasi tabelid

`enquiry` (`status`, `updated_at`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `enquiryId` ei leidu | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'enquiryId' väärtusega: 123", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |

## Vastuvõtu kriteeriumid

- [ ] Mõlemad endpointid muudavad staatust
- [ ] Sama staatuse korral salvestamist ei toimu
- [ ] Olematu päring → 404
- [ ] Teenustel on automaattestid
