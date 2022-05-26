<template>
  <div id="CreateRoom" style="margin-top:70px;">
    <div>
      <h1>노래방 생성</h1>
    </div>

    <hr class="my-4">

    <div class="login-window mx-auto">
      <div class="signupdiv">
        <label class="signuplabel text1">공개 여부</label>
        <div v-if="selected" style="display:inline-block;">
          <div @click="changeToPublicRoom" style="display:inline-block;">
            <b-button variant="primary" disabled class="create-room-button" style="margin-right: 20px;">공개</b-button>
          </div>
          <div style="display:inline-block;">
            <b-button variant="danger" @click="changeToSecretRoom" class="create-room-button">비공개</b-button>
          </div>
        </div>
        <div v-else style="display:inline-block;">
          <div style="display:inline-block;">
            <b-button variant="primary" @click="changeToPublicRoom" class="create-room-button" style="margin-right: 20px;">공개</b-button>
          </div>
          <div @click="changeToSecretRoom" style="display:inline-block;">
            <b-button variant="danger" disabled class="create-room-button">비공개</b-button>
          </div>
        </div>
      </div>  

      <div class="signupdiv">
        <label for="staticEmail" class="signuplabel text1">방 제목</label>
        <input type="text" class="signupinput" placeholder="title" aria-label="title" v-model="title">
      </div>

      <div class="signupdiv">
        <label for="staticEmail" class="signuplabel text1" style="margin-top: 7px;">비밀번호</label>
        <div v-if="selected" style="display:inline-block;">
          <input type="text" class="signupinput" placeholder="password" aria-label="password" v-model="password">
        </div>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel"></label>
        <b-button class="signup-button1" variant="primary" type="submit" @click="create_Room">완료</b-button>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel"></label>
        <b-button class="signup-button1" @click="cancel" variant="danger">뒤로가기</b-button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name:"CreateRoom",
  
  data: function() {
    return {
      title: "",
      password: "",
      selected: false
    }
  },

  methods: {
    create_Room: function() {
      if (this.title){
        let credentials = { 
          title: this.title,
          password: this.password
        }
        const token = localStorage.getItem('jwt')
        axios.post(
          this.$store.state.http_url + this.$store.state.url_room + "create",
          JSON.stringify(credentials),
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token": token,
            },
          }
        )
        .then(res =>{
          this.$store.dispatch("Set_Password", credentials.password)
          // console.log("방생성 완료", res.data.id)
          axios.get(
            this.$store.state.http_url + this.$store.state.url_room + "listRoom/"+ res.data.id,
            {
              headers: {
                "Content-Type": `application/json`,
                "access-token": token,
              },
            }
          )
          .then((response)=> {
            // console.log("방 정보획득 완료", response.data)
            this.$store.dispatch("Enter_Room", response.data)
            let dump = " "
            axios.post(
              this.$store.state.http_url+ this.$store.state.url_room + "join/" + response.data.id ,
              JSON.stringify(dump),
              {
                headers: {
                  "Content-Type": `application/json`,
                  "access-token": token,
                },
              }
            )
            .then((res) => {
              this.$router.push({name: 'MainRoom', params: {roomid : res.data}})
            })
            .catch(() => {
              alert("방 접속에 실패하였습니다")
            })
          })
        })
        .catch(() => {
        })

      }
      
    },
    cancel: function () {
      this.$router.go(-1)
    },
    changeToSecretRoom: function () {
      this.selected = true
    },
    changeToPublicRoom: function () {
      this.selected = false
    },
  }

}
</script>

<style>
.create-room-button {
  width: 140px;
}

.back4 {
  min-width: 1000px;
  height: 936px;
  background-image: url("../../assets/images/bgimg4.jpg");
  background-size: cover;
}

</style>