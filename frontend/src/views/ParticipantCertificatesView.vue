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
  <div class="container">
    <div class="row g-4 text-start mb-5">
      <div class="col-lg-3">
        <ProfileMenu />
      </div>

      <div class="col-lg-9">
        <fieldset class="border rounded bg-body p-3">
          <legend class="float-none w-auto px-2 fs-5">
            {{ $t('navbar.participantCertificates') }}
          </legend>
          <div class="d-flex align-items-center gap-3 text-secondary">
            <PhCertificate :size="40" />
            <p class="mb-0">{{ $t('participantCertificates.comingSoon') }}</p>
          </div>
        </fieldset>
      </div>
    </div>
  </div>
</template>
