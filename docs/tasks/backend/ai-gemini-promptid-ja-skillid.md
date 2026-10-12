# Gemini juhendamine: system prompt, JSON skeem ja skillid

**Kuupäev:** 2026-10-08
**Seotud:** [`ai-training-pdf-teostusplaan.md`](ai-training-pdf-teostusplaan.md), mock `docs/mock-wireframe/loo-mock-vaade/form-view/training-form-view-labimang.html` (skripti jaotis „Päris Gemini AI“)

Küsimus: kas projekti jaoks on vaja system prompte või skille, et juhendada Google Geminit, mida PDF-ist väljundina võtta?

## Skillid ≠ Gemini juhendamine

Need on kaks eri asja.

- **Skillid** (`.claude/skills/`, nt `skill-loo-backend-task`) on juhised **Claude Code'ile**, mis aitab koodi kirjutada ja taske luua. Need on arendustööriistad — rakenduses jooksev Gemini neid ei näe ega kasuta.
- **Gemini juhendamine** käib päringu enda sees. Mockis on see juba olemas:

| Osa | Mida teeb | Kus mockis |
|---|---|---|
| **System prompt** (`systemInstruction`) | Ütleb, mis rolli AI täidab ja millised on reeglid: PDF-i keel, ära mõtle juurde, lubatud HTML, sektsioonide järjekord, pikkused | `AI_PDF_PROMPT`, `AI_TRANSLATION_PROMPT` |
| **JSON skeem** (`responseSchema`) | Sunnib vastuse kindlale kujule: täpselt need väljad ja tüübid. Vaba teksti ega „Siin on teie JSON:“ eessõna ei tule | `AI_PDF_SCHEMA`, `AI_TRANSLATION_SCHEMA` |
| **Parameetrid** | `temperature 0.2`: täpne, mitte loominguline | `generateJson` |
| **Järeltöötlus** | Turvavõrk juhuks, kui AI juhist ei järgi: kärpimine, HTML puhastus | `toAiTrainingContent`, `sanitizeDescription` |

System prompt juhendab juba praegu, mida PDF-ist võtta. Küsimus on pigem selles, **kas seda tasub veel parandada**.

## Mida tasuks lisada

### 1. Näidiskirjeldus promptis (few-shot)

Praegu kirjeldab prompt struktuuri sõnadega. AI järgib stiili palju paremini, kui näeb ühte head näidet. Projektis on selleks valmis kandidaat: `docs/JSON/training-description-sample.html` (Noorem AI-arendaja programm). See on päris kirjeldus, õige toon ja ainult lubatud märgendid.

Prompti lisanduks näiteks:

> Here is an example of a good description. Match its tone and structure, but use only facts from the PDF.

Selle tulemusel on AI kirjeldused ühtlasema stiiliga ja sarnasemad olemasolevate koolituste omadele. Hind on umbes 1000 tokenit päringu kohta, mis on tühine.

### 2. Backendis prompt eraldi failina, mitte Java stringina

```
backend/src/main/resources/prompts/
  training-pdf-system.st
  training-translation-system.st
```

Spring AI loeb selle `Resource`-ina (`.system(promptResource)`). Plussid:

- prompti saab muuta ilma Java koodi lugemata;
- muudatused on gitis hästi näha;
- `PROMPT_VERSION` kõrval on selge, mis versioon kasutusel on.

### 3. Promptiga katsetamine käib mockis

Muuda mockis `AI_PDF_PROMPT`, värskenda lehte ja proovi 2–3 päris PDF-iga. Kui tulemus on hea, kopeeri prompt backendi faili. See ongi teostusplaani 1. päeva „prompti häälestamine“.

## Kas eraldi skilli oleks vaja?

Gemini jaoks mitte. Claude Code'i jaoks võiks teha skilli, mis aitab prompte hinnata (nt „anna PDF ja AI väljund, kontrolli, kas midagi on välja mõeldud“). See projekt on aga selleks liiga väike, käsitsi kontroll 2–3 PDF-iga on piisav.

## Järgmine samm

Lisada mocki `AI_PDF_PROMPT`-i näidiskirjeldus (`training-description-sample.html` põhjal) ja võrrelda, kas tulemus läheb paremaks.
