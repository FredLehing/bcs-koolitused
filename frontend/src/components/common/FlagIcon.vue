<script>
// Keele lipp (flag-icons, https://flagicons.lipis.dev/). Raam hoiab valged lipuosad heledal
// taustal nähtavana. Suuruse saab anda väljast klassiga (nt class="fs-3").
export default {
  name: 'FlagIcon',
  props: {
    flagIconCode: String,
  },
}
</script>

<template>
  <span class="fi border border-secondary-subtle" :class="flagIconCode"></span>
</template>
