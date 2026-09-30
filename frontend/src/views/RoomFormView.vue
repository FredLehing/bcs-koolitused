<script>
import RoomService from '@/api-services/RoomService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import AlertDanger from '@/components/common/AlertDanger.vue'

// Kaks olekut URL-ist: ilma roomId-ta uus ruum, roomId-ga muutmine
export default {
  name: 'RoomFormView',
  components: { AlertDanger },
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
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-lg-8">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-3">
          <h1 class="mb-0">
            {{ isNew ? $t('roomForm.title.new') : $t('roomForm.title.update') }}
          </h1>
          <button @click="navigateToAdminRoomsView" class="btn btn-outline-secondary" type="button">
            {{ $t('navbar.manageRooms') }}
          </button>
        </div>

        <fieldset class="border rounded p-3 mb-4">
          <legend class="float-none w-auto px-2 fs-5">{{ $t('roomForm.legend') }}</legend>
          <div class="text-start">
            <label class="form-label" for="roomName">{{ $t('roomForm.roomName') }} *</label>
            <input
              v-model="roomName"
              @keyup.enter="saveRoom"
              id="roomName"
              class="form-control"
              type="text"
              maxlength="255"
            />
          </div>
        </fieldset>

        <AlertDanger :error-message="errorMessage" />

        <div class="d-flex flex-wrap align-items-center gap-3 mb-5">
          <button @click="saveRoom" :disabled="isSending" class="btn btn-success" type="button">
            {{ isNew ? $t('roomForm.buttons.add') : $t('roomForm.buttons.save') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
