# Leheküljestamine Spring Data JPA-s: `Page<T>` ja MapStruct mapper – Algajatele

## Sissejuhatus

Kujuta ette, et andmebaasis on 5000 koolitust. Kui frontend küsib "anna mulle koolitused", ei ole mõistlik kõiki 5000 korraga tagasi saata. See oleks aeglane ja kasutaja näeb ekraanil niikuinii ainult mõnda korraga.

Selle asemel jagatakse tulemus **lehekülgedeks** (inglise keeles *pagination*). Frontend küsib näiteks: "anna mulle **lehekülg 0**, kus on **3 koolitust**". Backend tagastab need 3 koolitust ja ütleb lisaks, **mitu lehekülge ja mitu kirjet kokku** on, et frontend saaks joonistada lehitsemise nupud.

**Päriselu analoogia — raamat:**

```
📖 Raamat = kõik koolitused andmebaasis (nt 7 tükki)
📄 Lehekülg = üks "tükk" raamatust (nt 3 koolitust)

  Lehekülg 0        Lehekülg 1        Lehekülg 2
┌────────────┐   ┌────────────┐   ┌────────────┐
│ Koolitus 1 │   │ Koolitus 4 │   │ Koolitus 7 │
│ Koolitus 2 │   │ Koolitus 5 │   │            │
│ Koolitus 3 │   │ Koolitus 6 │   │            │
└────────────┘   └────────────┘   └────────────┘

Kokku: 7 kirjet (totalElements), 3 lehekülge (totalPages)
```

> ⚠️ **Leheküljed loetakse alates 0-st**, mitte 1-st. Esimene lehekülg on `page = 0`.

Spring Data annab leheküljestamiseks kolm tööriista:

| Tööriist | Mis see on | Analoogia |
|---|---|---|
| `Pageable` | **Küsimus**: millist lehte ja mitu kirjet lehel tahan? | "Anna mulle raamatust lk 2, 3 rida lehel" |
| `PageRequest` | Klass, millega `Pageable` luuakse | Raamatukogu tellimisleht, mille täidad |
| `Page<T>` | **Vastus**: see leht + info kogu raamatu kohta | Paljundatud lehekülg + märkus "raamatus on kokku 3 lk" |

---

## 1. `Pageable` — küsimus andmebaasile

`Pageable` on objekt, mis ütleb repositoryle, **millist lehte** ja **mitu kirjet lehel** tahame. Selle loome `PageRequest.of(...)` abil:

```java
// page = mitmes lehekülg (algab 0-st), limit = mitu kirjet ühel lehel
Pageable pageable = PageRequest.of(page, limit);

// Näide: PageRequest.of(0, 3) → "esimene leht, 3 kirjet"
// Näide: PageRequest.of(1, 3) → "teine leht, 3 kirjet" (kirjed 4–6)
```

Seejärel anname `Pageable` repository meetodile **viimase parameetrina** kaasa:

```java
public interface TrainingRepository extends JpaRepository<Training, Integer> {

    @Query("""
            select t from Training t
            where (:categoryId = 0 or t.category.id = :categoryId)
            order by t.id asc""")
    Page<Training> findFilteredTrainingsBy(Integer categoryId, Pageable pageable);
    //  ↑ tagastab Page, mitte List                              ↑ Pageable on viimane
}
```

**Mis kulisside taga juhtub?** Spring teeb andmebaasi **kaks päringut**:

```
1) Andmete päring (ainult üks leht):
   SELECT ... FROM training WHERE ... ORDER BY id LIMIT 3 OFFSET 0;

2) Loenduspäring (kõik sobivad kirjed kokku):
   SELECT count(*) FROM training WHERE ...;
```

Esimene päring toob ainult selle lehe kirjed, teine päring loendab, mitu kirjet kokku filtrile vastab. Just tänu teisele päringule teab `Page`, mitu lehekülge kokku on.

> 💡 `LIMIT 3` = "maksimaalselt 3 rida", `OFFSET 3` = "jäta 3 esimest vahele". Leht 1 (3 kirjega) on seega `LIMIT 3 OFFSET 3`.

---

## 2. `Page<T>` — vastus koos lisainfoga

`Page<Training>` on nagu **karp**, milles on:
- selle lehe kirjed (`List<Training>`)
- metainfo kogu tulemuse kohta

```
Page<Training>
┌──────────────────────────────────────────┐
│ getContent()       → [Training, Training] │  ← selle lehe kirjed (List)
│ getNumber()        → 0                    │  ← mitmes lehekülg see on
│ getSize()          → 3                    │  ← küsitud lehe suurus (limit)
│ getTotalElements() → 7                    │  ← kõik filtrile vastavad kirjed kokku
│ getTotalPages()    → 3                    │  ← mitu lehekülge kokku
│ hasNext()          → true                 │  ← kas järgmine leht on olemas
└──────────────────────────────────────────┘
```

Kasutamine teenuses:

```java
Pageable pageable = PageRequest.of(page, limit);
Page<Training> filteredTrainingPage = trainingRepository.findFilteredTrainingsBy(categoryId, pageable);

List<Training> trainings = filteredTrainingPage.getContent();       // selle lehe koolitused
int currentPage = filteredTrainingPage.getNumber();                 // nt 0
int totalPages = filteredTrainingPage.getTotalPages();              // nt 3
long totalElements = filteredTrainingPage.getTotalElements();       // nt 7 (NB! tüüp on long)
```

> ⚠️ `Page<T>` on **generic** tüüp: `<T>` asemele kirjutatakse, mis tüüpi asjad lehel on. `Page<Training>` = leht Training entity'tega, `Page<String>` = leht stringidega jne. Sama idee nagu `List<Training>`.

---

## 3. Miks `Page<Training>` ei tohi otse controllerist välja minna?

Projekti reegel (vt `backend/CLAUDE.md`): **controller näeb ainult DTO-sid, mitte entity'sid.** `Page<Training>` sisaldab `Training` entity'sid, seega see tuleb enne vastuse saatmist ümber teha DTO-ks.

Lisaks: kui `Page` objekti otse JSON-iks teha, tuleb vastusesse palju üleliigset infot (`pageable`, `sort`, `first`, `last` jne) ja Spring hoiatab, et see JSON-kuju ei ole stabiilne. Seega teeme **oma vastuse DTO**, kus on ainult vajalik:

```
Andmebaas        Repository         Service                       Controller → JSON
─────────        ──────────         ───────                       ─────────────────
training   →   Page<Training>  →   mapper + metainfo   →   TrainingSummaryDto
 tabel          (entity'd)         kokku panemine             (ainult DTO-d)
```

Taski `docs/tasks/backend/Koolituste-nimekirja-paring.md` järgi peab vastus välja nägema nii:

```json
{
  "page": 0,
  "totalPages": 1,
  "totalElements": 2,
  "trainingSummaries": [
    { "trainingId": 1, "title": "Java algkursus", "categoryId": 1, "isOrderOnly": false, "isPromoted": true, ... }
  ]
}
```

Näeme, et JSON koosneb kahest osast:
1. **Metainfo** (`page`, `totalPages`, `totalElements`) → tuleb `Page` objekti meetoditest
2. **Kirjete list** (`trainingSummaries`) → tuleb `getContent()`-st, **mapperi abil DTO-deks tehtuna**

---

## 4. DTO-d vastuse jaoks

Vaja on **kahte** DTO klassi: üks ühe koolituse jaoks ja teine terve vastuse (lehe) jaoks.

```java
// Üks koolitus nimekirjas (üks element "trainingSummaries" massiivis).
// NB! Klassi nimi on siin näitena — pane nimi vastavalt oma projekti kokkuleppele.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingSummaryItemDto {
    private Integer trainingId;
    private String title;
    private String shortDescription;
    private Integer categoryId;
    private String categoryName;
    private Boolean isOrderOnly;
    private Boolean isPromoted;
    private List<FundingTypeDto> fundingTypes;
}
```

```java
// Terve vastus: metainfo + selle lehe koolitused.
// Lihtväljad (page, totalPages, totalElements) on listist ETTEPOOL — projekti reegel.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingSummaryDto {
    private Integer page;
    private Integer totalPages;
    private Long totalElements;          // Long, sest Page.getTotalElements() tagastab long
    private List<TrainingSummaryItemDto> trainingSummaries;
}
```

---

## 5. Mapper — entity'd DTO-deks

MapStruct mapper on **tõlk**: sa kirjeldad, milline entity väli läheb millisesse DTO välja, ja MapStruct genereerib ise koodi (kausta `src/main/generated/`).

**Analoogia:** mapper on nagu kolimisfirma nimekiri — "köögikapp (entity `id`) → uue korteri kööki (DTO `trainingId`)". Kui nimi on mõlemas kohas sama, ei pea midagi kirjutama, MapStruct saab ise aru.

```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrainingMapper {

    // Üks Training → üks TrainingSummaryItemDto
    @Mapping(source = "id", target = "trainingId")            // erinev nimi → peab ütlema
    @Mapping(source = "category.id", target = "categoryId")   // punkt = "mine seose sisse"
    @Mapping(source = "isOrderOnly", target = "isOrderOnly")
    @Mapping(source = "isPromoted", target = "isPromoted")
    TrainingSummaryItemDto toTrainingSummaryItemDto(Training training);

    // List<Training> → List<TrainingSummaryItemDto>
    // Selle keha pole vaja kirjutada: MapStruct kasutab iga elemendi jaoks ülemist meetodit
    List<TrainingSummaryItemDto> toTrainingSummaryItemDtos(List<Training> trainings);
}
```

Selgitused:
- `source` = väli **entity's** (kust võtame), `target` = väli **DTO-s** (kuhu paneme).
- `category.id` — `Training`-il on väli `category` (tüüpi `Category`), ja selle sees on `id`. Punkt tähendab "mine objekti sisse".
- `unmappedTargetPolicy = ReportingPolicy.IGNORE` — kui DTO-s on välju, mida mapper ei täida (nt `title`), ei anta viga, need jäävad `null`-iks.
- `componentModel = SPRING` — mapper on Springi bean, seega saab selle teenusesse `private final TrainingMapper trainingMapper;` kaudu sisse süstida.

> ⚠️ Mapper **ei tea midagi `Page`-ist**. Mapperile anname ainult `List<Training>` (ehk `page.getContent()`). Metainfo paneme teenuses ise kokku.

---

## 6. Kõik kokku teenuses

```java
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;

    public TrainingSummaryDto findFilteredTrainings(Integer categoryId, Integer limit, Integer page) {
        // 1. Küsimus: millist lehte tahame
        Pageable pageable = PageRequest.of(page, limit);

        // 2. Repository tagastab lehe entity'tega + metainfo
        Page<Training> filteredTrainingPage = trainingRepository.findFilteredTrainingsBy(categoryId, pageable);

        // 3. Mapper teeb selle lehe entity'd DTO-deks
        List<TrainingSummaryItemDto> trainingSummaryItemDtos =
                trainingMapper.toTrainingSummaryItemDtos(filteredTrainingPage.getContent());

        // 4. Paneme vastuse kokku: metainfo Page'ist + DTO list mapperist
        TrainingSummaryDto trainingSummaryDto = new TrainingSummaryDto();
        trainingSummaryDto.setPage(filteredTrainingPage.getNumber());
        trainingSummaryDto.setTotalPages(filteredTrainingPage.getTotalPages());
        trainingSummaryDto.setTotalElements(filteredTrainingPage.getTotalElements());
        trainingSummaryDto.setTrainingSummaries(trainingSummaryItemDtos);
        return trainingSummaryDto;
    }
}
```

Voog pildina:

```
PageRequest.of(0, 3)
        │
        ▼
trainingRepository.findFilteredTrainingsBy(...)
        │
        ▼
Page<Training> ─────────────┬──────────────────────────────┐
                            │                              │
               getContent() │                              │ getNumber()
                            ▼                              │ getTotalPages()
                  List<Training>                           │ getTotalElements()
                            │                              │
          trainingMapper.toTrainingSummaryItemDtos(...)    │
                            │                              │
                            ▼                              ▼
              List<TrainingSummaryItemDto>        page, totalPages, totalElements
                            │                              │
                            └──────────────┬───────────────┘
                                           ▼
                                 TrainingSummaryDto  → controller → JSON
```

### Alternatiiv: `Page.map(...)`

`Page`-il on ka meetod `map`, mis teeb **uue lehe**, kus iga element on teisendatud, aga metainfo jääb samaks:

```java
Page<TrainingSummaryItemDto> dtoPage = filteredTrainingPage.map(trainingMapper::toTrainingSummaryItemDto);
// dtoPage.getContent() on nüüd DTO-de list, getTotalPages() jne on samad nagu enne
```

`trainingMapper::toTrainingSummaryItemDto` on *meetodiviide* — lühem viis kirjutada `training -> trainingMapper.toTrainingSummaryItemDto(training)`. Mõlemad variandid on korrektsed; ülal toodud samm-sammuline variant on algajale lihtsamini loetav.

---

## 7. Aga tõlgitud väljad (`title`, `categoryName`, `fundingTypes`)?

`Training` entity's **ei ole** `title`-t ega `shortDescription`-it — need on tabelis `training_translation` (vt `backend/CLAUDE.md` jaotist "Mitmekeelsus"). Seega mapper `Training → TrainingSummaryItemDto` jätab need väljad `null`-iks.

Tavaline lahendus:
1. Too `Page<Training>` abil **selle lehe** koolituste id-d.
2. Küsi nende id-de kohta tõlked (nt `training_summary` vaatest `translationLang` järgi) eraldi päringuga.
3. Täida DTO-de puuduvad väljad teenuses `handle...` meetodis (projekti reegel: tingimuslik loogika + DTO muutmine → `handle`-prefiks).

Oluline mõte: **leheküljestamine tehakse `training` tabeli peal** (üks rida = üks koolitus), et `totalElements` loeks koolitusi, mitte tõlke- või rahastustüübi ridu.

---

## Kokkuvõte

| Mõiste | Tähendus |
|---|---|
| Pagination / leheküljestamine | Suure tulemuse jagamine väiksemateks lehtedeks |
| `Pageable` | "Küsimus": milline leht (`page`) ja mitu kirjet (`limit`) |
| `PageRequest.of(page, limit)` | Loob `Pageable` objekti; `page` algab 0-st |
| `Page<T>` | "Vastus": selle lehe kirjed + metainfo kogu tulemuse kohta |
| `getContent()` | Selle lehe kirjed `List<T>`-na — seda anname mapperile |
| `getNumber()` / `getTotalPages()` / `getTotalElements()` | Praegune leht / lehti kokku / kirjeid kokku (`long`) |
| Loenduspäring | Spring teeb automaatselt `count(*)` päringu, et teada kogusummat |
| MapStruct mapper | Tõlgib entity'd DTO-deks; ei tea `Page`-ist midagi, töötab listiga |
| `Page.map(...)` | Teeb lehe elemendid ümber, metainfo jääb alles |
| Vastuse DTO | Oma klass (`page`, `totalPages`, `totalElements`, list) — `Page`-i ei saadeta otse välja |

---

## Järgmised sammud

1. **Kirjuta `TrainingMapper`** ja proovi `page.getContent()` DTO-deks teha.
2. **Loo vastuse DTO-d** (`TrainingSummaryDto` + ühe koolituse DTO) taski JSON-i põhjal.
3. **Lisa sorteerimine**: `PageRequest.of(page, limit, Sort.by("id"))` — uuri `Sort` klassi.
4. **Proovi Swaggeris** (`/swagger-ui.html`) erinevate `page` ja `limit` väärtustega ning vaata P6Spy logist, millised `LIMIT`/`OFFSET` ja `count(*)` päringud tehakse.
5. Edasijõudnutele: uuri **N+1 probleemi** — mis juhtub, kui mapper loeb `LAZY` seoseid (nt `training.category`) iga elemendi jaoks eraldi.
