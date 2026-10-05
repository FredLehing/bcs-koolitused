# AdminEnquiryView.vue — märkmed

Ühe huvilise päringu vaade koos staatuse muutmisega. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-enquiries-view/admin-enquiries-view-skeemid.md`. Interaktiivne läbimäng: `admin-enquiries-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminEnquiryView.vue
Frontend rada: /admin-enquiry?enquiryId={id}

Vaatega seotud lisainfo:
Kaart "Päring": Saabunud, Staatus, Koolitus (link avalikule lehele /training), Toimumiskord (või "Toimumiskorda pole valitud"), Ettevõte, Sõnum. Kaart "Kontakt": Nimi, E-post (mailto), Telefon (tel). Keele vahetusel laaditakse päring uuesti.
Uue päringu korral nupp "Märgi käsitletuks" (PUT /api/enquiry/{enquiryId}/handle), käsitletu korral "Märgi uueks" (PUT /api/enquiry/{enquiryId}/reopen); pärast seda laaditakse päring uuesti ja näidatakse eduteadet.
Kiirnupp "Koolituste päringud" → /admin-enquiries. Olematu päring → üldine veavaade.
```

## API märkmed — GET /api/admin-enquiry/{enquiryId}

```text
API: GET /api/admin-enquiry/{enquiryId}

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")

Response (200):
AdminEnquiryDto.java
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
  "companyName": null,
  "message": "Kas veebis osalejad saavad hiljem ka salvestust vaadata?",
  "fullName": "Martin Kask",
  "email": "martin.kask@example.com",
  "phone": "+37253344556"
}

API teenuse lisainfo:
Sama view admin_enquiry_summary (enquiry_id + contentLang). trainingTranslationId = kuvatud tõlke ID (link avalikule koolituse lehele). courseId, courseStartDate, courseEndDate ja companyName võivad olla null.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'enquiryId' väärtusega: 123"
```

## API märkmed — PUT /api/enquiry/{enquiryId}/handle

```text
API: PUT /api/enquiry/{enquiryId}/handle

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus: määrab enquiry.status = "H" (EnquiryStatus.HANDLED) ja uuendab updated_at. Juba käsitletud päringu korral midagi ei muutu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'enquiryId' väärtusega: 123"
```

## API märkmed — PUT /api/enquiry/{enquiryId}/reopen

```text
API: PUT /api/enquiry/{enquiryId}/reopen

Response (200): NONE

API teenuse lisainfo:
Tegevusteenus: määrab enquiry.status = "U" (EnquiryStatus.NEW) ja uuendab updated_at. Uue päringu korral midagi ei muutu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'enquiryId' väärtusega: 123"
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
