# Vue komponendi struktuur (Options API)

See fail kirjeldab meie projektis kasutatavat Vue 3 komponendi ülesehitust Options API-ga.

---

## Üldine järjekord `<script>` sees

```js
export default {
  // 1. Komponendi nimi
  name: 'KomponendNimi',

  // 2. Alamkomponendid
  components: { Komponent1, Komponent2 },

  // 3. Props — sisendandmed vanemkomponendist
  props: {
    propNimi: {
      type: String,
      default: '',
    },
  },

  // 4. Emits — sündmused, mida komponent saadab välja
  emits: ['event-midagi-juhtus'],

  // 5. Data — komponendi reaktiivne sisemine olek
  data() {
    return {
      muutuja: '',
    }
  },

  // 6. Computed — arvutatud väärtused (sõltuvad data või props väljadest)
  computed: {
    arvutatudVäärtus() {
      return this.muutuja.toUpperCase()
    },
  },

  // 7. Methods — funktsioonid ja sündmuste käsitlejad
  methods: {
    teeMiddagi() {
      this.$emit('event-midagi-juhtus', this.muutuja)
    },
  },

  // 8. Lifecycle hook — beforeMount käivitub enne HTML-i renderdamist
  beforeMount() {
    this.laeAndmed()
  },
}
```

---

## Props kirjutamise reeglid

Lihtsatel propidel piisab tüübist:

```js
props: {
  entityName: String,
  selectedParentId: Number,
  isOpen: Boolean,
}
```

Kui on vaja vaikeväärtust või kohustuslikku välja:

```js
props: {
  isOpen: {
    type: Boolean,
    default: false,
  },
  firstOptionLabel: {
    type: String,
    default: '-- Kõik --',
  },
}
```

---

## Emits ja sündmuste nimetamine

Kõik väljalähtuvad sündmused kirjutatakse `emits` massiivi.  
Sündmuse nimi algab alati **`event-`** eesliitega:

```js
emits: ['event-modal-closed', 'event-entity-deleted', 'event-new-parent-selected']
```

---

## Data — algväärtuste struktuur

`data()` tagastab alati objekti. Keerukamad andmed (API vastused) kirjutatakse välja koos tühja struktuuriga, et Vue saaks reaktiivsuse seadistada:

```js
data() {
  return {
    successMessage: '',
    errorMessage: '',
    selectedParentId: 0,

    entity: {
      parentId: 0,
      entityName: '',
      quantity: 1,
      imageData: '',
      relatedTypes: [
        {
          relatedTypeId: 0,
          relatedTypeName: '',
          isAvailable: false,
        },
      ],
    },

    errorResponse: {
      message: '',
      errorCode: 0,
    },
  }
},
```

---

## Methods — API päringute muster

API päringud käivad `.then()` / `.catch()` / `.finally()` ahelana.  
Iga päringu vastus suunatakse eraldi `handle`-meetodisse:

```js
methods: {
  getEntities() {
    EntityService.sendGetEntitiesRequest(this.selectedParentId)
      .then((response) => this.handleGetEntitiesResponse(response.data))
      .catch((error) => this.handleGetEntitiesError(error))
      .finally()
  },

  handleGetEntitiesResponse(entities) {
    this.entities = entities
  },

  handleGetEntitiesError(error) {
    const statusCode = error.response.status
    this.errorResponse = error.response.data

    if (statusCode === 404 && this.errorResponse.errorCode === 222) {
      this.errorMessage = this.errorResponse.message
      this.entities = []
    } else {
      NavigationService.navigateToErrorView()
    }
  },
},
```

---

## Lifecycle hook

Andmete laadimine käib `beforeMount` sees (mitte `mounted`):

```js
beforeMount() {
  this.successMessage = this.$route.query.successMessage ?? ''
  this.getParents()
  this.getEntities()
},
```

---

## Template — sündmuste ja propide sidumine

**Propid** antakse alla `:`-ga (lühivorm `v-bind:`):
```html
<ParentsDropdown :parents="parents" :selected-parent-id="selectedParentId" />
```

**Sündmused** kuulatakse `@`-ga (lühivorm `v-on:`):
```html
<EntitiesTable @event-entity-deleted="handleEntityDeleted" />
```

Lihtsad sündmused võib kirjutada otse template'i:
```html
@event-modal-closed="isInfoModalOpen = false"
@event-new-parent-selected="entity.parentId = $event"
```

---

## Täielik näidiskomponent

```vue
<template>
  <div class="container">
    <AlertError :error-message="errorMessage" />

    <ParentsDropdown
      :parents="parents"
      :selected-parent-id="selectedParentId"
      @event-new-parent-selected="handleParentSelected"
    />

    <button @click="save" class="btn btn-success">Salvesta</button>
  </div>
</template>

<script>
import AlertError from '@/components/alerts/AlertError.vue'
import ParentsDropdown from '@/components/ParentsDropdown.vue'
import ParentService from '@/api-services/ParentService.js'
import NavigationService from '@/navigation/NavigationService.js'

export default {
  name: 'NäidisView',
  components: { AlertError, ParentsDropdown },
  props: {
    startParentId: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-saved'],
  data() {
    return {
      errorMessage: '',
      selectedParentId: 0,
      parents: [],
    }
  },
  computed: {
    hasParent() {
      return this.selectedParentId !== 0
    },
  },
  methods: {
    getParents() {
      ParentService.sendGetParentsRequest()
        .then((response) => (this.parents = response.data))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },

    handleParentSelected(parentId) {
      this.selectedParentId = parentId
    },

    save() {
      if (!this.hasParent) {
        this.errorMessage = 'Vali väärtus'
        return
      }
      this.$emit('event-saved', this.selectedParentId)
    },
  },
  beforeMount() {
    this.selectedParentId = this.startParentId
    this.getParents()
  },
}
</script>
```
