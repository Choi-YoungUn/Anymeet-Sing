<template>
  <div v-if="isDelete" class="admin-room mx-auto" @click="showDetail">
    <div style="background: #EEEEEE; border-bottom: 1px solid;">{{ room.title }}</div>

    <div v-if="isDetail">

      <div class="admin-room-div mt-3">
        <label for="" class="signuplabel">방번호(pk):</label>
        <div class="admin-user-nick">{{ room.id }}</div>
      </div>
      
      <div class="admin-room-div">
        <label for="" class="signuplabel">host id:</label>
        <div class="admin-user-nick">{{ room.host_id }}</div>
      </div>

      <div class="admin-room-button">
        <b-button @click="deleteRoom" class="admin-btn my-2">삭제하기</b-button>
      </div>

    </div>
  </div>  
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminRoom',
  props: {
    room: {
      type: Object,
    }
  },
  data: function() {
    return {
      isDetail: false,
      isDelete: true,
    }
  },
  methods: {
    showDetail: function() {
      this.isDetail = !this.isDetail
    },
    deleteRoom: function() {
      let http_url = this.$store.state.http_url
      let url_admin_delete_room = this.$store.state.url_admin_delete_room
      let roomNum = this.room.id

      const token = localStorage.getItem('jwt')
      // console.log(`${http_url}${url_admin_delete_room}/${roomNum}`)
      axios.delete(
        `${http_url}${url_admin_delete_room}/${roomNum}`,
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          }
        }
      )
      .then(() => {
        this.isDelete = false
      })
    },
  }
}
</script>

<style>
.admin-room {
  width: 500px;
}

.admin-room-button {
  text-align: center;
}

.admin-room-div {
  width: 500px;
  height: 30px;
  text-align: left;
}
</style>
