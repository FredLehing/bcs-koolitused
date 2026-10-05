# PDF-ist koolituse tekstiväljade loomine salvestamata failist

**Teenus:** `POST /api/ai-training/pdf`

**Kasutav vaade:** `TrainingFormView.vue`, `/training-form`, olekud `new-training ja new-translation`

**Vaste mockupis:** [interaktiivne läbimäng](../../mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html). PDF-i ekspordi pildid ei sisalda veel uut AI-nuppu; ajakohane allikas on läbimäng ja olekute märkmed.

## Sisend

Päring on `multipart/form-data`. Väljal `curriculum` saadetakse PDF-fail binaarkujul (mitte Base64 ega JSON). Väli on kohustuslik; kasutatakse vormis valitud salvestamata faili. Tõlke või koolituse ID-d ei saadeta.

Frontend lubab PDF-faile kuni 10 MB. Praegune placeholder ei valideeri faili sisu ega kontrolli ID olemasolu. Spring kontrollib ainult päringu sidumist controlleri parameetritega.

## Väljund

**Response (200 OK):** `AiTrainingContentDto.java` (`title`, `shortDescription`, `description`).

```json
{
  "title": "PDF-ist genereeritud pealkiri (TO BE IMPLEMENTED)",
  "shortDescription": "PDF-ist genereeritud lühikirjeldus (TO BE IMPLEMENTED)",
  "description": "PDF-ist genereeritud kirjeldus (TO BE IMPLEMENTED)"
}
```

Placeholder-väärtused on fikseeritud. Sama DTO-d kasutatakse ka AI tõlke jaoks. Swaggeri operatsiooni pealkiri algab `TO BE IMPLEMENTED`; näidised ja sisend on dokumenteeritud. Tulemus täidab ainult vormi tekste; PDF-faili ennast AI päring ei muuda.

## Eesmärk

Admin vajutab valitud või salvestatud PDF-i kõrval nuppu „Täida vorm PDF + AI abiga“. Käesolevas etapis ühendatakse frontend placeholder-controlleriga, et läbi mängida faili saatmine ja tekstiväljade täitmine. Client → backend → Gemini AI → backend → Client. Sisend ei tule andmebaasist. Praegune controller tagastab otse placeholder-vastuse; Gemini kutse ja DB lugemine lisatakse hiljem.

## Seotud andmebaasi tabelid

Praegune ja planeeritud AI päring ei salvesta andmebaasi. Sisendfail on salvestamata, mistõttu selle teenuse jaoks pole andmebaasi tabelit ega DB näidisandmeid vaja.

Admin salvestab lõpliku tulemuse eraldi „Lisa“, „Lisa tõlge“ või „Salvesta“ nupuga. AI päring ei loo ega uuenda DB ridu.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Kohustuslik curriculum osa puudub või päring ei ole multipart | 400 / 415 (Springi päringu sidumine) | Springi standardne veavastus; kohandatud äriveakoodi selles etapis pole |
| Ootamatu serveriviga | 500 Internal Server Error | Standardne vea response body vastavalt globaalsele veakäsitlusele |

Swaggeris kirjeldatud 400 (vigane PDF), 413 (liiga suur fail), 503 (AI pole saadaval) on tulevase teostuse leping. Placeholder ei viska neid ärivigu ega kasuta selleks fiktiivseid veakoode. Frontend kuvab AI päringu vea vormis ja jätab tekstid ning valitud PDF-i alles.

## Vastuvõtu kriteeriumid

- [x] Meetod on `AiTrainingController` klassis ülaltoodud POST-rajal.
- [x] curriculum on kohustuslik multipart-fail.
- [x] Vastus on 200 ja ühine kolme tekstiväljaga DTO täpsete placeholder-väärtustega.
- [x] Swaggeri kokkuvõttel on `TO BE IMPLEMENTED` eesliide, sisend, vastuse skeem ja tulevased vead.
- [x] Meetod ei loe ega kirjuta DB-d ega kutsu Gemini AI-d.
- [x] HTTP-lepingu kontroll katab PDF-i saatmise ja kohustusliku failiosa puudumise.

## Hilisem AI-teostus

PDF-i tegelik kontroll, Gemini API kutse ja ärivigade käsitlus on `TO BE IMPLEMENTED`. Need ei kuulu praeguse taski placeholder-etapi vastuvõttu.

**Kontroll:** `./gradlew test`; `AiTrainingControllerTest` katab tegelikud HTTP-rajad ja multipart-sisendid, testides ei kasutata DB-d ega AI API-t.
