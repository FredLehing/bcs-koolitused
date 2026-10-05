<script>
import { Carousel } from 'bootstrap'

// Galerii pildid loetakse automaatselt kaustast src/assets/images/gallery/ (failinime järjekorras).
// Uue pildi lisamiseks pane fail sinna kausta — koodi muuta pole vaja.
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

  data() {
    return {
      images: GALLERY_IMAGES,
    }
  },
  computed: {
    slideCount() {
      return this.images.length > 0 ? this.images.length : PLACEHOLDER_SLIDE_COUNT
    },
  },
  mounted() {
    // Käivitame automaatse kerimise ise: data-bs-ride="carousel" loetakse ainult lehe esmalaadimisel,
    // aga SPA-s võib komponent tekkida hiljem (nt router-lingiga avalehele tulles)
    this.carousel = Carousel.getOrCreateInstance(this.$refs.carouselElement, {
      interval: AUTOPLAY_INTERVAL_MS,
      ride: 'carousel',
    })
  },
  beforeUnmount() {
    // Peatame taimeri ja eemaldame Bootstrapi kuularid, kui avalehelt lahkutakse
    this.carousel.dispose()
  },
}
</script>

<template>
  <!-- Bootstrapi karussell: nooled ja indikaatorid töötavad data-bs-* atribuutidega, automaatne kerimine käivitatakse mounted()-is -->
  <section class="py-4">
    <div
      id="homeGallery"
      ref="carouselElement"
      class="carousel slide rounded overflow-hidden shadow-sm"
    >
      <div class="carousel-indicators">
        <button
          v-for="slideIndex in slideCount"
          :key="slideIndex"
          type="button"
          data-bs-target="#homeGallery"
          :data-bs-slide-to="slideIndex - 1"
          :class="{ active: slideIndex === 1 }"
          :aria-current="slideIndex === 1 ? 'true' : undefined"
          :aria-label="$t('homeView.gallery.slide', { number: slideIndex })"
        ></button>
      </div>

      <div class="carousel-inner">
        <template v-if="images.length > 0">
          <div
            v-for="(image, imageIndex) in images"
            :key="image"
            class="carousel-item"
            :class="{ active: imageIndex === 0 }"
          >
            <img :src="image" class="d-block w-100 gallery-image" alt="" />
          </div>
        </template>
        <template v-else>
          <div
            v-for="slideIndex in slideCount"
            :key="slideIndex"
            class="carousel-item"
            :class="{ active: slideIndex === 1 }"
          >
            <div
              class="gallery-image gallery-placeholder d-flex align-items-center justify-content-center"
            >
              <span class="fs-4 text-body-secondary">
                {{ $t('homeView.gallery.placeholder', { number: slideIndex }) }}
              </span>
            </div>
          </div>
        </template>
      </div>

      <button
        class="carousel-control-prev"
        type="button"
        data-bs-target="#homeGallery"
        data-bs-slide="prev"
      >
        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
        <span class="visually-hidden">{{ $t('homeView.gallery.previous') }}</span>
      </button>
      <button
        class="carousel-control-next"
        type="button"
        data-bs-target="#homeGallery"
        data-bs-slide="next"
      >
        <span class="carousel-control-next-icon" aria-hidden="true"></span>
        <span class="visually-hidden">{{ $t('homeView.gallery.next') }}</span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.gallery-image {
  height: 400px;
  object-fit: cover;
}

.gallery-placeholder {
  background: linear-gradient(135deg, var(--bs-secondary-bg), var(--bs-tertiary-bg));
}

/* Valged nooled ja täpid tumeda tausta/äärisega, et need paistaksid nii heledal kui tumedal pildil */
.carousel-control-prev-icon,
.carousel-control-next-icon {
  width: 3rem;
  height: 3rem;
  background-color: rgba(0, 0, 0, 0.45);
  background-size: 50%;
  border-radius: 50%;
}

/* Bootstrapi vaikimisi on noole ala 15% laiune ja nool selle keskel — lükkame nooled äärde */
.carousel-control-prev,
.carousel-control-next {
  width: auto;
  padding: 0 0.75rem;
}

.carousel-indicators [data-bs-target] {
  /* drop-shadow, mitte box-shadow: täpi nupul on läbipaistev ääris, box-shadow joonistaks selle ümber kasti */
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.6));
}

@media (max-width: 576px) {
  .gallery-image {
    height: 220px;
  }
}
</style>
