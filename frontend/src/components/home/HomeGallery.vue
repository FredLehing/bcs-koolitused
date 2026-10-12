<script>
import { PhCaretLeft, PhCaretRight } from '@phosphor-icons/vue'

// Galerii pildid loetakse automaatselt kaustast src/assets/images/gallery/ (failinime järjekorras).
// Uue pildi lisamiseks pane fail sinna kausta — koodi muuta pole vaja. Järjekorra määrab
// failinime numbriline eesliide (01_, 02_, ...). SVG-d (logod) kuvatakse tervikuna, mitte lõigatult.
const galleryImageModules = import.meta.glob('@/assets/images/gallery/*.{jpg,jpeg,png,webp,svg}', {
  eager: true,
  import: 'default',
})
const GALLERY_IMAGES = Object.keys(galleryImageModules)
  .sort()
  .map((imagePath) => galleryImageModules[imagePath])

// Kuni päris pilte pole, kuvatakse nii mitu kohatäitjat
const PLACEHOLDER_SLIDE_COUNT = 4

// Mitme millisekundi tagant karussell järgmise pildi peale liigub
const AUTOPLAY_INTERVAL_MS = 8000

export default {
  name: 'HomeGallery',
  components: { PhCaretLeft, PhCaretRight },

  data() {
    return {
      images: GALLERY_IMAGES,
      activeIndex: 0,
      autoplayTimerId: null,
    }
  },
  computed: {
    slideCount() {
      return this.images.length > 0 ? this.images.length : PLACEHOLDER_SLIDE_COUNT
    },
  },
  methods: {
    getImageFitClass(image) {
      return image.endsWith('.svg') ? 'object-contain p-8 sm:p-16' : 'object-cover'
    },

    showSlide(slideIndex) {
      this.activeIndex = (slideIndex + this.slideCount) % this.slideCount
      this.restartAutoplay()
    },

    restartAutoplay() {
      clearInterval(this.autoplayTimerId)
      this.autoplayTimerId = setInterval(
        () => (this.activeIndex = (this.activeIndex + 1) % this.slideCount),
        AUTOPLAY_INTERVAL_MS,
      )
    },
  },
  mounted() {
    this.restartAutoplay()
  },
  beforeUnmount() {
    clearInterval(this.autoplayTimerId)
  },
}
</script>

<template>
  <section
    class="relative overflow-hidden rounded-2xl bg-brand-100 shadow-sm"
    aria-roledescription="carousel"
  >
    <div
      class="flex transition-transform duration-500 ease-out"
      :style="{ transform: `translateX(-${activeIndex * 100}%)` }"
    >
      <template v-if="images.length > 0">
        <img
          v-for="(image, imageIndex) in images"
          :key="image"
          :src="image"
          :aria-hidden="imageIndex !== activeIndex"
          :class="getImageFitClass(image)"
          class="aspect-video w-full shrink-0"
          alt=""
        />
      </template>
      <template v-else>
        <div
          v-for="slideIndex in slideCount"
          :key="slideIndex"
          class="flex aspect-video w-full shrink-0 items-center justify-center text-xl text-brand-700"
        >
          {{ $t('homeView.gallery.placeholder', { number: slideIndex }) }}
        </div>
      </template>
    </div>

    <button
      @click="showSlide(activeIndex - 1)"
      :aria-label="$t('homeView.gallery.previous')"
      class="absolute top-1/2 left-3 flex size-11 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full bg-navy/55 text-white hover:bg-navy/75"
      type="button"
    >
      <PhCaretLeft :size="22" weight="bold" />
    </button>
    <button
      @click="showSlide(activeIndex + 1)"
      :aria-label="$t('homeView.gallery.next')"
      class="absolute top-1/2 right-3 flex size-11 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full bg-navy/55 text-white hover:bg-navy/75"
      type="button"
    >
      <PhCaretRight :size="22" weight="bold" />
    </button>

    <div class="absolute inset-x-0 bottom-3 flex justify-center gap-1">
      <button
        v-for="slideIndex in slideCount"
        :key="slideIndex"
        @click="showSlide(slideIndex - 1)"
        :aria-current="slideIndex - 1 === activeIndex ? 'true' : undefined"
        :aria-label="$t('homeView.gallery.slide', { number: slideIndex })"
        class="flex size-6 cursor-pointer items-center justify-center"
        type="button"
      >
        <span
          :class="slideIndex - 1 === activeIndex ? 'w-6 bg-white' : 'w-2 bg-white/60'"
          class="h-2 rounded-full shadow transition-all"
        ></span>
      </button>
    </div>
  </section>
</template>
