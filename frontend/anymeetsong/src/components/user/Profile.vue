<template>
  <div style="margin-top:70px;">
    <h1>{{ this.myInfo.nickname }}님의 회원정보</h1>
    <hr class="my-4">
    <div class="row login-window mx-auto profile-back">
      <div class="profile-div">
        <label for="" class="profile-label text1">ID:</label>
        <span class="text1">{{ myInfo.id }}</span>
      </div>

      <div class="profile-div">
        <label for="" class="profile-label text1">별명:</label>
        <span class="text1">{{ myInfo.nickname }}</span>
      </div>

      <div class="profile-div">
        <label for="" class="profile-label text1">phone:</label>
        <span class="text1">{{ myInfo.phone }}</span>
      </div>

      <div class="profile-div mt-4">
        <span class="profile-text2 text1">다른 사람들 에게 {{ myInfo.singing_count }}개의 노래를 불렀습니다.</span>
      </div>

      <div class="profile-div mb-4">
        <span class="profile-text2 text1">다른 사람의 노래를 {{ myInfo.listen_count }}개 들었습니다..</span>
      </div>

    </div>

    <div class="mx-auto" style="width:500px;">
      <div class="profile-div">
        <b-button @click="editMyInfo" variant="primary" class="profile-text2 profile-button">회원정보 수정</b-button>
      </div>

      <div class="profile-div">
        <b-button @click="cancel" variant="danger" class="profile-text2 profile-button">뒤로가기</b-button>
      </div>

      <div class="profile-div">
        <b-button @click="deleteMyId" variant="danger" class="profile-text2 profile-button">회원 탈퇴</b-button>
      </div>
    </div>


  </div>  
</template>

<script>
import axios from 'axios'


export default {
  name: "Profile",
  data: function() {
    return {
      
    }
  },
  methods: {
    cancel: function () {
      this.$router.go(-1)
    },
    editMyInfo: function () {
      this.$router.push({name: 'EditMyInfo'})
    },
    deleteMyId: function () {
      const token = localStorage.getItem('jwt')
      axios.delete(
        this.$store.state.http_url+ this.$store.state.url_user + "delete",
        {
          headers: {
            "access-token": token,
          },
        }
      )
      .then(() => {
        alert("탈퇴 성공")
        localStorage.removeItem('jwt') 
        //노래방 리스트 페이지로 이동용
        this.$router.push({name: 'LogHome'})
      })
      .catch(() => {
        alert("아이디 혹은 비밀번호를 확인해주세요.")
      })
    }
  },

  computed: {
    myInfo: function() {
      return this.$store.state.myprofile
    }
  },

  created() {
    this.$store.dispatch("Get_MyProfile")
  }
}
</script>

<style>
  .back3 {
    min-width: 800px;
    height: 936px;
    background-image: url("../../assets/images/bgimg3.jpg");
    background-size: cover;
  }

  .profile-label {
    width: 100px !important;
    text-align: right;
    margin-right: 10px;
  }

  .profile-div {
    width: 500px;
    height: 65px !important;
    text-align: left;
  }

  .profile-back {
    width: 500px;
    background: rgba(255, 255, 255, 0.2);
    padding-top: 50px;
    text-align: left;
    margin: 20px;
    /* backdrop-filter: blur(3px); */
    border-radius: 10px;
  }

  .profile-text {
    margin-left: 140px;
  }

  .profile-text2 {
    margin-left: 110px;
  }

  .profile-button {
    width: 150px;
  }
</style>
