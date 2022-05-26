<template>
  <div v-if="isDelete" class="admin-user mx-auto" @click="showDetail">
    <div style="background: #EEEEEE; border-bottom: 1px solid;">{{ user.id }}</div>

    <div v-if="isDetail">

      <div class="admin-user-div mt-3">
        <label for="" class="signuplabel">별명: </label>
        <span v-if="isEditNickname">
          <div class="admin-user-nick">{{ user.nickname }}</div>
          <b-button @click="showEditNickname" class="mx-3 admin-btn">수정하기</b-button>
        </span>
        <span v-else>
          <input class="admin-user-nick" v-model="user.nickname">
          <b-button @click="editNickname" class="mx-3 admin-btn">수정 완료</b-button>
        </span>
        
      </div>

      <div class="admin-user-div admin-user-button my-2">
        <b-button @click="initPw" class="admin-btn">비밀번호 초기화</b-button>
        <b-button @click="banUser" class="mx-3 admin-btn">밴</b-button>
        <b-button @click="deleteUser" class="admin-btn">유저삭제</b-button>
      </div>

    </div>
  </div>  
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminUser',
  props: {
    user: {
      type: Object,
    }
  },
  data: function() {
    return {
      isDetail: false,
      isEditNickname: true,
      isDelete: true
    }
  },
  methods: {
    showDetail: function() {
      this.isDetail = !this.isDetail
    },
    showEditNickname: function() {
      this.isEditNickname = !this.isEditNickname
    },
    banUser: function() {
      let http_url = this.$store.state.http_url
      let url_banuser = this.$store.state.url_banuser

      const token = localStorage.getItem('jwt')

      // console.log("유저 밴 요청")
      axios.put(
        `${http_url}${url_banuser}`,        
        {
          id: this.user.id,
          isBlocked: 1 - this.user.is_blocked
        },
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          }
        }
      )
      .then(() => {
        this.user.is_blocked = 1 - this.user.is_blocked
        alert("변경완료.")
      })
    },
    editNickname: function() {
      let http_url = this.$store.state.http_url
      let url_admin_edit_user = this.$store.state.url_admin_edit_user

      const token = localStorage.getItem('jwt')

      axios.put(
        `${http_url}${url_admin_edit_user}`,
        {
          id: this.user.id,
          nickname: this.user.nickname
        },
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          }
        }
      )
      .then(() => {
        alert("변경완료.")
        this.isEditNickname = !this.isEditNickname
      })
    },
    initPw: function() {
      let http_url = this.$store.state.http_url
      let url_admin_pw = this.$store.state.url_admin_pw

      const token = localStorage.getItem('jwt')

      axios.put(
        `${http_url}${url_admin_pw}`,
        {
          id: this.user.id,
        },
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          }
        }
      )
      .then(() => {

      })
    },
    deleteUser: function() {
      let http_url = this.$store.state.http_url
      let url_admin_delete_user = this.$store.state.url_admin_delete_user
      const token = localStorage.getItem('jwt')

      const form = {
        id: this.user.id,
      }

      axios.post(
        `${http_url}${url_admin_delete_user}`,
        JSON.stringify(form),
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
.admin-user {
  width: 500px;
}

.admin-user-div {
  width: 500px;
  height: 30px;
  text-align: left;
}

.admin-user-button {
  text-align: center;
}

.admin-user-nick {
  display: inline-block;
  text-align: center;
  width: 200px;
}

.admin-btn {
  padding: 1px 4px !important;

}
</style>
