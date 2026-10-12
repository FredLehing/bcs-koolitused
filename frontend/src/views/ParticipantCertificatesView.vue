<script>
import { PhCertificate } from '@phosphor-icons/vue'
import ProfileMenu from '@/components/profile/ProfileMenu.vue'

// Tunnistused: /participant-certificates (router guard: sisse logimata → login, admin → NotAuthorizedView).
// Sisu tuleb hiljem (vt docs/mock-wireframe/loo-mock-vaade/profile-view/profile-view-skeemid.md, jaotis 7)
export default {
  name: 'ParticipantCertificatesView',
  components: { PhCertificate, ProfileMenu },
}
</script>

<template>
  <div class="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-10">
    <div class="lg:grid lg:grid-cols-[14rem_1fr] lg:items-start lg:gap-8">
      <ProfileMenu />

      <section class="rounded-2xl border border-line bg-white p-5 sm:p-6">
        <h1 class="mb-4 text-xl sm:text-2xl">{{ $t('navbar.participantCertificates') }}</h1>
        <div class="flex items-center gap-3 text-muted">
          <PhCertificate :size="40" class="shrink-0" />
          <p class="mb-0">{{ $t('participantCertificates.comingSoon') }}</p>
        </div>
      </section>
    </div>
  </div>
</template>
