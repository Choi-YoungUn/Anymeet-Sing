<template>
  <div style="margin-top:70px;">
    <h1>회원정보수정</h1>
    <hr class="my-4">

    <div class="row login-window mx-auto">
      <div class="signupdiv">
        <label for="" class="signuplabel text1">ID</label>
        <span class="text1">{{ id }}</span>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">전화번호</label>
        <input v-model="form.phone" class="signupinput">
        <p v-if="phoneValid" class="text-danger">다시 한번 확인해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">별명</label>
        <input v-model="form.nickname" class="signupinput">
      <p v-if="nicknameValid" class="text-danger">별명을 입력해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">ID찾기 질문</label>
        <b-form-select
          v-model="form.questionsId"
          :options="questions"
           class="signupinput"
        ></b-form-select>
        <p v-if="questionValid" class="text-danger">질문을 선택해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">답변</label>
        <input v-model="form.answer" class="signupinput">
      <p v-if="answerValid" class="text-danger">답변을 입력해주세요.</p>
      </div>

      <div class="signupdiv">
        <b-button @click="submitForm" variant="primary" class="profile-text signup-button1">완료</b-button>
      </div>

      <div class="signupdiv">
        <b-button @click="changePw" variant="primary" class="profile-text signup-button1">비밀번호 변경</b-button>
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
  name: "EditMyInfo",
  data: function() {
    return {
      id: '',
      form: {
        nickname: '',
        phone: '',
        questionsId: 0,
        answer: '',
      },
      questions: [{ text: '선택', value: null }, 'Carrots', 'Beans', 'Tomatoes', 'Corn'],
      phoneValid: false,
      nicknameValid: false,
      questionValid: false,
      answerValid: false,
    }
  },
  methods: {
    cancel: function () {
      this.$router.go(-1)
    },
    changePw: function () {
      this.$router.push({name: 'ChangePw'})
    },
    submitForm: function () {
      let valid = true

      // console.log("회원정보 수정 요청")
      // 전화번호 유효성 검사
      let re2 = /^[0-9+]{10,11}$/
      if(re2.test(this.form.phone)) {
        this.phoneValid = false
      }
      else {
        this.phoneValid = true
        valid = false
      }

      // 별명 유효성 검사
      if(this.form.nickname==='') {
        this.nicknameValid = true
        valid = false
      }
      else {
        this.nicknameValid = false
      }

      // 질문선택 유효성 검사
      if(this.questionsId === null) {
        this.questionValid = true
        valid = false
      }
      else {
        this.questionValid = false
      }

      // 답변 유효성 검사
      if(this.form.answer==='') {
        valid = false
        this.answerValid = true
      }
      else {
        this.answerValid = false
      }

      // 모두 통과한다면 회원정보수정 요청 보내기
      if(valid) {
        let http_url = this.$store.state.http_url
        let url_useredit = this.$store.state.url_useredit

        const token = localStorage.getItem('jwt')
        // console.log("유효성 검사 통과")
        
        axios.put(
          `${http_url}${url_useredit}`,
          JSON.stringify(this.form),
          {
            headers: {
              "Content-Type": `application/json`,
              "access-token":  token,
            },
          }
        )
        .then(() => {
          this.$store.dispatch("My", {nickname: this.form.nickname})
          alert('수정 완료.')

          this.$router.go(-1)
        })
      }
    },
  },
  created() {
    // 유저 정보 불러오기
    this.id = this.$store.state.myprofile.id
    this.form.nickname = this.$store.state.myprofile.nickname
    this.form.phone = this.$store.state.myprofile.phone
    this.form.questionsId = this.$store.state.myprofile.questionsId
    this.form.answer = this.$store.state.myprofile.answer


    // 질문 목록 가져오기
    let http_url = this.$store.state.http_url
    let url_question = this.$store.state.url_question
    axios({
      method: "get",
      url: `${http_url}${url_question}`,
    })
    .then((res) => {
      const res_questions = res.data.questions
      this.questions = []

      for(let i = 0; i < res_questions.length; i++) {
        this.questions.push({ text: res_questions[i], value: i + 1})
      }
    })
  },
}
</script>

<style>

</style>