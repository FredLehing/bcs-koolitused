# Koolituste päringud — tööde järjekord

`AdminEnquiriesView.vue`, `AdminEnquiryView.vue`. Katusharu `feature/RAIN-enquiries`.

Allikad: skeemid `admin-enquiries-view-skeemid.md`, märkmed `docs/mock-wireframe/markmed/admin-enquiries-view-markmed.md` ja `admin-enquiry-view-markmed.md`, läbimäng `admin-enquiries-view-labimang.html`.

| # | Töö | Taskifail | Märkus |
|---|---|---|---|
| 0.1 | View `admin_enquiry_summary`, seed, `EnquiryStatus`, `Enquiry` seosed | `docs/tasks/backend/enquiry-db-changes.md` | kõik järgmised sõltuvad sellest |
| 1.1 | `GET /api/admin-enquiries` | `docs/tasks/backend/GET-api-admin-enquiries.md` | |
| 1.2 | `GET /api/admin-enquiry/{enquiryId}` | `docs/tasks/backend/GET-api-admin-enquiry-enquiryId.md` | |
| 1.3 | `PUT /api/enquiry/{enquiryId}/handle` ja `/reopen` | `docs/tasks/backend/PUT-api-enquiry-enquiryId-handle.md` | |
| 2.1 | `AdminEnquiriesView.vue` + `AdminEnquiryView.vue` + navbar + router | `docs/tasks/frontend/admin-enquiries-view.md` | |

## Hiljem

- Päringu loomise vorm avalikul koolituse lehel (`POST /api/enquiry`).
- Admini märkmed, e-kirja teavitus, kustutamine.
