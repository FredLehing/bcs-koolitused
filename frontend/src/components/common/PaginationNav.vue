<script>
// Leheküljestus (Bootstrap pagination). page on 0-põhine nagu backendi päringus,
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
    <ul class="pagination justify-content-center mb-0">
      <li v-if="!isFirstPage" class="page-item">
        <a class="page-link" href="#" @click.prevent="changePage(page - 1)">
          {{ $t('pagination.previous') }}
        </a>
      </li>
      <li
        v-for="pageNumber in totalPages"
        :key="pageNumber"
        class="page-item"
        :class="{ active: pageNumber === page + 1 }"
      >
        <a
          class="page-link"
          href="#"
          :aria-current="pageNumber === page + 1 ? 'page' : null"
          @click.prevent="changePage(pageNumber - 1)"
        >
          {{ pageNumber }}
        </a>
      </li>
      <li v-if="!isLastPage" class="page-item">
        <a class="page-link" href="#" @click.prevent="changePage(page + 1)">
          {{ $t('pagination.next') }}
        </a>
      </li>
    </ul>
  </nav>
</template>
