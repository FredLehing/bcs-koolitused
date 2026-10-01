# Koolituse detailvaade — skeemid

[Mock](training-view-labimang.html), [frontend task](../../../tasks/frontend/training-view.md),
[backend task](../../../tasks/backend/GET-api-training-summary-trainingId.md),
[märkmed](../../markmed/training-view-markmed.md).

## Paigutus

```mermaid
flowchart TB
    Back[Tagasi: returnTo või /trainings] --> Columns
    subgraph Columns[Kaks veergu; mobiilis üks]
        subgraph Left[Vasak 8/12]
            Info[Koolituse info: pealkiri, admini pliiats, lühikirjeldus, kirjeldus]
            Info --> PDF[Õppekava PDF: ainult valitud tõlke faili olemasolul]
        end
        subgraph Right[Parem 4/12]
            Data[Koolituse andmed: õppekeel, kategooria, rahastus, asukoht, tellitavus]
            Data --> Lecturers[Koolitajad: peidus kui pole]
            Lecturers --> Next[Järgmised koolitused: kuupäevad, viis, Täis märgis või tühi teade]
            Next --> Calendar[Üldkalendri link]
        end
    end
```

## Andmed, tõlke valik ja PDF

```mermaid
flowchart TD
    Open[trainingId ja valikuline trainingTranslationId] --> API[GET /api/training-summary/id koos contentLang-ga]
    API --> Active{Koolitus olemas ja mitte D?}
    Active -->|Ei| Error[404: kuva veateade]
    Active -->|Jah| Requested{Antud tõlge kuulub koolitusele?}
    Requested -->|Jah| Selected[Valitud tõlge]
    Requested -->|Ei või puudub| Language{contentLang tõlge olemas?}
    Language -->|Jah| Selected
    Language -->|Ei| Main[Põhikeele varutekst ja märkus; tõlketa 404]
    Selected --> File{Selle tõlke PDF olemas?}
    File -->|Jah| Show[Nimi ja suurus; PDF kaart]
    File -->|Ei| Hide[PDF kaart peidetud]
    Main --> Hide
    Show --> Download[Klikk: GET /api/training-translation/id/curriculum]
    Download --> Attachment[Fail alla; serveri Content-Disposition attachment]
    API --> Lecturers[Seotud aktiivsed koolitajad sort_order järjekorras]
    Lecturers --> Bulk[Tõlked ja foto versioonid hulgi; lecturers koondvastuses]
    Bulk --> Card[LecturerCard: ainult kuvamine, JSON-päringuid pole]
    Card --> Photo{photoVersion olemas?}
    Photo -->|Jah| Image[GET /api/lecturer/id/photo koos v-ga]
    Photo -->|Ei| Placeholder[Pildi kohatäide]
    API --> Courses[public_course_summary: koolitus P, toimumiskord O/F, algus vähemalt täna]
    Courses --> List[Alguse ja ID järjekorras; tühi nimekiri annab teate]
```

## Navigeerimine

```mermaid
sequenceDiagram
    participant Origin as Nimekiri või koolituse vorm
    participant Training as /training
    participant Lecturer as /lecturer
    participant Course as /course
    participant Calendar as /courses
    Origin->>Training: trainingId, vajadusel trainingTranslationId, returnTo=origin.fullPath
    Training->>Lecturer: lecturerId ja returnTo=training.fullPath
    Lecturer->>Training: Tagasi: taasta kogu lähte-URL
    Training->>Origin: Tagasi: taasta algne lähte-URL
    Training->>Course: Järgmised koolitused: ainult courseId, returnTo puudub
    Course->>Calendar: Tagasi: varusiht /courses
    Note over Training: Keelevahetus eemaldab tõlke ID, säilitab returnTo
```

Muutmise ikoon asub pealkirja järel ja avaneb ainult adminile.
Koolituse õppekeel ei sõltu teksti kuvamiskeelest. Tõlke puudumisel ei pakuta
teise keele PDF-i. Vormi salvestamata fail ei kuulu avaliku eelvaate sisusse.

Tagasitee [ühine leping ja piirid](../return-to-navigation-skeemid.md).
