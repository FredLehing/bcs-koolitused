# PDF-ist koolituse tekstiväljade loomine olemasoleva tõlke vormis

**Teenus:** `POST /api/ai-training/pdf/{trainingTranslationId}`

**Kasutav vaade:** `TrainingFormView.vue`, `/training-form`, olekud `update`

**Vaste mockupis:** [interaktiivne läbimäng](../../mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html). PDF-i ekspordi pildid ei sisalda veel uut AI-nuppu; ajakohane allikas on läbimäng ja olekute märkmed.

## Sisend

Path variable `trainingTranslationId: Integer` on vormis avatud tõlke ID.

Päring on `multipart/form-data`. Väljal `curriculum` saadetakse PDF-fail binaarkujul (mitte Base64 ega JSON). Väli on valikuline: uus valitud salvestamata fail on eelistatud; selle puudumisel peab tulevane teostus lugema salvestatud õppekava. Ka failita päring saadetakse multipart-kujul.

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

Admin vajutab valitud või salvestatud PDF-i kõrval nuppu „Täida vorm PDF + AI abiga“. Käesolevas etapis ühendatakse frontend placeholder-controlleriga, et läbi mängida faili saatmine ja tekstiväljade täitmine. Client → backend → Gemini AI → backend → Client (uus valitud fail). Faili puudumisel: Client → backend → DB → backend → Gemini AI → backend → Client. Praegune controller tagastab otse placeholder-vastuse; Gemini kutse ja DB lugemine lisatakse hiljem.

## Seotud andmebaasi tabelid

Praegune placeholder ei kasuta ühtegi tabelit. Hilisema salvestatud faili lugemise allikas on `training_translation_curriculum`, mille `training_translation_id` seostab faili vormis avatud tõlkega. Uut faili eelistades ei loeta salvestatud PDF-i. Tabelistruktuur: `docs/database/2_create.sql` (väljad `id`, `training_translation_id`, `file`, `file_name`, `file_size`, `created_at`, `updated_at`; üks fail ühe tõlke kohta).

Admin salvestab lõpliku tulemuse eraldi „Lisa“, „Lisa tõlge“ või „Salvesta“ nupuga. AI päring ei loo ega uuenda DB ridu.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| ID pole täisarv või päring ei ole multipart | 400 / 415 (Springi päringu sidumine) | Springi standardne veavastus; kohandatud äriveakoodi selles etapis pole |
| Ootamatu serveriviga | 500 Internal Server Error | Standardne vea response body vastavalt globaalsele veakäsitlusele |

Swaggeris kirjeldatud 400 (vigane PDF), 413 (liiga suur fail), 503 (AI pole saadaval) ja 404 (tõlge või õppekava puudub) on tulevase teostuse leping. Placeholder ei viska neid ärivigu ega kasuta selleks fiktiivseid veakoode. Frontend kuvab AI päringu vea vormis ja jätab tekstid ning valitud PDF-i alles.

## Vastuvõtu kriteeriumid

- [x] Meetod on `AiTrainingController` klassis ülaltoodud POST-rajal.
- [x] curriculum on valikuline; päring töötab nii uue PDF-iga kui ilma failiosata.
- [x] Vastus on 200 ja ühine kolme tekstiväljaga DTO täpsete placeholder-väärtustega.
- [x] Swaggeri kokkuvõttel on `TO BE IMPLEMENTED` eesliide, sisend, vastuse skeem ja tulevased vead.
- [x] Meetod ei loe ega kirjuta DB-d ega kutsu Gemini AI-d.
- [x] HTTP-lepingu kontroll katab valikulise PDF-i ja failita multipart-päringu.

## Hilisem AI-teostus

PDF-i tegelik kontroll, Gemini API kutse ja salvestatud õppekava lugemine ning ärivigade käsitlus on `TO BE IMPLEMENTED`. Need ei kuulu praeguse taski placeholder-etapi vastuvõttu.

**Kontroll:** `./gradlew test`; `AiTrainingControllerTest` katab tegelikud HTTP-rajad ja multipart-sisendid, testides ei kasutata DB-d ega AI API-t.
