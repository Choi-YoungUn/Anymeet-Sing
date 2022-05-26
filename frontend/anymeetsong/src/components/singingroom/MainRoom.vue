<template>
  
  <div id="MainRoom">
    <b-navbar toggleable="lg" type="dark" class="px-3 myNavbar navbar-setting" style="min-width: 800px;">
      <b-navbar-brand style="font-size:25px; color:white; cursor: default;">Any Meet & Sing</b-navbar-brand>
      <b-navbar-brand><img src="../../assets/images/logo.png" alt="" class="mx-auto" style="width: 150px;"></b-navbar-brand>

      <div id="nav-collapse" is-nav>
        
        <b-navbar-nav class="navbar-menu">
          <b-nav-item-dropdown right style="width: 200px;">
            <template #button-content>
              <em class="text-white fs-4">Menu</em>
            </template>
            
            <b-dropdown-item v-if="isHost == true" class="ms-auto" @click="showModal">
              <!-- <input type="button" id="show-btn" value="방 정보 수정하기" @click="showModal"> -->
              방 정보 수정하기
            </b-dropdown-item>
            <b-dropdown-item v-if="isHost" @click="clickDeleteRoom">
              <!-- <input type="button"  value="방 삭제"> -->
              방 삭제
            </b-dropdown-item>
            <b-dropdown-item v-else @click="clickOutRoom">
              <!-- <input type="button"  value="방 퇴장"> -->
              방 퇴장
            </b-dropdown-item>
            <b-dropdown-item @click="copyUrl">
              <input type="hidden" id="urlInput" class="url-input"/>
              <!-- <input type="button" @click="copyUrl" class="url-copy-btn" value="링크 복사하기"> -->
              링크 복사하기
            </b-dropdown-item>
          </b-nav-item-dropdown>
        </b-navbar-nav>
      </div>
      
      <!-- <b-dropdown-item class="ms-auto">
        
      </b-dropdown-item> -->
    </b-navbar>

    <h1 style="margin:20px;">{{title}}</h1>

    <div class="m-3" style="width: 95%; ">
      
      <UserCam>
      </UserCam>

    </div>   

    <b-modal ref="my-modal" hide-footer title="방 정보 수정">
      <div class="d-block text-center">
        <div class="m-3 row">
          <label for="staticEmail" class="col-sm-2 col-form-label">제목</label>
          <div class="col-sm-10">
              <input type="text" class="form-control" placeholder= enterroom.title aria-label="title" v-model="title">
          </div>
        </div>
        <div class="m-3 row">
          <label for="staticEmail" class="col-sm-2 col-form-label">비밀번호</label>
          <div class="col-sm-10">
              <input type="text" class="form-control" placeholder="" aria-label="title" v-model="password">
          </div>
        </div>

      </div>
      <b-button class="mt-3" variant="outline-primary" block @click="hideModal">수정하기</b-button>
    </b-modal>
    
    
    
      
      
    
  </div>
</template>

<script>
import UserCam from '@/components/singingroom/UserCam'
import axios from 'axios'


export default {
  name: "MainRoom",
  data: function(){
    return {
      title : "",
      password : "",
    }
  },
  components:{
    UserCam
  },
  methods: {
    clickDeleteRoom: function(){
      this.clickout = true
      const token = localStorage.getItem('jwt')
      let room_id = String(this.enterroom.id)
      let myid = String(this.enterroom.host_id)
      axios.delete(
        this.$store.state.http_url + this.$store.state.url_room + "delete/" + room_id + "/" + myid,
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(() =>{
        // console.log("정상 방 삭제",res)
          //데이터를 저장해주자
        let reset_room = {
          id : -1,
          title : "",
          person_count: null,
          host_id : null,
          password : ""
        }
        
        this.$store.dispatch('Enter_Room', reset_room)
        this.$store.dispatch('Set_Password', "")
        this.$router.push({name: 'RoomHome'})
      })
      .catch(() => {
        // console.log(err)
      })
    },

    clickOutRoom: function(){
      this.clickout = true
      let room_id = String(this.enterroom.id)

      const token = localStorage.getItem('jwt')
        axios.delete(
          this.$store.state.http_url + this.$store.state.url_room + "leave/" + room_id,
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token": token,
            },
          }
        )
        .then(() =>{
          
          let reset_room = {
            id : -1,
            title : "",
            person_count: null,
            host_id : null,
            password : ""
          }
          
          this.$store.dispatch('Enter_Room', reset_room)
          this.$store.dispatch('Set_Password', "")
          this.$router.push({name: 'RoomHome'})
        })
        .catch(() => {
          
        })
    },

    

    showModal() {
      this.$refs['my-modal'].show()
    },
    hideModal() {
      const token = localStorage.getItem('jwt')
      console.log(token)
      
      let credentials = {
        id : this.enterroom.id,
        title: this.title,
        password : this.password
      }
      axios.put(
        this.$store.state.http_url+ this.$store.state.url_room + "update",
        JSON.stringify(credentials),
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(() => {
        this.$refs['my-modal'].hide()

        axios.get(
          this.$store.state.http_url + this.$store.state.url_room + "listRoom/"+ this.enterroom.id,
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token": token,
            },
          }
        )
        .then((res)=> {

          this.$store.dispatch("Enter_Room", res.data)
        })
        .catch(() => {
          alert("방 데이터를 가져오지 못했습니다.")
        })
      })
      .catch(() => {
        alert("수정에 실패하였습니다")
      })

    },

    copyUrl: function(){
      let now_url = window.location.href
      
      
      let t = document.createElement("textarea");
      document.body.appendChild(t);
      t.value = now_url;
      t.select();
      document.execCommand('copy');
      document.body.removeChild(t);

      alert('복사가 완료되었습니다');
    },

  },
  created: function(){
    this.title = this.enterroom.title
    this.$store.dispatch('Set_MusicList', this.enterroom.id)
  },

  computed:  {
    enterroom: function(){
      return this.$store.state.enterroom
    },
    checkpassword: function(){
      return this.$store.state.password
    },
    isHost: function() {
      if (this.$store.state.myid == this.$store.state.enterroom.host_id){
        return true
      } else {
        return false
      }
    },
    mynickname: function() {
      return this.$store.state.mynickname
    },

  },

  beforeDestroy: function(){
    this.$store.dispatch('Set_SearchMusic', null)
  },
  
}
</script>

<style>
.scroll {
  overflow-x: scroll;
  overflow-y: hidden;
}

li::marker {
  display: none !important;
}


/* 스크롤바 설정*/ 
.type::-webkit-scrollbar{
   /* 스크롤바 막대 너비 설정 */ 
   width: 2px; 
   } 
   /* 스크롤바 막대 설정*/ 
.type::-webkit-scrollbar-thumb{ 
    /* 스크롤바 막대 높이 설정 */ 
    height: 13%;
    background-color: rgba(255, 255, 255, 0.205);
     /* 스크롤바 둥글게 설정 */ 
     border-radius: 10px; 
     } 
  /* 스크롤바 뒷 배경 설정*/ 
  .type::-webkit-scrollbar-track
  { 
    background-color: rgba(0,0,0,0); 
    display: none;
  }

  .btnapp {
  position:relative;
  left:40%;
  transform: translateX(-50%);
  margin-bottom: 40px;
  width:80%;
  height:40px;
  background: linear-gradient(125deg,#e63f48,#bd0725,#d6353d);
  background-position: left;
  background-size: 200%;
  color:white;
  font-weight: bold;
  border:none;
  cursor:pointer;
  transition: 0.4s;
  display:inline;
}

.btnapp:hover {
  background-position: right;
}
</style>