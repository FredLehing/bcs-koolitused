# LecturerFormView.vue (state: "new-lecturer") — märkmed

Üks kolmest LecturerFormView oleku märkmete failist (vt ka teisi `lecturer-form-view-state-*-markmed.md` faile). Olekud ja tõlkeloogika on samad mis TrainingFormView-l. Otsused, andmebaasi ettepanek ja skeemid: `docs/mock-wireframe/loo-mock-vaade/admin-lecturers-view/admin-lecturers-view-skeemid.md`. Uute teenuste DTO-d on ettepanek.

## Vaate märkmed

```text
Roll: Admin
Failinimi: LecturerFormView.vue
Frontend rada: /lecturer-form

Vaatega seotud lisainfo:
state: "new-lecturer" — query parameetreid pole. Avaneb AdminLecturersView nupust "+ Lisa uus koolitaja". Pealkiri "Lisa uus koolitaja", lipukesi pole, kiirnupp "Koolitajad" → /admin-lecturers.
Kaart "Koolitaja andmed": Täisnimi *, Pilt (valikuline: "Vali pilt" → eelvaade, "Eemalda"; PNG, JPEG või WebP kuni 2 MB, kontrollitakse enne saatmist). Kaart "Tõlge (et)": Ametinimetus *, Lühikirjeldus *, Kirjeldus * (RichTextEditor). Välja "Ametinimetus" sildi kõrval on "?" ikoon tooltip'iga: "Ametinimetus kuvatakse koolitaja kaardil nime all. Kirjuta lühidalt, millega koolitaja tegeleb, nt „Lektor/konsultant“ või „Projektijuht/lektor“. Iga keele jaoks eraldi tõlge." Lühikirjelduse all vihje "Kuvatakse koolitaja kaardil koolituse lehel ja toimumiskorra juures".
Vead AlertDanger.vue-ga ("Täida kõik kohustuslikud väljad", pildi viga või backendi message). "Lisa" → POST /api/lecturer (userId localStorage'ist). Vastuse järgi router.replace → state "update", eduteade "Koolitaja lisatud".
```

## API märkmed — GET /api/languages

```text
API: GET /api/languages

Response (200):
SystemLanguageDto.java
[
  {
    "languageId": 1,
    "languageCode": "et",
    "languageName": "Eesti",
    "isMainLanguage": true,
    "requiresTranslation": true,
    "flagIconCode": "fi-ee"
  },
  ...
]

API teenuse lisainfo:
Olemasolev teenus. LecturerFormView kasutab seda põhikeele (isMainLanguage = true) ja tõlkelippude (requiresTranslation = true) leidmiseks ning languageId ↔ languageCode teisendamiseks. Uue koolitaja tõlge on alati põhikeeles.

Veateated: —
```

## API märkmed — POST /api/lecturer

```text
API: POST /api/lecturer

Request body:
LecturerCreateRequestDto.java
{
  "userId": 1,
  "fullName": "Kristjan Kuusk",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAg...",
  "photoContentType": "image/png",
  "title": "Andmeinsener",
  "shortDescription": "Koolitab SQL-i ja andmeanalüüsi teemadel.",
  "description": "<p>Kristjan on ehitanud andmelaohooneid ja aruandlussüsteeme.</p>"
}

Response (200):
LecturerCreateResponseDto.java
{
  "lecturerId": 10,
  "lecturerTranslationId": 16
}

API teenuse lisainfo:
Loob koolitaja ühes transaktsioonis: lecturer (status = "A", created_by = userId), lecturer_photo (ainult kui photo ≠ null) ja põhikeele (language.is_main_language) lecturer_translation. Kohustuslikud: userId, fullName (kuni 255), title (ametinimetus, kuni 255), shortDescription (kuni 255), description (HTML, mitte tühi). photo on Base64 või null; photoContentType on kohustuslik, kui photo on olemas. Lubatud tüübid image/png, image/jpeg, image/webp; dekodeeritud pilt kuni 2 MB. Backend normaliseerib pildi: lõikab ruuduks, vähendab 400×400-ks ja salvestab JPEG-ina (content_type = image/jpeg, EXIF eemaldatud). Sama nimega koolitajat ei keelata. Vastuse järgi teeb frontend router.replace → state "update".

Veateated:
HTTP: 403
errorCode: PHOTO_TYPE_NOT_ALLOWED
message: "Lubatud on ainult PNG, JPEG või WebP pilt"

HTTP: 403
errorCode: PHOTO_TOO_LARGE
message: "Pilt on liiga suur, lubatud kuni 2 MB"
```


## Tagasitee — täiendatud navigatsioon

Vaade võtab vastu valikulise `returnTo` query parameetri ja kuvab lingi „← Tagasi“ (`BackLink.vue`). Avamislingid annavad kaasa lähtevaate täieliku URL-i. Tagasilingi puuduv, väline, tundmatu või iseendale osutav siht asendatakse vaate varusihtkohaga. Oleku- ja tõlkevahetus ei kaota tagasiteed. Eraldi nimega nimekirja-/kalendrinupud säilitavad oma sihtkoha. Täpne [kaardistus ja varusihtkohad](../../tasks/frontend/return-to-navigation.md) ning [skeemid](../loo-mock-vaade/return-to-navigation-skeemid.md).
