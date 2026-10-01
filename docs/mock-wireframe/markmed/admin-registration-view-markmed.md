# AdminRegistrationView.vue — märkmed

Ühe registreerumise vaatamine ja muutmine (staatus, tasumine, sülearvuti, admini märkmed). Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-registrations-view/admin-registrations-view-skeemid.md`. Interaktiivne läbimäng: `admin-registrations-view-labimang.html`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: AdminRegistrationView.vue
Frontend rada: /admin-registration?courseParticipantId={id}

Vaatega seotud lisainfo:
Pealkiri "Registreerumine", all osaleja nimi. Ülal tagasilink: kui URL-is on returnTo (nt silm /admin-course osalejate tabelis), siis "← Tagasi" → returnTo (ainult sisemine rada, NavigationService.isInternalPath), muidu "← Kõik registreerumised" → /admin-registrations.
Kaart "Osaleja" (ainult lugemiseks, profiil on ühine kõigile osaleja registreerumistele): Nimi, E-post (mailto), Telefon (tel), Konto e-post (ainult kui erineb profiili e-postist). Kaart "Toimumiskord" (ainult lugemiseks): Koolitus, Toimumisaeg, Staatus (CourseStatusBadge + "Toimunud"), link "Ava toimumiskord" → /admin-course?courseId={id}.
Kaart "Registreerumine" (vorm): Staatus raadionuppudega Registreerunud / Loobunud; lülitid "Tasunud" ja "Vajab sülearvutit"; "Osaleja lisainfo" ainult lugemiseks (puudumisel "—"); "Admini märkmed" (textarea); Registreerus ja Viimati muudetud. Nupud "Salvesta" (PUT /api/admin-registration/{courseParticipantId}) ja "Tühista" (taastab laaditud väärtused).
Staatuse muutmisel küsitakse enne salvestamist kinnitust (ConfirmModal): "Märgi osaleja loobunuks?" / "Taasta registreerumine?". Täis toimumiskorrale taastamine on lubatud. Pärast salvestamist laaditakse registreerumine uuesti ja näidatakse samas vaates eduteadet "Registreerumine salvestatud". Keele vahetusel laaditakse andmed uuesti. Olematu registreerumine → üldine veavaade.
```

## API märkmed — GET /api/admin-registration/{courseParticipantId}

```text
API: GET /api/admin-registration/{courseParticipantId}

Query parameetrid:
contentLang: String — koolituse nime keel ("et"/"en")

Response (200):
AdminRegistrationDto.java
{
  "courseParticipantId": 1,
  "status": "R",
  "hasPaid": true,
  "requiresLaptop": true,
  "notes": "Registreerus veebilehe kaudu.",
  "adminNotes": null,
  "createdAt": "2026-09-10T09:00:00Z",
  "updatedAt": "2026-09-10T09:00:00Z",
  "participantName": "Anna Saar",
  "email": "anna.saar@example.com",
  "phone": "+37256789012",
  "accountEmail": "kasutaja@vali-it.ee",
  "courseId": 1,
  "trainingTitle": "Java algkursus",
  "courseStartDate": "2026-10-05",
  "courseEndDate": "2026-10-09",
  "courseStatus": "O",
  "isPast": false
}

API teenuse lisainfo:
Üks course_participant rida koos osaleja (participant.name, profile.email, profile.phone, user.email → accountEmail) ja toimumiskorra infoga (course + koolituse nimi contentLang keeles, puudumisel põhikeeles). notes = osaleja enda sisestatud lisainfo (tühi string, kui puudub); adminNotes = admini märkmed (null, kui puudub). courseStatus: "U" mustand, "O" avatud, "F" täis, "X" tühistatud. isPast = course.end_date < täna.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123"
```

## API märkmed — PUT /api/admin-registration/{courseParticipantId}

```text
API: PUT /api/admin-registration/{courseParticipantId}

Request body:
AdminRegistrationUpdateRequest.java
{
  "status": "C",
  "hasPaid": false,
  "requiresLaptop": true,
  "adminNotes": "Teatas telefoni teel 25.09."
}

Väljad: status* ("R"/"C"), hasPaid*, requiresLaptop*, adminNotes (valikuline)

Response (200): NONE

API teenuse lisainfo:
Muudab ainult registreerumise enda välju (course_participant.status, has_paid, requires_laptop, admin_notes) ja uuendab updated_at. adminNotes trimmitakse, tühi → null. Osaleja lisainfot (notes) ja profiili ei muudeta. Taastamine (C → R) on lubatud ka täis toimumiskorrale (status "F") — COURSE_FULL kontrolli pole, sest mahutavust veel ei modelleerita. Tasumise väärtus jääb loobumisel alles. Staatuse muutus mõjutab GET /api/admin-courses arve (participantCount ja paidCount loevad ainult "R"). Kustutamist pole.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'courseParticipantId' väärtusega: 123"
```
