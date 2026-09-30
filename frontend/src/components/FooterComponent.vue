<script setup>
import { PhFacebookLogo, PhMapPin } from '@phosphor-icons/vue'
</script>

<template>
  <footer class="mt-auto py-3 footer-bcs">
    <div class="container">
      <div class="row">
        <div class="col-6 d-flex align-items-center gap-2">
          <PhFacebookLogo size="32" />
          {{ $t('footer.facebook') }}
        </div>
        <div class="col-6 d-flex align-items-center justify-content-end pe-5">
          {{ $t('footer.privacy') }}
        </div>
      </div>
      <div class="row">
        <div class="col-6 d-flex align-items-center gap-2">
          <PhMapPin size="32" />
          BCS Koolitus AS, Aia 7, Tallinn
        </div>
        <div class="col-6 d-flex align-items-center justify-content-end pe-5">
          {{ $t('footer.studyTerms') }}
        </div>
      </div>
    </div>
  </footer>
</template>
