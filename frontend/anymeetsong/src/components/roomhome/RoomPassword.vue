<template>
  <div id="RoomPassword">
    <div >
      <h1>{{room.title}}</h1>
      <div class="m-3 row">
        <label for="staticEmail" class="col-sm-2 col-form-label">비밀번호</label>
        <div class="col-sm-10">
          <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
            <input type="password" class="form-control" placeholder="Password" aria-label="Password" @keyup.enter="checkPassword" v-model="Password">
            <button class="btn btn-primary" type="button"  @click="checkPassword">확인</button>
          </div>
        </div>
      </div>
    </div>
    <button @click="clickBack">뒤로가기</button>
    
    

  </div>
</template>

<script>
import axios from 'axios'


export default {
  name:"RoomPassword",
  data: function(){
    return {
      Password: "",
      check: true,
    }
  },
  computed: {
    room: function() {
      return this.$store.state.enterroom
    },
    
  },
  methods: {
    checkPassword: function() {
      //여기서 비밀번호를 확인해주자 제발.
      let room_pw = this.$store.state.enterroom.password
      
      if (this.Password == room_pw){
        this.check = true
        
        const token = localStorage.getItem('jwt')
        let room_id = String(this.$store.state.enterroom.id)

        let dump = " "
        axios.post(
          this.$store.state.http_url+ this.$store.state.url_room + "join/" + room_id ,
          JSON.stringify(dump),
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token": token,
            },
          }
        )
        .then(() => {
          
          this.$store.dispatch('Set_Password', this.Password)
          this.$router.push({name: 'MainRoom', params: {roomid : room_id}})
        })
        .catch(() => {
          this.$router.push({name: 'RoomHome'})
          alert("방 접속에 실패하였습니다\n로그아웃 후 다시 접속해주세요")
        })


      } else {
        this.check = false
        if (this.Password) {
          alert("비밀번호를 확인해주세요")
        }
      }
    },

    clickBack: function(){
      this.$router.push({name: 'RoomHome'})
    }

  },
  created: function() {
    this.checkPassword()
  }
}
</script>

<style>

</style>