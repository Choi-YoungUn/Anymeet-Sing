<template>
  <div id="LogHome">

    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label text1"></label>
      <div class="col-sm-10">
        <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
          <img src="../../assets/images/logo.png" alt="" style="width: 400px; margin-top:100px; margin-bottom:100px;">
        </div>
      </div>
    </div>

    <!-- <img src="../../assets/images/logo.png" alt="" style="width: 400px; margin:100px;"> -->
    <!-- <h1 class="title" style="cursor:default; color:black;">Any Meet & Sing</h1> -->
    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label text1">ID (Email)</label>
      <div class="col-sm-10">
        <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
          <input type="text" class="form-control" placeholder="Email" aria-label="Username" v-model="Username">
          <!-- <button class="btn btn-outline-secondary" type="button" id="button-addon2">Button</button> -->
        </div>
      </div>
    </div>
    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label text1">Password</label>
      <div class="col-sm-10">
        <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
          <input type="password" class="form-control" placeholder="Password" aria-label="Password" v-model="Password" @keyup.enter="clickLogin">
        </div>
      </div>
    </div>
    <div>
      
    </div>
    <!-- <p>{{Username}}@{{Server}}</p>
    <p>{{Password}}</p> -->
    <div class="login-window mx-auto">
      <div class="my-5">
        <label for="" class="col-sm-2 col-form-label text1"></label>
        <b-button @click="clickLogin" class="button1" variant="primary">로그인</b-button>
      </div>
      <p class="text1" style="cursor:default;"><label for="" class="col-sm-2 col-form-label text1"></label>계정이 없으신가요?</p>
      <div class="mb-3">
        <label for="" class="col-sm-2 col-form-label text1"></label>
        <b-button @click="clickSighup" variant="primary" class="button1">회원가입</b-button>
      </div>
      <p class="text1" style="cursor:default;"><label for="" class="col-sm-2 col-form-label text1"></label>계정을 분실하셨나요?</p>
      <div>
        <label for="" class="col-sm-2 col-form-label text1"></label>
        <b-button @click="clickIdFind" class="button2" variant="primary">ID 찾기</b-button>
        <b-button @click="clickPwFind" class="ms-5 button2" variant="primary">비밀번호 찾기</b-button>
      </div>
    </div>

    
  </div>
</template>

<script>
import axios from 'axios'



export default {
  name: "LogHome",
  data: function() {
    return {
      Username : '',
      Password : '',
    }
  },
  methods: {
    clickLogin: function() {
      let credentials = {
        id: this.Username,
        password: this.Password,
      }
      axios.post(
        this.$store.state.http_url + this.$store.state.url_user + "login",
        JSON.stringify(credentials),
        {
          headers: {
            "Content-Type": `application/json`,
          },
        }
      )
      .then((res) => {
        localStorage.setItem('jwt', res.data.access_token)
        // console.log(res.data.access_token)
        let datas = {
          id: res.data.id,
          nickname: res.data.nickname,
          is_admin: res.data.is_admin
        }
        this.$store.dispatch("My", datas)
        this.$router.push({name: 'RoomHome'})
      })
      .catch(() => {
        alert("아이디 혹은 비밀번호를 확인해주세요.")
      })
    },
    clickSighup: function() {
      this.$router.push({name: 'SignUp'})
    },

    clickIdFind: function() {
      this.$router.push({name: 'IdFind'})
    },

    clickPwFind: function() {
      this.$router.push({name: 'PwFind'})
    },
  },
  computed: {

  },
}
</script>

<style>
.back1 {
  min-width: 800px;
  height: 936px;
  background-image: url("../../assets/images/bgimg1.jpg");
  background-size: cover;
}

.login-window {
  width: 600px;
}

.title {
  margin-bottom: 100px;
  font-weight: bold;
  font-size: 80px;
}

.text1 {
  font-weight: bold;
}

.button1 {
  width: 400px;
}

.button2 {
  width: 175px;
}
</style>