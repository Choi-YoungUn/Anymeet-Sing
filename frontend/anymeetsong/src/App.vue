<template>
<!-- version 0.000.01 -->
  <div id="app">
    <!-- <div id="nav">
      <router-link to="/">Home</router-link> |
      <router-link to="/about">About</router-link>
    </div> -->
    
    <router-view/>
  </div>
</template>

<script>
import VueJwtDecode from 'vue-jwt-decode'
import axios from 'axios'

export default {
  name: "app",
  created: function(){ 
    const token = localStorage.getItem('jwt')
    // console.log(token)

    if (token) {
      // 토큰 있으면 로그인하기
      const temp = VueJwtDecode.decode(token)
      
      let now = new Date();
      let expiry = temp.exp - Number(now.getTime().toString().substr(0, 10));
      // console.log(expiry)
      console.log(this.$router.history.current.name)
      if (expiry < 8000){
        localStorage.removeItem('jwt')
        this.$router.push({name: 'LogHome'})
      } else if (this.$router.history.current.name === 'LogHome' || 
          this.$router.history.current.name === 'IdFind' || 
          this.$router.history.current.name === 'PwFind' || 
          this.$router.history.current.name === 'SignUp'){
        this.$router.push({name: 'RoomHome'}).catch(()=>{});
      } 
    } else {
      this.$router.push({name: 'LogHome'}).catch(()=>{});
    }
  },
  beforeDestroy: function(){
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
    })
    


  },
  mounted() {
    const backgroundImg = document.getElementById("app")
    backgroundImg.className = "back1";
    const routerName = this.$route.path
    if (routerName==="/") {
      document.getElementById("app").className = "back1";
    }else if (routerName==="/SignUp") {
      document.getElementById("app").className = "back1-1";
    }else if (routerName==="/ChangePw") {
      document.getElementById("app").className = "back3";
    }else if (routerName==="/EditMyInfo") {
      document.getElementById("app").className = "back3";
    }else if (routerName==="/IdFind") {
      document.getElementById("app").className = "back1";
    }else if (routerName==="/Profile") {
      document.getElementById("app").className = "back3";
    }else if (routerName==="/PwFind") {
      document.getElementById("app").className = "back1";
    }else if (routerName==="/RoomHome") {
      document.getElementById("app").className = "back2";
    }else if (routerName==="/CreateRoom") {
      document.getElementById("app").className = "back4";
    }
  },
  watch:{
    $route() {
      const routerName = this.$route.path
      if (routerName==="/") {
        document.getElementById("app").className = "back1";
      }else if (routerName==="/SignUp") {
        document.getElementById("app").className = "back1-1";
      }else if (routerName==="/ChangePw") {
        document.getElementById("app").className = "back3";
      }else if (routerName==="/EditMyInfo") {
        document.getElementById("app").className = "back3";
      }else if (routerName==="/IdFind") {
        document.getElementById("app").className = "back1";
      }else if (routerName==="/Profile") {
        document.getElementById("app").className = "back3";
      }else if (routerName==="/PwFind") {
        document.getElementById("app").className = "back1";
      }else if (routerName==="/RoomHome") {
        document.getElementById("app").className = "back2";
      }else if (routerName==="/CreateRoom") {
        document.getElementById("app").className = "back4";
      }
    }
  }
}
</script>


<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #000000;
  min-height: 100vh;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
