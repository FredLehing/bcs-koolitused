import axios from 'axios'

export default {
  // Backend task: docs/tasks/backend/GET-api-rooms.md (ainult aktiivsed — room-deleted-status.md)
  sendGetRoomsRequest() {
    return axios.get('/api/rooms')
  },

  // Backend task: docs/tasks/backend/GET-api-admin-rooms.md
  sendGetAdminRoomsRequest(includeDeleted) {
    return axios.get('/api/admin-rooms', {
      params: {
        includeDeleted: includeDeleted,
      },
    })
  },

  // Backend task: docs/tasks/backend/GET-api-room-roomId.md
  sendGetRoomRequest(roomId) {
    return axios.get(`/api/room/${roomId}`)
  },

  // Backend task: docs/tasks/backend/POST-api-room.md
  sendPostRoomRequest(roomCreateRequest) {
    return axios.post('/api/room', roomCreateRequest)
  },

  // Backend task: docs/tasks/backend/PUT-api-room-roomId.md
  sendPutRoomRequest(roomId, roomUpdateRequest) {
    return axios.put(`/api/room/${roomId}`, roomUpdateRequest)
  },

  // Backend task: docs/tasks/backend/DELETE-api-room-roomId.md
  sendDeleteRoomRequest(roomId) {
    return axios.delete(`/api/room/${roomId}`)
  },

  // Backend task: docs/tasks/backend/PUT-api-room-roomId-restore.md
  sendPutRoomRestoreRequest(roomId) {
    return axios.put(`/api/room/${roomId}/restore`)
  },
}
