<script>
// Leheküljestus. page on 0-põhine nagu backendi päringus,
// kasutajale kuvatakse numbrid alates 1-st.
export default {
  name: 'PaginationNav',
  props: {
    page: {
      type: Number,
      default: 0,
    },
    totalPages: {
      type: Number,
      default: 0,
    },
  },
  emits: ['event-page-changed'],
  computed: {
    isFirstPage() {
      return this.page === 0
    },

    isLastPage() {
      return this.page >= this.totalPages - 1
    },
  },
  methods: {
    changePage(newPage) {
      if (newPage < 0 || newPage >= this.totalPages || newPage === this.page) {
        return
      }
      this.$emit('event-page-changed', newPage)
    },
  },
}
</script>

<template>
  <nav v-if="totalPages > 0" :aria-label="$t('pagination.label')">
    <ul class="flex flex-wrap items-center justify-center gap-1.5">
      <li v-if="!isFirstPage">
        <a
          class="page-button border-line bg-white px-3.5 text-ink hover:border-brand-600 hover:text-brand-700"
          href="#"
          @click.prevent="changePage(page - 1)"
        >
          ← {{ $t('pagination.previous') }}
        </a>
      </li>
      <li v-for="pageNumber in totalPages" :key="pageNumber">
        <a
          :class="
            pageNumber === page + 1
              ? 'border-brand-600 bg-brand-600 text-white hover:text-white'
              : 'border-line bg-white text-ink hover:border-brand-600 hover:text-brand-700'
          "
          class="page-button"
          href="#"
          :aria-current="pageNumber === page + 1 ? 'page' : null"
          @click.prevent="changePage(pageNumber - 1)"
        >
          {{ pageNumber }}
        </a>
      </li>
      <li v-if="!isLastPage">
        <a
          class="page-button border-line bg-white px-3.5 text-ink hover:border-brand-600 hover:text-brand-700"
          href="#"
          @click.prevent="changePage(page + 1)"
        >
          {{ $t('pagination.next') }} →
        </a>
      </li>
    </ul>
  </nav>
</template>

<style scoped>
.page-button {
  display: inline-flex;
  min-width: 2.75rem;
  height: 2.75rem;
  align-items: center;
  justify-content: center;
  border-width: 1px;
  border-radius: 0.625rem;
  font-weight: 600;
}
</style>
