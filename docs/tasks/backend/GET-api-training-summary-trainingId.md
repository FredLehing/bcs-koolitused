# GET /api/training-summary/{trainingId}

**Seis:** implementeeritud (`TrainingSummaryController`, `TrainingPageService`, `TrainingPageMapper`, `TrainingPageDto`).

## Eesmärk

Koolituse detaili `/training` koondandmed ühe päringuga: tekst, tõlkepõhise
PDF-i metainfo, koolituse andmed, koolitajad ja avalikud tulevased toimumiskorrad.
Varem sai vaade eraldi tekstid ja koolitajad; muud kaardid olid kohatäited.

## Sisend ja tõlke valik

- `trainingId`: koolituse ID (path).
- `contentLang`: kasutajaliidese keele kood (kohustuslik query).
- `trainingTranslationId`: kindla tõlke eelvaade (valikuline query).

Valik: koolitusele kuuluv `trainingTranslationId` → `contentLang` → põhikeel.
Olematu või teisele koolitusele kuuluv tõlke ID jäetakse kõrvale, nagu senises
frontendis. Vormi mustandi (U) eelvaade säilib. Kustutatud (D) või olematu
koolitus ning koolitus sobiva tõlketa annavad 404. Vigane ID tüüp annab 400.

Kui `contentLang` tõlge puudub ja kasutatakse põhikeele varuteksti, on
`isMainLanguageFallback=true` ning PDF-i väljad null. Teise keele PDF-i selle
asemel ei näidata. Konkreetse tõlke eelvaade kasutab selle tõlke PDF-i.
Olemasoleval valitud tõlkel puuduva PDF-i asemel ei otsita mõne teise tõlke faili.

## Vastus: TrainingPageDto

```json
{
  "trainingId": 1,
  "trainingTranslationId": 1,
  "isMainLanguageFallback": false,
  "title": "Java algkursus",
  "shortDescription": "Java programmeerimise alused algajatele.",
  "description": "<p>Koolituse kirjeldus.</p>",
  "categoryName": "Programmeerimine",
  "trainingLanguageCode": "et",
  "trainingLanguageFlagIconCode": "ee",
  "locationName": "BCS Koolitus",
  "isOnline": false,
  "isOrderable": true,
  "isPromoted": true,
  "curriculumFileName": "Java-algkursus_oppekava_et.pdf",
  "curriculumFileSize": 419430,
  "fundingTypes": [{"fundingTypeId": 1, "fundingTypeName": "Töötukassa"}],
  "lecturers": [{"lecturerId": 1, "fullName": "Rain Tüür", "title": "Lektor/konsultant", "shortDescription": "Java koolitaja.", "photoVersion": 1784106000}],
  "upcomingCourses": [{"courseId": 1, "startDate": "2026-10-05", "endDate": "2026-10-09", "status": "O", "isOnSite": true, "isOnline": false}]
}
```

Kategooria ja rahastuse nimed valitakse kuvatava tõlke keeles; puuduv kategooria
on null, puuduvad rahastuse/koolitaja/toimumiskorra read on tühjad massiivid.
Õppekeel (`trainingLanguageCode`, lipp) on koolituse enda atribuut ja ei muutu
sisu tõlke valikul. Asukoht ja `isOnline` tulevad koolituse asukoha kirjest;
toimumiskordade tegelik osalemisviis tuleb igast toimumiskorrast eraldi.

`upcomingCourses` kasutab sama `public_course_summary` view'd nagu `/course`:
koolitus P, toimumiskord O/F, `start_date >= current_date`, alguse ja ID järgi.
Mustandi, tühistatud, kustutatud ja möödunud toimumiskordi ei kuvata.
`UpcomingCourseDto` on mõlema ressursi jaoks jagatud `controller/common/dto` klass.

PDF-i baite koondpäring ei loe. Nimi ja suurus võetakse olemasolevast
projektsioonist; allalaadimine kasutab muutmata olemasolevat
`GET /api/training-translation/{trainingTranslationId}/curriculum` teenust.
DB kirjutamisi ega skeemimuudatusi ei lisandu.

## Koolitajakaardid ühe JSON-päringuga

`lecturers` kasutab jagatud `LecturerSummaryDto` klassi (`controller/common/dto`).
Iga kirje sisaldab fullName, title, shortDescription ja photoVersion.
Koolitajate tekstid on kasutajaliidese contentLang keeles (puudumisel põhikeeles),
ka konkreetse koolituse tõlke eelvaates. Seoste training_lecturer sort_order
järjekord säilib, kustutatud koolitajad jäetakse välja. Kui tõlkeid pole,
jäävad kaardi tekstiväljad null; nimi ja foto võivad siiski olla olemas.
Tõlked ja fotode versioonid loetakse hulgi, ilma iga koolitaja eraldi teenusekutseta.
LecturerCard on ainult kuvamise komponent, fotod laaditakse eraldi pilditeenusest.

## Vastuvõtukriteeriumid

- Õige tõlge, tekstivaruvariant ja konkreetse tõlke eelvaade on eristatavad.
- Võõra tõlke ID ei ava selle teksti ega PDF-i.
- Õppekava puudumine või teise keele varutekst tagastab null PDF-i väljad.
- Avalike toimumiskordade valik on sama nagu `/course` lehel.
- Mustandi eelvaade toimib ja kustutatud koolitus annab 404.

Vaate [task](../frontend/training-view.md), [skeemid](../../mock-wireframe/loo-mock-vaade/training-view/training-view-skeemid.md).
