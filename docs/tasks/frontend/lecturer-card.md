# Jagatud koolitaja kaart

**Komponent:** `components/common/LecturerCard.vue`; pilt: `LecturerAvatar.vue`.
**Kasutavad vaated:** `/course` ja `/training`, parema veeru „Koolitajad“.
**Roll:** kõik rollid.

## Andmed ja vastutus

Kaart saab kohustusliku Object prop-i `lecturerSummary`:

```json
{
  "lecturerId": 1,
  "fullName": "Rain Tüür",
  "title": "Lektor/konsultant",
  "shortDescription": "Java koolitaja.",
  "photoVersion": 1784106000
}
```

Andmed pärinevad `/api/course-summary/{courseId}` või
`/api/training-summary/{trainingId}` vastuse `lecturers` massiivist.
`LecturerSummaryDto` on jagatud `controller/common/dto` klass; sama DTO on
kasutusel `/lecturers` nimekirja ning olemasoleva üksiku koolitaja teenuses.

Komponent JSON-päringuid ei tee, ei hoia eraldi laadimisolekut ega väljasta
`event-lecturer-not-found` sündmust. Vanemvaade laadib andmed keele või ID
muutumisel. Backend jätab kustutatud koolitajad välja, säilitab seose
sort_order järjekorra ja valib kaardi tekstid contentLang keeles, puudumisel
põhikeeles. Tõlketa koolitaja nimi ning foto võivad olla olemas.

## Kuvamine

- `LecturerAvatar`: 56 px ümar pilt; olemasolev
  `/api/lecturer/{lecturerId}/photo?v={photoVersion}` pilditeenus.
- `photoVersion=null`: PhUser kohatäide, pildipäringut ei tehta.
- Nimi paksus kirjas, ametinimetus väiksemas hallis kirjas, lühikirjeldus.
- Kogu kaart on link `/lecturer?lecturerId={id}&returnTo={lähtevaate fullPath}`.
- Vanemvaade peidab kogu jaotise, kui koondvastuse `lecturers` on tühi.

Mockid: [koolituse detail](../../mock-wireframe/loo-mock-vaade/training-view/training-view-labimang.html),
[toimumiskorra detail](../../mock-wireframe/loo-mock-vaade/courses-view/courses-view-labimang.html).
