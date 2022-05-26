<template>
  <div>
    <h1 class="title2" style="cursor:default;">Any Meet & Sing</h1>
    <h1 style="cursor:default;">회원가입</h1>
    <hr>
    <div class="login-window mx-auto">
      <div class="signupdiv">
        <label for="" class="signuplabel text1">ID</label>
        <span v-if="idAuthenticationCodeValid2" class="signupinput2">{{ form.id }}</span>
        <input class="signupinput" v-else v-model="form.id">
        <b-button v-if="idAuthenticationCodeValid2" variant="primary" class="codebtn" @click="idRewrite">아이디 변경</b-button>
        <b-button v-else @click="sendCode" variant="primary" class="codebtn">인증번호 발송</b-button>
        <p v-if="idValid" class="text-danger error-text">이메일을 입력해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">인증코드</label>
        <span v-if="idAuthenticationCodeValid2">
          <span>인증이 완료되었습니다.</span>
        </span>
        <span v-else>
          <input class="signupinput" v-model="idAuthenticationCode">
          <b-button @click="checkCode" variant="primary" class="codebtn">인증</b-button>
          <p v-if="idAuthenticationCodeValid" class="text-danger error-text">인증코드가 일치 하지 않습니다.</p>
        </span>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">PW</label>
        <input class="signupinput" v-model="form.password" type="password">
        <p v-if="pwValid" class="text-danger error-text">8~16글자이며, 영어, 숫자, 특수문자를 포함해야합니다.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">PW Conf.</label>
        <input class="signupinput" v-model="passwordConfirm" type="password">
        <p v-if="pwConfirmValid" class="text-danger error-text">비밀번호와 일치하지 않습니다.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">전화번호</label>
        <input class="signupinput" v-model="form.phone">
        <p v-if="phoneValid" class="text-danger error-text">다시 한번 확인해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">별명</label>
        <input class="signupinput" v-model="form.nickname">
      <p v-if="nicknameValid" class="text-danger error-text">별명을 입력해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">ID찾기 질문</label>
        <b-form-select
          v-model="form.questionsId"
          :options="questions"
          class="signupinput"
        ></b-form-select>
        <p v-if="questionValid" class="text-danger error-text">질문을 선택해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel text1">답변</label>
        <input class="signupinput" v-model="form.answer">
      <p v-if="answerValid" class="text-danger error-text">답변을 입력해주세요.</p>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel"></label>
        <b-button @click="submitForm" variant="primary" class="signup-button1">완료</b-button>
      </div>

      <div class="signupdiv">
        <label for="" class="signuplabel"></label>
        <b-button @click="cancel" variant="danger" class="signup-button1">뒤로가기</b-button>
      </div>
    </div>
  </div>
</template>

<script>
  import axios from 'axios'

  export default {
    name: "SignUp",
    data: function() {
      return {
        form: {
          id: '',
          password: '',
          nickname: '',
          phone: '',
          questionsId: 0,
          answer: '',
        },
        questions: [{ text: '선택', value: null }, 'Carrots', 'Beans', 'Tomatoes', 'Corn'],
        idAuthenticationCode: '',
        idAuthenticationCodeConfirm: '',
        idAuthenticationCodeValid: false,
        idAuthenticationCodeValid2: false,
        passwordConfirm: '',
        pwConfirmValid: false,
        phoneValid: false,
        nicknameValid: false,
        questionValid: false,
        answerValid: false,
        idValid: false,
        pwValid: false,
      }
    },
    methods: {
      submitForm: function () {
        let valid = true

        let re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i
        if(re.test(this.form.id)) {
          this.idValid = false
        }
        else {
          this.idValid = true
          valid = false
        }

        // 이메일 인증 통과 여부 검사
        if(this.idAuthenticationCodeValid2 === false) {
          valid = false
          this.idAuthenticationCodeValid = true
        }
        else {
          this.idAuthenticationCodeValid = false
        }

        // 비밀번호 유효성 검사
        const pw = this.form.password
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
        if(this.form.password != this.passwordConfirm) {
          this.pwConfirmValid = true
          valid = false
        }
        else {
          this.pwConfirmValid = false
        }

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
        if(this.form.questionsId === 0) {
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

        // 모두 통과한다면 회원가입 요청 보내기
        if(valid) {
          let http_url = this.$store.state.http_url
          let url_signup = this.$store.state.url_signup

          

          axios.post(
            `${http_url}${url_signup}`,
            JSON.stringify(this.form),
            {
              headers: {
                "Content-Type": `application/json`,
              },
            }
          )
          .then(() => {
            this.$router.push({name:'LogHome'})
          })

        }
        else {
          console.log("올바르지 않습니다.")
        }
      },
      sendCode: function () {
        let http_url = this.$store.state.http_url
        let url_sendcode = this.$store.state.url_sendcode
        axios.post(
          `${http_url}${url_sendcode}`,
          {"id": this.form.id},
          {contentType: "application/json"},
        )
        .then(() => {
          alert("인증 메일을 전송하였습니다.")
          console.log('sendCode')
        })
      },
      checkCode: function () {
        let http_url = this.$store.state.http_url
        let url_checkcode = this.$store.state.url_checkcode
        let email = this.form.id
        let code = this.idAuthenticationCode
        axios({
          method: "get",
          url: `${http_url}${url_checkcode}/${email}/${code}`,
        })
        .then((res) => {
          this.idAuthenticationCodeValid2 = res.data.checkCode
          if(this.idAuthenticationCodeValid2) {
            this.idAuthenticationCodeValid = false
          }
          else {
            this.idAuthenticationCodeValid = true
          }
        })
        .catch(() => {
          this.idAuthenticationCodeValid = true
        })
        
      },
      idRewrite: function () {
        this.idAuthenticationCodeValid2 = false
        this.idAuthenticationCode = ''
      },
      cancel: function () {
        this.$router.push({name:'LogHome'})
      }
    },
    created() {
      let http_url = this.$store.state.http_url
      let url_question = this.$store.state.url_question
      axios({
        method: "get",
        url: `${http_url}${url_question}`,
      })
      .then((res) => {
        const res_questions = res.data.questions
        this.questions = [{ text: '선택', value: null }]

        for(let i = 0; i < res_questions.length; i++) {
          this.questions.push({ text: res_questions[i], value: i + 1})
        }
      })
    },

  }
</script>

<style>
  .signupdiv {
    width: 600px;
    height: 65px !important;
    text-align: left;
  }

  .signuplabel {
    width: 130px !important;
    text-align: right;
    margin-right: 10px;
  }

  .signupinput {
    display: inline-block;
    width: 300px;
    height: 38px;
    border: 1px solid #ced4da;
    border-radius: 0.25rem;
    padding-left: 7px;
  }

  .signupinput2 {
    display: inline-block;
    width: 300px;
    height: 38px;
  }

  .codebtn {
    margin-left: 10px;
  }

  .title2 {
    margin-bottom: 50px;
    font-weight: bold;
    font-size: 80px;
  }

  .back1-1 {
    min-width: 800px;
    height: 936px;
    background-image: url("../../assets/images/bgimg1.jpg");
    background-size: cover;
    padding-top: 50px;
  }

  .signup-button1 {
    width: 300px
  }

  .error-text {
    margin-left: 110px;
  }
</style>
