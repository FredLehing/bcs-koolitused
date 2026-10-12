<script>
import { PhQuotes } from '@phosphor-icons/vue'

// Tagasisided tulevad tõlkefailidest (homeView.testimonials.items), et need oleksid mõlemas keeles
export default {
  name: 'HomeTestimonials',
  components: { PhQuotes },
}
</script>

<template>
  <section>
    <h2 class="mb-6 text-2xl font-extrabold sm:text-3xl">
      {{ $t('homeView.testimonials.title') }}
    </h2>
    <div class="grid gap-5 md:grid-cols-3">
      <figure
        v-for="(testimonial, testimonialIndex) in $tm('homeView.testimonials.items')"
        :key="testimonialIndex"
        class="flex flex-col gap-4 rounded-2xl border border-line bg-white p-6"
      >
        <PhQuotes :size="32" weight="fill" class="text-brand-300" />
        <blockquote class="flex-1">{{ $rt(testimonial.text) }}</blockquote>
        <figcaption>
          <span class="font-semibold text-navy">{{ $rt(testimonial.name) }}</span>
          <span v-if="$rt(testimonial.name) && $rt(testimonial.company)" class="text-muted">
            ·
          </span>
          <span class="text-muted">{{ $rt(testimonial.company) }}</span>
        </figcaption>
      </figure>
    </div>
  </section>
</template>
