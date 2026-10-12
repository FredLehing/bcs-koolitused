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
  <ul
    ref="tabList"
    class="mb-6 flex gap-6 overflow-x-auto border-b border-line [scrollbar-width:thin]"
  >
    <li v-for="tab in tabs" :key="tab.routeName" class="shrink-0">
      <RouterLink
        :to="{ name: tab.routeName }"
        :class="
          $route.name === tab.routeName
            ? 'active border-brand-600 text-brand-600'
            : 'border-transparent text-muted hover:text-ink'
        "
        :aria-current="$route.name === tab.routeName ? 'page' : undefined"
        class="-mb-px block border-b-[3px] px-0.5 pt-2 pb-3 font-semibold whitespace-nowrap"
      >
        {{ tab.label }}
      </RouterLink>
    </li>
  </ul>
</template>
