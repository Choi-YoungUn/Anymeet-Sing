<template>
  <div id="PwFind" style="padding-top:100px;">
    <h1 class="title mb-5">Any Meet & Sing</h1>
    <h2 class="mb-5">비밀번호 찾기</h2>

    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label fw-bold">ID (Email)</label>
      <div class="col-sm-10">
        <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
          <input type="text" class="form-control" placeholder="Email" aria-label="Email" v-model="Username">
        </div>
      </div>
    </div>

    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label fw-bold">질문 선택</label>
      <div class="col-sm-10">
        <b-form-select v-model="question" :options="question_list" class="mb-3 inputSelect" style="min-width:400px; width: 80%;">
          <template #first>
            <b-form-select-option :value="null" disabled>질문을 선택해 주세요</b-form-select-option>
          </template>
        </b-form-select>
        <b-form-input class="mx-auto" v-model="answer" placeholder="답변을 입력해주세요" style="min-width:400px; width: 80%;"></b-form-input>
      </div>
    </div>
    <div class="login-window mx-auto my-5">
      <label for="" class="col-sm-2 col-form-label"></label>
      <b-button class="button3" @click="clickPasswordFind" variant="primary">찾기</b-button>
      <b-button class="ms-5 button4" @click="clickBack" variant="danger">뒤로가기</b-button>
    </div>
    
  </div>
</template>

<script>
import axios from 'axios'


export default {
  name:"PwFind",
  data: function() {
    return {
      Username : '',
      question: null,
      answer: '',
      question_list: [
        // 질문 리스트 
          { value: 1, text: 'Option A ' },
          { value: 2, text: 'Option B ' }
        ],
    }
  },

  methods: {
    clickBack: function() {
      this.$router.push({name: 'LogHome'})
    },
    clickPasswordFind: function(){


      //질문 답변 확인
      if (this.question && this.answer && this.Username && this.Server) {
        
        let credentials = {
          id: this.Username,
          question: this.question,
          answer: this.answer
        }
        axios.post(
        this.$store.state.http_url+ this.$store.state.url_user + "password",
        JSON.stringify(credentials),
        {
          headers: {
            "Content-Type": `application/json`,
          },
        }
      )
        .then(() => {
          alert("비밀번호를 변경하였습니다. 이메일을 확인해주세요" )
        })
        .catch(() => {
          alert("아이디를 찾을 수 없습니다." )
        })
    
      } else {
        alert("아이디 혹은 질문 선택과 답변을 입력해주세요")
        return
      }
    },
  },
  created: function () {
    axios.get(
      this.$store.state.http_url+ this.$store.state.url_user + "questions",
    )
    .then((res) => {
      this.question_list = res.data.questions
    })
    .catch(() => {
    })
  }
}
</script>

<style>
.button3 {
  width: 200px;
}

.button4 {
  width: 150px;
}
</style>
