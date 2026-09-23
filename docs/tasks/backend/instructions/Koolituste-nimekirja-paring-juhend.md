# Juhend: GET /api/trainings

**Taski fail:** `Koolituste-nimekirja-paring.md`
**Kontroller:** `TrainingController.java` (uus, alampaketti `controller/training/`)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab avaliku koolituste nimekirja lehekülgede kaupa. Kõik query parameetrid on valikulised: nendega saab filtreerida kategooria, rahastustüübi ja õppekeele järgi ning valida tõlgitud väljade keele. Päring läbib kõik kolm kihti. Selle harjutuse käigus õpid kolme uut asja: kuidas teha **valikulisi filtreid** JPQL päringus, kuidas kasutada Spring Data **leheküljestamist** (`Pageable` / `Page`) ja kuidas koguda **mitmekeelseid andmeid** (`*_translation` tabelid) ühte vastuse DTO-sse.

> **Tähelepanu, eelsamm:** Projektis on praegu olemas ainult `Language`, `Role` ja `User` entiteedid. Selle taski jaoks on vaja veel järgmisi entiteete: `training`, `training_translation`, `category`, `category_translation`, `training_funding_type`, `funding_type` ja `funding_type_translation`. Genereeri need **JPA Buddy** abil andmebaasist (paremklõps `persistance` paketil → New → JPA Entities from DB). Pane iga entiteet oma alampaketti, samamoodi nagu olemasolev `persistance/language/`. Pärast genereerimist kontrolli, et `@Table` annotatsioonis on õige skeem (`bcs_koolitused`).

---

## Samm 1 — RestController

### Mida teha?

Koolituste jaoks eraldi kontrollerit veel ei ole. Olemas on ainult `LoginController`. Loo uus kontrolleriklass, mis vastab teele `/api/trainings`.

Kontrolli esmalt, kas vastav kontrolleriklass on juba olemas:
- Kaust: `backend/src/main/java/ee/bcskoolitus/controller/`
- Kui **puudub**, loo uus klass IntelliJ-ga (File → New → Java Class).
- Kui **on olemas**, ava see klass ja lisa sinna uus meetod.

Uue kontrolleri klassiannotatsioonid:

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**. Nii saad kõigepealt loogika paika:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // esialgu tühi meetod
}
```

> **Mõtle:** Mis oleks selle meetodi hea nimi? Nimi peaks ütlema, mida meetod tagastab.
> Vihje annavad HTTP meetod ja API tee taskifailist.

Seejärel lisa:
1. **Mappingannotatsioon**: `@GetMapping`
2. **Parameetrite annotatsioonid**: kõik kuus parameetrit on **query parameetrid** ja **valikulised**. Mõtle, millist `@RequestParam` atribuuti on vaja, et parameeter ei oleks kohustuslik.
3. **Swagger annotatsioonid**: `@Operation` ja `@ApiResponses`. Vaata taskifaili "Veaolukorrad" sektsiooni: seal on 200 ja 400 (vigane numbriline parameeter).

```java
@GetMapping("/mingi/rada")
@Operation(summary = "Lühikokkuvõte")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Kirjeldus, mida viga sisaldab",
                content = @Content(schema = @Schema(implementation = VeaKlass.class)))})
public TagastatavTüüp meetodiNimi(@RequestParam(...) SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

> **Mõtle:** Mis juhtub `limit` ja `page` parameetriga, kui klient neid ei saada? Kas `null` on siin mõistlik, või võiks neil olla vaikeväärtus?

### Service klassi ettevalmistus

Kontrolli, kas service klass on juba olemas:
- Kaust: `backend/src/main/java/ee/bcskoolitus/service/`
- Kui **puudub**, loo uus klass.

```java
@Service
@RequiredArgsConstructor
public class TeenusKlass {
    // ...
}
```

Lisa service muutuja kontrolleri klassi:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu kontrollerist service meetod välja. Esialgu jääb see tühjaks väljakutseks:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi(...)` on punasega alla joonitud,
> vii kursor punasele joonele ja vajuta **Alt+Enter** → **"Create method in TeenusKlass"**.

---

## Samm 2 — Service ja esimene repository päring

### Mida teha?

Nüüd liigud service meetodisse. Sisse tulevad kuus valikulist parameetrit. Neist neli (`categoryId`, `fundingTypeId`, `trainingLang` ja `translationLang`) mõjutavad seda, **milliseid ridu** ja **mis keeles** pärida. Ülejäänud kaks (`limit` ja `page`) mõjutavad seda, **mitu rida ja millise lehe** saad tagasi.

### Leheküljestamine

Spring Data oskab leheküljestamist ise teha. Mõtle järgmistele küsimustele:
- Millise objektiga saad `page` ja `limit` väärtused repositooriumile edasi anda? Otsi `PageRequest` klassi.
- Mida tagastab repository meetod siis, kui ta saab parameetriks `Pageable`? Vaata `Page<...>` tüüpi ja selle meetodeid (`getTotalPages()`, `getTotalElements()`, `getContent()`).

> Taskifailis on nõue, et `totalPages` ja `totalElements` peavad kajastama **kogu filtreeritud tulemushulka**, mitte ainult tagastatud lehte. Mõtle, kas `Page` annab selle info sulle juba valmis kujul kätte.

### Repository ühenduse loomine

Mõtle: **millisest tabelist** on vaja põhiandmed pärida? Vaata taskifaili sektsiooni "Seotud andmebaasi tabelid".

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta muutuja nime algus ja IntelliJ pakub vastavat repositooriumi.
> Vajuta **Tab** ja repositoorium lisatakse klassiväljana.

> **Kui repositooriumi interface'i pole olemas:** Alt+Enter → loo uus interface, mis laiendab `JpaRepository`-t, ja kontrolli, et see läheks entiteediga samasse paketti.

**Küsi endalt:** Kas `findAll()` on siin piisav? Arvesta, et filtrid on valikulised ja kõik neli võivad olla `null`.

> **Rusikareegel:** Kui päringu sisend on midagi muud kui tabeli `id`, on tõenäoliselt vaja **uut meetodit** (vt Samm 5).

Kui repository meetod midagi tagastab, **pane tulemus kohe muutujasse** ("Meetodi palve").

---

## Samm 3 — DTO klassid (loo need kohe, mitte alles lõpus)

### Mida teha?

Selle taski vastus on **kolmetasandiline**:

1. Välimine DTO (`TrainingSummaryDto`) sisaldab leheküljestamise metaandmeid ja koolituste listi.
2. Listi element sisaldab ühe koolituse andmeid.
3. Iga koolituse sees on rahastustüüpide list (`fundingTypes`).

Selle kuju leiad taskifaili "Väljund" sektsiooni JSON näidisest.

**Miks luua DTO-d kohe?** Andmed tulevad mitmest allikast: `training`, tõlketabelid, kategooria ja rahastustüübid. Ühte DTO objekti on lihtsam samm-sammult täita kui koguda palju eraldi muutujaid ja need alles lõpus kokku panna.

Kontrolli, kas DTO-d on juba olemas:
- Kaust: `backend/src/main/java/ee/bcskoolitus/controller/training/dto/`

> **Mõtle:** Kas mõni neist DTO-dest võiks tulevikus minna ka teiste kontrollerite kasutusse? Näiteks rahastustüübi `id` + `name` paar. Kui jah, siis kuulub see backend/CLAUDE.md järgi paketti `controller/common/dto/`.

> **Väljade järjekord:** Välimises DTO-s tulevad lihtväljad (`page`, `totalPages`, `totalElements`) **enne** listi-välja. Nii nõuab backend/CLAUDE.md.

> **Mitme allikaga DTO:** Kuna väljad tulevad mitmest entiteedist, ei piisa JPA Buddy ühe entiteedi generaatorist. Loo DTO-de struktuur taskifaili JSON näidise järgi. JPA Buddy abil (paremklõps entiteedil → New → DTO, **Flat** struktuur) võid teha alustuseks põhja ja selle siis käsitsi korda teha.

---

## Samm 4 — Mapper ja ülejäänud andmete kogumine

### Mida teha?

Nüüd, kui koolituste lehekülg on käes, tuleb koolituse väljad kaardistada koolituse DTO-sse. Osa välju ei ole `training` entiteedis endas: tõlgitud pealkiri ja lühikirjeldus, kategooria nimi ja rahastustüübid. Neid tuleb eraldi päringutega juurde koguda.

### Mapper

Mapper kuulub `persistance/<entiteet>/` paketti, samamoodi nagu olemasolev `LanguageMapper`.

Nimeta meetodid konventsiooni järgi:

```java
// Ühele DTO-le
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);

// Listist DTO listiks
List<TagastatavDtoTüüp> toDtoKlassiNimid(List<EntiteetTüüp> entiteedid);
```

> `@Mapping` annotatsioonid käivad alati **üksiku objekti** meetodile. List-meetod jääb annotatsioonideta, sest MapStruct genereerib selle ise.

Lisa `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele ja vajuta **Ctrl+Space**.
> IntelliJ näitab kõiki DTO välju. Nii tead, mitu `@Mapping` rida vaja on.

Täida kõik väljad. Seda, mis ei tule otse entiteedist, märgi `ignore = true`-ga:

```java
@Mapping(source = "seotudObjekt.id", target = "seotudObjektiId")
@Mapping(source = "tavaveerg", target = "samaNimiDtos")
@Mapping(ignore = true, target = "väljaMidaHiljemTäidetakse")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **Mõtle:** Millised koolituse DTO väljad saad otse `training` entiteedist (ka seoste kaudu)? Millised sõltuvad `translationLang`-ist ja vajavad eraldi päringut?

> **Väljad, mida entiteet ei kata** (`ignore = true`), täida service meetodis pärast mappimist. Iga täiendava päringu puhul kehtib sama muster: kas on olemas valmis meetod, kas tulemus võib puududa (`Optional`) ja **pane tulemus kohe muutujasse**.

> **Nõue taskifailist:** Kui koolitusel pole ühtegi rahastustüüpi, peab `fundingTypes` olema **tühi list**, mitte `null`.

### Service meetodi lõpetamine

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    Page<EntiteetTüüp> entiteediLeht = entiteetRepository.meetodiNimi(..., pageable);
    List<ElemendiDtoTüüp> elemendiDtod = mapperMuutuja.toDtoKlassiNimid(entiteediLeht.getContent());
    // täida iga elemendi puuduolevad väljad (tõlked, rahastustüübid)
    // loo välimine DTO ja pane sinna leheküljestamise metaandmed + list
    return välimineDto;
}
```

> **IntelliJ vihje:** Meetodi tagastustüüp on praegu `void`, aga sees on `return` lause.
> Vajuta punase joone peal **Alt+Enter** ja IntelliJ parandab tagastustüübi.

---

## Samm 5 — Repository (täiendavad päringud)

### Mida teha?

Selles taskis on vaja vähemalt kahte kohandatud päringut:
1. **Filtreeritud ja leheküljestatud koolituste päring.** Kõik neli filtrit on valikulised.
2. **Tõlgete ja rahastustüüpide päringud** konkreetse keele jaoks.

### Valikulised filtrid JPQL-is

Siin näen õpilasi kõige sagedamini komistamas. Mõtle järgmistele küsimustele:
- Kuidas kirjutada JPQL tingimus, mis kehtib **ainult siis**, kui parameeter ei ole `null`, ja muidu ei piira midagi?
- `trainingLang` tuleb sisse **koodina** (`et`/`en`), aga `training` tabelis on `training_language_id`. Kuidas jõuad seose kaudu `language.code` väljani?
- `fundingTypeId` filter käib liitetabeli `training_funding_type` kaudu. Kuidas kontrollida, et koolitusel **on olemas** vastav rahastustüüp, ilma et koolitused tulemuses dubleeruksid?

### Uue meetodi loomine JPA Buddy abil

1. Paremklõps repository klassis → JPA Buddy
2. Vali **Query**
3. Vali meetodi tüüp: **Find instance** / **Find collection** / **Count** / **Exists**
4. **Wrap type**: üksiku rea puhul kaalu `Optional<...>`, mitme rea puhul `List<...>`. Leheküljestatud päringu puhul on see `Page<...>`, ja `Pageable` lisad meetodile viimaseks parameetriks.
5. Lisa **query conditionid**
6. **Advanced** sektsioonis vali alati **Named parameters**
7. Mõtle läbi **Order By**. Leheküljestamisel peab järjekord olema stabiilne, muidu võivad read lehtede vahel "hüpata".

Kui meetod on loodud:
- Anna parameetritele konkreetsed nimed ja tee sama muudatus ka `@Query` annotatsioonis.
- Nimeta meetod nii, et nimest oleks näha, mida ta tagastab, näiteks `findFiltered<Entiteedid>By(...)`, mitte `findFilteredBy(...)`.

### Optional käsitlemine

Kui tõlke päring tagastab `Optional<...>`, otsusta teadlikult, mida teha, kui tõlget valitud keeles pole:
- **`orElseThrow(...)`**, kui tõlge on kohustuslik
- **`orElse(...)`** või muu `Optional` API, kui puudumine on lubatud

---

## Samm 6 — Tagasi RestController'isse

### Mida teha?

Service meetod on nüüd valmis. Täienda kontrolleri meetodit nii, et see tagastaks service'i tulemuse:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    teenuseMuutuja.meetodiNimi(parameetriNimi);  // <- praegu jääb tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` ja vajuta **Alt+Enter** → "Change return type".

```java
public TagastatavTüüp meetodiNimi(SisendTüüp parameetriNimi) {
    return teenuseMuutuja.meetodiNimi(parameetriNimi);
}
```

---

## Samm 7 — Kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata, kas seda saab puhtamaks teha. Selles taskis on service meetod tõenäoliselt pikk: seal on leheküljestamine, mappimine ning tõlgete ja rahastustüüpide täitmine. Ekstrakti need helper meetoditeks. Kui helper muudab DTO-d, kasuta `handle`-prefiksit (vt backend/CLAUDE.md).

**Extract Method IntelliJ-ga:** märgi koodilõik → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ annab ekstraktimisel parameetriks tihti terve objekti.
> Vaata üle, kas helper vajab kogu objekti või ainult üht välja.

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

Pärast (parem, sest antakse edasi ainult vajalik):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    boolean onProbleem = repositoorium.kontrollimeetod(väljaNimi);
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

### Meetodite järjekord

1. `public` meetodid kõigepealt
2. `private` meetodid nende järel
3. Järjesta meetodid ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all.

---

## Kokkuvõte ja kontrollnimekiri

- [ ] Vajalikud entiteedid on genereeritud õigesse paketti ja õige skeemiga (`bcs_koolitused`)
- [ ] RestController klassil on `@RestController`, `@RequestMapping` ja `@RequiredArgsConstructor`
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses`
- [ ] Kõik kuus query parameetrit on valikulised
- [ ] Service klassil on `@Service` ja `@RequiredArgsConstructor`
- [ ] Repository interface'id laiendavad `JpaRepository`-t
- [ ] Repository meetoditel on `@Query` annotatsioon Named parameters stiilis
- [ ] Mapperil on `@Mapper(componentModel = "spring")`
- [ ] Kõik `@Mapping` annotatsioonid on täidetud: igal target-väljal on kas `source` või `ignore = true`
- [ ] `page`, `totalPages` ja `totalElements` kajastavad kogu filtreeritud hulka
- [ ] Koolitusel ilma rahastustüüpideta on `fundingTypes` tühi list
- [ ] Meetodite järjekord: `public` enne `private`-t, järjestatud väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`).
> Proovi läbi ilma parameetriteta päring, `categoryId=1`, `fundingTypeId=1`, `limit=1&page=1` ning olematu `categoryId`, mis peab andma tühja listi.
