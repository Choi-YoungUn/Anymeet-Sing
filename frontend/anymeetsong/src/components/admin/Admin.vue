<template>
  <div class="admin-menu">
    <b-button @click="clickUser" class="mx-3">Users</b-button>
    <b-button @click="clickRoom">Rooms</b-button>
    <admin-users v-if="isUser"></admin-users>
    <admin-rooms v-if="isRoom"></admin-rooms>
  </div>
</template>

<script>
import AdminRooms from './AdminRooms.vue'
import AdminUsers from './AdminUsers.vue'
export default {
  components: { AdminUsers, AdminRooms },
  name: "Admin",
  data: function() {
    return {
      isRoom: false,
      isUser: true,
    }
  },
  methods: {
    clickUser: function () {
      this.isUser = true
      this.isRoom = false
    },
    clickRoom: function () {
      this.isUser = false
      this.isRoom = true
    }
  },
  created() {
    if(this.$store.state.is_admin != 1) {
      alert("권한이 없습니다.")
      this.$router.go(-1)
    }
  }
}
</script>

<style>
.admin-menu {
  margin-top: 50px;
}
</style>
