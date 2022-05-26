<template>
  <div class="mx-auto">
    <div>
      <div style="width:60%; display:inline-block;">
        <b-form-input list="input-list" 
        id="input-with-list" 
        v-model="searchtitle" 
        @keyup.enter="searchMusic"
        ></b-form-input>
        <b-form-datalist id="input-list" :options="options"></b-form-datalist>
      </div>
      
      <div class="ms-3" style="display:inline-block;">
        <b-button type="btn" class="btnapp my-auto" style="width: 100px;" @click="searchMusic">검색</b-button>
      </div>
        
    </div>
  </div>
</template>

<script>
import axios from 'axios'


export default {
  name: 'SearchMovieForm',
  data: function() {
    return {
      searchtitle : null,
      // ischeck: false,
      options: ['1', '2'],
    }
  },
  methods: {
    searchMusic: function() {
      const token = localStorage.getItem('jwt')
      axios.post(
        this.$store.state.http_url + this.$store.state.url_sing + "search/" + this.searchtitle,
        JSON.stringify(""),
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(res =>{
        this.$store.dispatch('Set_SearchMusic', res.data.list)
      })
      .catch(() => {
        
      })

    },
  },
  computed: {
    // options: function(){
    //   return this.$store.state.search_music.map(obj => {
    //     return obj.title
    //   })
    // },
    
  },
  
  
}
</script>

<style>
.btnapp {
  position:relative;
  left:40%;
  transform: translateX(-50%);
  margin-bottom: 40px;
  width:80%;
  height:40px;
  background: linear-gradient(125deg,#d46167,#bb213b,#a85567);
  background-position: left;
  background-size: 200%;
  color:white;
  font-weight: bold;
  border:none;
  cursor:pointer;
  transition: 0.4s;
  display:inline;
}

.btn:hover {
  background-position: right;
}
</style>