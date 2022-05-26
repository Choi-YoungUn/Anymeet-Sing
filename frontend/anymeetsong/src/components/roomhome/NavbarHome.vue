<template>
  <div id="Navbar">
    <b-navbar toggleable="lg" type="dark" class="px-3 myNavbar navbar-setting">
      <b-navbar-brand style="font-size:25px; color:white; cursor: default;" @click="clickLogo">Any Meet & Sing</b-navbar-brand>
      <b-navbar-brand @click="clickLogo" style="cursor: pointer;"><img src="../../assets/images/logo.png" alt="" class="mx-auto" style="width: 150px;"></b-navbar-brand>

      <div id="nav-collapse" is-nav>
        
      <b-navbar-nav class="navbar-menu">
        <b-nav-item-dropdown right style="width: 200px;">
          <template #button-content>
            <em class="text-white fs-4">Menu</em>
          </template>
          <b-dropdown-item @click="clickProfile">회원정보</b-dropdown-item>
          <b-dropdown-item @click="clickLogout">로그아웃</b-dropdown-item>
          <b-dropdown-item v-if="isAdmin" @click="clickAdmin">Admin</b-dropdown-item>
        </b-nav-item-dropdown>
      </b-navbar-nav>
      </div>
    </b-navbar>

    <router-view/>

  </div>
</template>

<script>
import axios from 'axios'


export default {
  name:"Navbar",
  
  data: function() {
    return {
      
    }
  },
  methods : {
    clickLogout: function() {
        
      let credentials = {
        id: this.$store.state.myid,
        roomId : this.$store.state.enterroom.id
      }
      
      const token = localStorage.getItem('jwt')
      axios.post(
        this.$store.state.http_url+ this.$store.state.url_user + "logout",
        JSON.stringify(credentials),
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(() => {
        localStorage.removeItem('jwt') 
        //노래방 리스트 페이지로 이동용
        
        this.$router.push({name: 'LogHome'})
      })
      .catch(() => { 
        alert("로그아웃 실패")
      })
    }, 
    clickProfile: function() {
      this.$router.push({name: 'Profile'})
    },
    clickAdmin: function() {
      this.$router.push({name: 'Admin'})
    },
    clickLogo: function() {
      this.$router.push({name: 'RoomHome'}).catch(()=>{});
    },
    
  },
  computed: {
    mynickname: function() {
      return this.$store.state.mynickname
    },
    isAdmin: function() {
      return this.$store.state.is_admin
    }
  },

  created: function() {
    
  }
}
</script>

<style>
.myNavbar {
  background: rgba(100, 100, 100, 0.5);
  padding: 10px;
  backdrop-filter: blur(10px);
}

.navbar-setting {
  justify-content: space-between !important;
  font: bold;
}

.navbar-menu {
  width: 200px;
  text-align: right;
  display: inline !important;
}
</style>