<template>
  <div id="IdFind" style="padding-top: 100px;">
    <h1 class="title mb-5">Any Meet & Sing</h1>
    <h2 class="mb-5">ID 찾기</h2>

    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label fw-bold">전화번호</label>
      <div class="col-sm-10">
        <div class="input-group mb-3 " style="min-width: 400px; width: 80%; margin-left: auto; margin-right: auto;">
          <input type="text" class="form-control" placeholder="000" aria-label="first_Num" v-model="first_Num">
          <span></span>
          <input type="text" class="form-control" placeholder="0000" aria-label="mid_Num" v-model="mid_Num">
          <span></span>
          <input type="text" class="form-control" placeholder="0000" aria-label="last_Num" v-model="last_Num">
        </div>
      </div>
    </div>

    <div class="row login-window mx-auto">
      <label for="staticEmail" class="col-sm-2 col-form-label fw-bold">질문 선택</label>
      <div class="col-sm-10">
        <b-form-select v-model="question" :options="question_list" class="mb-3 inputSelect" style="min-width:400px; width: 80%;">
          <template #first>
            <b-form-select-option :value="null" disabled class="p-2">질문을 선택해 주세요</b-form-select-option>
          </template>
        </b-form-select>
        <b-form-input class="mx-auto" v-model="answer" placeholder="답변을 입력해주세요" style="min-width:400px; width: 80%;"></b-form-input>
      </div>
    </div>
    <div class="login-window mx-auto my-5">
      <label for="" class="col-sm-2 col-form-label"></label>
      <b-button class="button3" @click="clickIdFind" variant="primary">찾기</b-button>
      <b-button class="ms-5 button4" @click="clickBack" variant="danger">뒤로가기</b-button>
    </div>
    
  </div>
</template>

<script>
import axios from 'axios'


export default {
  name:"IdFind",
  data: function() {
    return {
      first_Num : null,
      mid_Num : null,
      last_Num : null,
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

    clickIdFind: function() {
      let phone_num = this.first_Num + this.mid_Num + this.last_Num
      
      if (this.first_Num == 0 || this.mid_Num == 0 || this.last_Num == 0){
        alert("전화번호 양식을 확인해주세요")
        return
      }

      //전화번호 양식 확인
      for (let i in phone_num) {
        
        let str = Number(phone_num[i])
        if ( 0 <= str && str <= 9 ){
          continue;
        } else {
          alert("전화번호 양식을 확인해주세요")
          this.first_Num = null
          this.mid_Num = null
          this.last_Num = null
          return
        }
      }
      let questions = this.question_list.indexOf(this.question)
      console.log(questions)
      //질문 답변 확인
      if (this.question && this.answer) {
        
        console.log(this.$store.state.http_url + this.$store.state.url_user + phone_num + '/' + questions + '/' + this.answer + '/')
        axios({
        method: 'get',
        url: this.$store.state.http_url + this.$store.state.url_user + phone_num + '/' + questions + '/' + this.answer + '/',
        })
        .then((res) => {
          // console.log(res.data.id)
          alert("회원님의 아이디는 : " + res.data.id + " 입니다.")
          // this.$router.push({name: 'LogHome'})
        })
        .catch(() => {
          alert("아이디를 찾을 수 없습니다." )
        })
    
      } else {
        alert("질문 선택과 답변을 입력해주세요")
        return
      }
    }

  },
  created: function () {
    axios.get(
      this.$store.state.http_url+ this.$store.state.url_user + "questions",
    )
    .then((res) => {
      // console.log(res.data.questions)
      this.question_list = res.data.questions
    })
    .catch(() => {
      // console.log(err)
    })
  }

}
</script>

<style>

  .inputSelect {
    display: inline-block;
    width: 400px;
    height: 38px;
    border: 1px solid #ced4da;
    border-radius: 0.25rem;
  }

</style>