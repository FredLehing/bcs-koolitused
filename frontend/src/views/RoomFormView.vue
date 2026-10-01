<script>
import BackLink from '@/components/common/BackLink.vue'
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'

// Kaks olekut URL-ist: ilma roomId-ta uus ruum, roomId-ga muutmine
export default {
  name: 'RoomFormView',
  components: { BackLink, AlertDanger },
  data() {
    return {
      errorMessage: '',
      isSending: false,
      roomId: 0,
      roomName: '',
    }
  },
  computed: {
    isNew() {
      return this.roomId === 0
    },
  },
  methods: {
    loadView() {
      this.roomId = Number(this.$route.query.roomId ?? 0)
      this.errorMessage = ''
      this.roomName = ''
      if (!this.isNew) {
        this.getRoom()
      }
    },

    getRoom() {
      RoomService.sendGetRoomRequest(this.roomId)
        .then((response) => (this.roomName = response.data.roomName))
        .catch(() => NavigationService.navigateToErrorView())
    },

    // ---------- "Lisa" / "Salvesta" ----------

    saveRoom() {
      this.errorMessage = ''
      if (this.roomName.trim() === '') {
        this.errorMessage = this.$t('roomForm.validation.fillRequired')
        return
      }
      this.isSending = true
      const request = this.isNew
        ? RoomService.sendPostRoomRequest({
            userId: SessionStorageService.getUserId(),
            roomName: this.roomName,
          })
        : RoomService.sendPutRoomRequest(this.roomId, { roomName: this.roomName })
      request
        .then(() => this.handleSaveRoomResponse())
        .catch((error) => this.handleSaveRoomError(error))
        .finally(() => (this.isSending = false))
    },

    handleSaveRoomResponse() {
      const successMessage = this.isNew
        ? this.$t('roomForm.messages.added')
        : this.$t('roomForm.messages.saved')
      NavigationService.navigateToAdminRoomsView(successMessage)
    },

    // 403 ROOM_NAME_EXISTS ja 400 → backendi teade vormis; muu (nt vahepeal kustutatud ruum) → veavaade
    handleSaveRoomError(error) {
      const statusCode = error.response?.status
      if (statusCode === 400 || statusCode === 403) {
        this.errorMessage = error.response.data?.message ?? ''
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    navigateToAdminRoomsView() {
      NavigationService.navigateToAdminRoomsView()
    },
  },
  beforeMount() {
    if (SessionStorageService.userIsAdmin()) {
      this.loadView()
    } else {
      NavigationService.navigateToNotAuthorizedView()
    }
  },
}
</script>

<template>
  <div class="mx-auto w-full max-w-6xl px-6 py-8">
    <BackLink :fallback="{ name: 'adminRoomsRoute' }" />
    <div class="max-w-2xl">
      <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
        <h1 class="text-3xl font-extrabold tracking-tight">
          {{ isNew ? $t('roomForm.title.new') : $t('roomForm.title.update') }}
        </h1>
        <button @click="navigateToAdminRoomsView" class="btn btn-outline-secondary" type="button">
          {{ $t('navbar.manageRooms') }}
        </button>
      </div>

      <section class="rounded-2xl border border-line bg-white p-6" aria-labelledby="room-heading">
        <h2 id="room-heading" class="mb-4 text-lg font-bold">{{ $t('roomForm.legend') }}</h2>
        <label class="form-label" for="roomName">{{ $t('roomForm.roomName') }} *</label>
        <input
          v-model="roomName"
          @keyup.enter="saveRoom"
          id="roomName"
          class="form-control"
          type="text"
          maxlength="255"
        />
      </section>

      <AlertDanger :error-message="errorMessage" class="mt-6" />

      <div class="mt-6 flex flex-wrap items-center gap-3">
        <button @click="saveRoom" :disabled="isSending" class="btn btn-primary" type="button">
          {{ isNew ? $t('roomForm.buttons.add') : $t('roomForm.buttons.save') }}
        </button>
      </div>
    </div>
  </div>
</template>
