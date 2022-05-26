<template>
  <div>
    <h2 class="my-3">유저 목록</h2>
    <admin-user
      v-for="user, index in users"
      :key="index"
      :user="user"
      class="admin-list-border"
    ></admin-user>
  </div>    
</template>

<script>
import axios from 'axios'
import AdminUser from './AdminUser.vue'
export default {
  components: { AdminUser },
  name: "AdminUsers",
  data: function() {
    return {
      users: null,
    }
  },
  methods: {

  },
  created() {
    let http_url = this.$store.state.http_url
    let url_adminuser = this.$store.state.url_adminuser2

    const token = localStorage.getItem('jwt')
    
    axios.get(
      `${http_url}${url_adminuser}`,
      {
        headers: {
          "Content-Type": `application/json`,
          "access-token": token,
        },
      }
    )
    .then((res) => {
      this.users = res.data.users
    })
    .catch(() => {
      
    })
  }
}
</script>

<style>
.admin-list-border{
  border: 1px solid;
}
</style>
