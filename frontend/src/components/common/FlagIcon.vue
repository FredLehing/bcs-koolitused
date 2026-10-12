<script>
// Keele lipp (flag-icons, https://flagicons.lipis.dev/). Raam hoiab valged lipuosad heledal
// taustal nähtavana. Suuruse saab anda väljast teksti suuruse klassiga (nt class="text-2xl").
export default {
  name: 'FlagIcon',
  props: {
    flagIconCode: String,
  },
}
</script>

<template>
  <span class="fi rounded-[3px] ring-1 ring-slate-300" :class="flagIconCode"></span>
</template>
