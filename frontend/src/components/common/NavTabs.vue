<script>
// Vahelehtedena lingid sama rühma vaadete vahel (nt admini nimekirjad, koolitused ja kalender).
// tabs: [{ routeName, label }]; aktiivne vaheleht tuleb praegusest marsruudist.
export default {
  name: 'NavTabs',
  props: {
    tabs: Array,
  },
  // Kitsal ekraanil keritakse rida nii, et aktiivne vaheleht on keskel — näha on ka naabrid
  mounted() {
    const tabList = this.$refs.tabList
    const activeTab = tabList.querySelector('.active')
    if (activeTab) {
      tabList.scrollLeft = activeTab.offsetLeft - (tabList.clientWidth - activeTab.offsetWidth) / 2
    }
  },
}
</script>

<template>
  <ul ref="tabList" class="nav nav-tabs nav-tabs-scroll mb-3">
    <li v-for="tab in tabs" :key="tab.routeName" class="nav-item">
      <RouterLink
        :to="{ name: tab.routeName }"
        :class="{ active: $route.name === tab.routeName }"
        :aria-current="$route.name === tab.routeName ? 'page' : undefined"
        class="nav-link"
      >
        {{ tab.label }}
      </RouterLink>
    </li>
  </ul>
</template>

<style scoped>
/* Kõik vahelehed ühel real; kui ekraanile ei mahu, keritakse rida külgsuunas (ei murra kahele reale) */
.nav-tabs-scroll {
  position: relative;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  /* Kerimine lõikaks aktiivse vahelehe alla ulatuva serva ära — alumine joon on seepärast vari,
     mille aktiivne vaheleht oma taustaga katab */
  border-bottom: 0;
  box-shadow: inset 0 calc(-1 * var(--bs-nav-tabs-border-width)) 0 var(--bs-nav-tabs-border-color);
}

.nav-tabs-scroll .nav-link {
  margin-bottom: 0;
  white-space: nowrap;
}
</style>
