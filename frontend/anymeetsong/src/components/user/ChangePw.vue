<template>
  <div style="margin-top:70px;">
    <h1>비밀번호 변경</h1>
    <hr class="my-4">

    <div class="row login-window mx-auto">
      <div class="signupdiv">
        <label for="" class="signuplabel text1">현재 비밀번호</label>
        <input v-model="form.password" type="password" class="signupinput">
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">새 비밀번호</label>
        <input v-model="form.changepw" type="password" class="signupinput">
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">새 비밀번호 확인</label>
        <input v-model="changepwConfirm" type="password" class="signupinput" @keyup.enter="submitForm">
      </div>

      <div class="signupdiv">
        <b-button @click="submitForm" variant="primary" class="profile-text signup-button1">완료</b-button>
      </div>

      <div class="signupdiv">
        <b-button @click="cancel" variant="danger" class="profile-text signup-button1">뒤로가기</b-button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: "ChangePw",
  data: function() {
    return {
      form: {
        password: '',
        changepw: '',
      },
      changepwConfirm: '',
      pwValid: false,
      pwConfirmValid: false,
    }
  },
  methods: {
    cancel: function () {
      this.$router.go(-1)
    },
    submitForm: function () {
      let valid = true

      // 비밀번호 유효성 검사
      const pw = this.form.changepw
      const num = pw.search(/[0-9]/g)
      const eng = pw.search(/[a-z]/ig);
      const spe = pw.search(/[`~!@@#$%^&*|₩₩₩'₩";:₩/?]/gi)
      const pwlen = pw.length > 8 && pw.length < 20
      const space = pw.search(/₩s/)
      if(num<0||eng<0||spe<0||pwlen===false||space != -1) {
        this.pwValid = true
        valid = false
      }
      else {
        this.pwValid = false
      }

      // 비밀번호 확인 유효성 검사
      if(this.form.changepw != this.changepwConfirm) {
        this.pwConfirmValid = true
        valid = false
      }
      else {
        this.pwConfirmValid = false
      }


      if(valid) {
        let http_url = this.$store.state.http_url
        let url_changepw = this.$store.state.url_changepw

        const token = localStorage.getItem('jwt')

        axios.put(
          `${http_url}${url_changepw}`,
          JSON.stringify(this.form),
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token":  token,
            },
          }
        )
        .then(() => {
          alert('수정 완료.')
          this.$router.go(-1)
          // localStorage.setItem('jwt', res.data.access_token)
        })
      }
    }
  }
}
</script>

<style>

</style>