import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

import App from './App.vue'
import router from './router'
import i18n from './i18n'

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
// JS tuleb importida samast sisenemispunktist ('bootstrap' → ESM build), mida kasutavad
// komponendid (nt import { Tooltip } from 'bootstrap'). Kaks eri buildi korraga registreeriksid
// dropdownide klikikäsitleja kaks korda — menüü avaneks ja sulguks kohe uuesti.
import 'bootstrap'

// Lipuikoonid (flag-icons)
import 'flag-icons/css/flag-icons.min.css'

// Extra imports
// leafleti css kujindused
import 'leaflet/dist/leaflet.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)

// Axios globaalselt kättesaadavaks
app.config.globalProperties.$axios = axios

app.mount('#app')
