<script setup>
import {PhFacebookLogo, PhMapPin} from '@phosphor-icons/vue'
</script>

<template>
  <footer class="mt-auto py-3">
    <div class="container">
      <div class="row">
        <div class="col-6">
          <div>
            <PhFacebookLogo />
            {{ $t('footer.facebook') }}
          </div>
          <div>
            <PhMapPin />
            BCS Koolitus AS, Aia 7, Tallinn
          </div>
        </div>

        <div class="col-6 d-flex justify-content-end pe-5">
          <div>
            <div>{{ $t('footer.privacy') }}</div>
            <div>{{ $t('footer.studyTerms') }}</div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>
