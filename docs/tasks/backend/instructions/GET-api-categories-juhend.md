# Juhend: GET /api/categories

**Taski fail:** `GET-api-categories.md`
**Kontroller:** `CategoryController.java` (uus)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab kõik kategooriad valitud keeles (`contentLang`). TrainingsView kasutab neid filtri "Koolituse kategooria" valikutena ja TrainingFormView vormi rippmenüüs. Päring käib läbi kõigi kihtide: kontroller võtab vastu query parameetri, service küsib repositooriumilt õiges keeles tõlked ja mapper teisendab need DTO-deks.

Võrreldes `GET /api/languages` endpointiga tuleb selles harjutuses juurde kolm uut asja:
- **query parameeter** (`@RequestParam`), mis antakse kontrollerist kuni repositooriumini edasi;
- **tõlketabel**: kategooria nimi ei asu `category` tabelis, vaid `category_translation` tabelis;
- **uus DTO, repositoorium ja mapper**, sest need tuleb seekord ise luua.

> **Tähelepanu:** selles projektis on persisteerimise pakett kirjutatud kujul `persistance` (mitte `persistence`).

> **Hea eeskuju:** rahastustüüpide tõlked on projektis juba sama mustriga lahendatud: `persistance/fundingtype/translation/` (repositoorium + mapper) ja `controller/common/dto/FundingTypeDto`. Kui jääd kuskil kinni, vaata sealt, kuidas asjad on üles ehitatud.

---

## Samm 1 — RestController

### Mida teha?

Kategooriate jaoks kontrollerit veel pole. Loo **uus kontroller** oma alampaketti `controller` kausta all, samamoodi nagu tegid keelte puhul.

Kontrolli esmalt, kas vastav kontrolleri klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/controller/`
- Kui **puudub**, loo uus klass IntelliJ-ga (File → New → Java Class, nimeks `alampakett.KlassiNimi`)
- Kui **on olemas**, ava see klass ja lisa sinna uus meetod

> **Kontrolli kohe:** kas klass tekkis õigesse alampaketti? Vaata faili esimest rida (`package ...`).

Uuel kontrolleril on vaja järgmisi klassiannotatsioone:

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Vaata, kuidas nimetasid keelte endpointi meetodi.
>
> **Mõtle ka:** Seekord on endpointil **üks sisend**. Vaata taskifaili jaotist "Sisend":
> - mis on parameetri **nimi** ja **tüüp**?
> - kas see tuleb URL-i **rajast** (`@PathVariable`) või **küsimärgi järel** (`@RequestParam`)? Vaata taskifaili rida "Teenus".

Seejärel lisa:
1. **Mappingannotatsioon** `@GetMapping` koos õige rajaga
2. **Parameetri annotatsioon** parameetri ette
3. **Swagger annotatsioonid** `@Operation` ja `@ApiResponses`. Mõtle, millised vastuskoodid on taskifaili "Veaolukorrad" jaotise järgi asjakohased.

### Service klassi ettevalmistus

Kontrolli, kas sobiv service klass juba eksisteerib:
- Kaust: `backend/src/main/java/ee/bcskoolitus/service/`
- Kui **puudub**, loo uus klass otse `service` kausta

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa kontrolleri klassi service muutuja:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetod välja ja **anna parameeter edasi**:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

> **Meeldetuletus:** pöördu **muutuja** poole (väike algustäht), mitte klassi poole. Muidu lisab IntelliJ sõna `static`.
>
> **IntelliJ vihje:** Vajuta punasel meetodinimel **Alt+Enter** ja vali **"Create method ... in TeenusKlass"**.
> IntelliJ loob meetodi koos sama parameetriga.

---

## Samm 2 — Service ja repository päring

### Mida teha?

Service meetodisse tuleb sisse keelekood (`contentLang`). Meetodi ülesanne on küsida andmebaasist kõigi kategooriate nimed selles keeles.

### Millisest tabelist?

Vaata taskifaili jaotist "Seotud andmebaasi tabelid". Tabeleid on kolm:

| Tabel | Mida hoiab |
|---|---|
| `category` | ainult kategooria `id` ja tehnilised väljad, **nime siin pole** |
| `category_translation` | kategooria **nimi** ühes keeles (`category_id` + `language_id` + `name`) |
| `language` | keelekood (`et`/`en`) |

> **Mõtle:** Kumb tabel annab korraga nii kategooria `id` (seose kaudu) kui ka **nime**? Selle tabeli entiteedist lähtudes tuleb päring teha.

Leia vastav entiteet `persistance` kaustast. Seejärel mõtle, **kas sellel entiteedil on juba repositoorium**. Vaata samasse kausta.

### Repository loomine (kui puudub)

Kui repositooriumi pole, loo see samasse paketti, kus on entiteet.

> **Eeskuju:** vaata, kuidas on üles ehitatud `persistance/fundingtype/translation/FundingTypeTranslationRepository.java`:
> - see on `interface`, mitte `class`,
> - see laiendab `JpaRepository<Entiteet, IdTüüp>`.

Kui repositoorium on olemas, lisa see service'isse väljana: kirjuta meetodi sisse **väikese tähega** muutuja nime algus ja vajuta **Tab**.

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {

    private final EntiteetRepository entiteetRepository;

    public void meetodiNimi(SisendTüüp parameetriNimi) {
        entiteetRepository
    }
}
```

**Küsi endalt:** Kas JPA baasmeetod (`findAll()`) oskab filtreerida **keele järgi**? Kas see järjestab tulemuse?

> **Rusikareegel:** Kui päringusse läheb sisendina muu väärtus kui tabeli `id`
> (siin keelekood), on vaja **uut meetodit** (vt samm 5).

Kui repository meetod tagastab midagi, **pane tulemus kohe muutujasse**.

---

## Samm 3 — DTO klass

### Mida teha?

Vastus peab olema `CategoryDto` objektide list. Seda DTO-d **veel pole**, see tuleb luua.

Kus see asub? Taskifaili "Väljund" jaotis ütleb: kausta `controller/common/dto/`, kõrvuti `FundingTypeDto`-ga.

> **Mõtle:** Miks `common/dto`, mitte `controller/category/dto`? Vaata taskifaili jaotist "Avatud küsimused", punkti 3.

### DTO loomine

Selle DTO väljad tulevad **kahest eri kohast**: üks väli seotud entiteedi kaudu, teine tõlketabelist. Seepärast on siin lihtsam luua DTO **käsitsi**:

1. Ava võrdluseks `controller/common/dto/FundingTypeDto.java`
2. Loo samasse kausta uus klass
3. Lisa samad klassiannotatsioonid, mis on `FundingTypeDto`-l
4. Lisa väljad taskifaili JSON näidise järgi: vaata väljade **nimesid** ja mõtle, mis **tüüpi** need on (number või tekst)

> Alternatiiv: JPA Buddy (paremklõps entiteedil → New → DTO) sobib ka, aga siis kontrolli väljade nimed üle, sest need peavad klappima taskifaili JSON-iga.

---

## Samm 4 — Mapper

### Mida teha?

Repositooriumist tulevad **tõlketabeli entiteedid**. Need tuleb teisendada DTO-deks. Kategooria mapperit veel pole, see tuleb luua.

### Mapper interface'i loomine

> **Eeskuju:** `persistance/fundingtype/translation/FundingTypeTranslationMapper.java` teeb **täpselt sama asja** rahastustüüpidega. Ava see ja vaata:
> - kuidas interface on annoteeritud (`@Mapper(...)`),
> - millised meetodid seal on ja mis tüüpi need on.

Loo uus mapper interface samasse paketti, kus on tõlketabeli entiteet ja repositoorium.

Meetodid konventsiooni järgi:

```java
// Ühele DTO-le
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);

// Listi DTO listiks
List<TagastatavDtoTüüp> toDtoKlassiNimid(List<EntiteetTüüp> entiteedid);
```

> **Meeldetuletus:** `@Mapping` annotatsioonid käivad **ainult üksiku objekti** meetodile (ainsuses). Listi meetod jääb annotatsioonideta.

Lisa üksiku objekti meetodile `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele ja vajuta **Ctrl+Space**.
> IntelliJ näitab DTO välju, nii tead, mitu `@Mapping` rida on vaja.

Täida iga target-väli:
- üks DTO väli tuleb **seotud entiteedi kaudu**. Vaata, kuidas `FundingTypeTranslationMapper` kirjutab `source` väärtuse punktiga (`seotudObjekt.väli`);
- teine tuleb otse tõlketabeli entiteedi väljast.

**Iga DTO target-väli peab olema kaardistatud** kas `source`-iga või `ignore = true`-ga.

### Service meetodi lõpetamine

Lisa mapper service'isse väljana (väike täht + **Tab**), kutsu listi meetod välja ja tagasta tulemus:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    List<EntiteetTüüp> entiteedid = entiteetRepository.meetodiNimi(parameetriNimi);
    return mapperMuutuja.toDtoKlassiNimid(entiteedid);
}
```

> **IntelliJ vihje:** Vajuta `return` peal **Alt+Enter** → **"Change method return type"**.

---

## Samm 5 — Repository (keele järgi filtreeritud ja järjestatud päring)

### Mida teha?

Taskifaili järgi:
- tagastatakse **kõik** kategooriad,
- nimi on `contentLang` **keeles**,
- tulemus on sorteeritud **kategooria `id`** järgi **kasvavalt**.

> **Mõtle enne JPA Buddy avamist:**
> 1. Tõlketabeli entiteedil pole välja "keelekood". Keelekood asub **seotud** entiteedis. Kuidas jõuad tõlketabeli entiteedist keelekoodini? (Vihje: punkt seose ja välja vahel.)
> 2. Sorteerida tuleb **kategooria** `id` järgi, mitte tõlkerea enda `id` järgi. Kuidas sellele ligi pääseb?
> 3. Vaata `FundingTypeTranslationRepository` päringut, seal on sarnased asjad juba tehtud.

### Uue meetodi loomine JPA Buddy abil

Mine repository interface'i faili, pane kursor `{ }` vahele ja vajuta **Alt+Insert** → **Repository Method...**

1. Vali **Query** variant (mitte tuletatud *Method*)
2. Vali **Find collection**
3. **Wrap type:** `List`
4. **Query condition:** lisa tingimus keelekoodi kohta. Parameetri nimi peaks olema sama, mis kontrolleris (`contentLang`).
5. **Order By:** kategooria `id`, õige suunaga

> **Kui JPA Buddy ei lase seotud entiteedi välju valida:** loo meetod lihtsama tingimusega ja paranda `@Query` tekst pärast käsitsi, võttes eeskujuks `FundingTypeTranslationRepository`.

Peale meetodi loomist:
- **Meetodi nimi:** JPA Buddy pakub pika nime. Pane lühike nimi, mis ütleb, **mida** meetod tagastab (vt `backend/CLAUDE.md` jaotist "Repositooriumi meetodi nimetamine" ja näidet `findTrainingFundingTypeTranslationsBy`).
- **Parameetri nimi** meetodis ja `@Query` sees (`:nimi`) peavad klappima.
- Eemalda ebavajalikud `@Param()` annotatsioonid.

Kasuta uut meetodit service'is ja anna talle `contentLang` edasi.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod tagastab nüüd DTO-de listi. Mine tagasi kontrolleri meetodisse ja lisa `return`:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` ja vajuta **Alt+Enter** → "Change return type".

Tulemus:

```java
public TagastatavTüüp meetodiNimi(SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata üle, kas koodi saab puhtamaks muuta.

**Extract Method IntelliJ-ga:**

Märgi service meetodis koodilõik, mille tahad eraldada helper meetodiks, ja tee paremklõps → Refactor → Extract Method.

> **Tähelepanu:** Ekstraktimisel annab IntelliJ helper meetodile parameetrina kogu objekti.
> Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja, ja paranda vajadusel.

Enne:
```java
kontrolliMidagiHelper(dtoObjekt);

private void kontrolliMidagiHelper(DtoTüüp dto) {
    boolean onProbleem = repositoorium.kontrollimeetod(dto.getMingiVäli());
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

Pärast (parem, sest edasi antakse ainult vajalik):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    boolean onProbleem = repositoorium.kontrollimeetod(väljaNimi);
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

> Selle taski service meetod on lühike. Ekstraktimist ei pruugi vaja olla, otsusta ise.

### Meetodite järjekord

Kontrolli meetodite järjekorda Java konventsiooni järgi:
1. `public` meetodid eespool
2. `private` meetodid tagapool
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] Uus RestController on õiges alampaketis ja sellel on annotatsioonid `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`
- [ ] Kontrolleri meetodil on `@GetMapping`, `@RequestParam` parameeter, `@Operation` ja `@ApiResponses`
- [ ] Uus service klass on olemas ja sellel on annotatsioonid `@Service`, `@RequiredArgsConstructor`
- [ ] `CategoryDto` asub kaustas `controller/common/dto/` ja selle väljad klapivad taskifaili JSON-iga
- [ ] Repository interface laiendab `JpaRepository`-t ja uuel meetodil on `@Query` annotatsioon Named parameters stiilis
- [ ] Päring filtreerib keelekoodi järgi ja järjestab kategooria `id` järgi kasvavalt
- [ ] Repository meetodi nimi ütleb, mida see tagastab
- [ ] Mapper interface on olemas ja sellel on `@Mapper(... componentModel = SPRING)` annotatsioon
- [ ] Kõik `@Mapping` annotatsioonid on täidetud ja ükski väli ei ole kaardistamata
- [ ] Meetodite järjekord: `public` enne, `private` pärast, lisaks väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui.html`):
> - `contentLang=et` → Programmeerimine, Disain, Juhtimine
> - `contentLang=en` → Programming, Design, Management
> - `contentLang=xx` → tühi list `[]`
