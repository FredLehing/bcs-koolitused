<script>
import { PhQuotes } from '@phosphor-icons/vue'

// Tagasisided tulevad tõlkefailidest (homeView.testimonials.items), et need oleksid mõlemas keeles
export default {
  name: 'HomeTestimonials',
  components: { PhQuotes },
}
</script>

<template>
  <section class="py-5">
    <h2 class="text-center fw-bold mb-4">{{ $t('homeView.testimonials.title') }}</h2>
    <div class="row g-4">
      <div
        v-for="(testimonial, testimonialIndex) in $tm('homeView.testimonials.items')"
        :key="testimonialIndex"
        class="col-md-4"
      >
        <div class="card h-100 shadow-sm">
          <div class="card-body d-flex flex-column">
            <PhQuotes :size="32" weight="fill" class="text-primary mb-3" />
            <p class="card-text fst-italic flex-grow-1">{{ $rt(testimonial.text) }}</p>
            <div>
              <div class="fw-semibold">{{ $rt(testimonial.name) }}</div>
              <div class="small text-body-emphasis">{{ $rt(testimonial.company) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
