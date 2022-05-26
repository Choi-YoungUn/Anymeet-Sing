<template>
  <div>
    <h2 class="my-3">방 목록</h2>
    <admin-room
      v-for="room, index in rooms"
      :key="index"
      :room="room"
      class="admin-list-border"
    ></admin-room>
  </div>
</template>

<script>
import axios from 'axios'
import AdminRoom from './AdminRoom.vue'
export default {
  components: { AdminRoom },
  name: "AdminRooms",
  data: function() {
    return {
      rooms: null,
    }
  },
  methods: {

  },
  created() {
    let http_url = this.$store.state.http_url
    let url_adminroom = this.$store.state.url_adminroom

    const token = localStorage.getItem('jwt')

    axios.get(
      `${http_url}${url_adminroom}`,
      {
        headers: {
          "Content-Type": `application/json`,
          "access-token": token,
        },
      }
    )
    .then((res) => {
      this.rooms = res.data.rooms
    })
    .catch(() => {
      
    })
  }
}
</script>

<style>

</style>
