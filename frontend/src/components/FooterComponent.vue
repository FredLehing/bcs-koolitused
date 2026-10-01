<script setup>
import { PhFacebookLogo, PhMapPin } from '@phosphor-icons/vue'
</script>

<template>
  <footer class="mt-auto border-t border-line bg-white">
    <div
      class="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-10 text-[15px] text-muted sm:px-6 md:flex-row md:items-start md:justify-between"
    >
      <img src="@/assets/bcs-koolitus.svg" alt="BCS Koolitus" class="h-10 w-auto self-start" />
      <div class="flex flex-col gap-2">
        <span class="inline-flex items-center gap-2">
          <PhMapPin :size="20" class="text-brand-600" />
          BCS Koolitus AS, Aia 7, Tallinn
        </span>
        <a href="#" class="inline-flex items-center gap-2">
          <PhFacebookLogo :size="20" />
          {{ $t('footer.facebook') }}
        </a>
      </div>
      <div class="flex flex-col gap-2">
        <a href="#" class="text-muted hover:text-brand-700">{{ $t('footer.privacy') }}</a>
        <a href="#" class="text-muted hover:text-brand-700">{{ $t('footer.studyTerms') }}</a>
      </div>
    </div>
  </footer>
</template>
