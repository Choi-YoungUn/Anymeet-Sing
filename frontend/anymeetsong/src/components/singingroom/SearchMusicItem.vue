<template>
  <div id="SearchMusicItem" @click="sel_music">
    <img 
      :src="music.thumbnail" 
      alt="youtube thumbnail"
      style="width:250px;"
    >
    <label for="SearchMusicItem" style="color:white;">{{music.title}}</label>
  </div>
</template>

<script>
import axios from 'axios'


export default {
  name: "SearchMusicItem",
  props: {
    // 상위에서 썸네일 데이터를 받는다.
    music: {
      type: Object,
    }
  },

  methods: {
    sel_music: function() {
      const token = localStorage.getItem('jwt')
      let room_id = this.$store.state.enterroom.id
      let credentials = {
        
        videoId: this.music.videoId,
        title: this.music.title,
        thumbnail: this.music.thumbnail,
        id : this.$store.state.myid,
        nickname: this.$store.state.mynickname
      }
      // console.log("데이터 확인",credentials)
      // console.log("url 확인",this.$store.state.http_url+ this.$store.state.url_sing + "list/" + room_id)
      axios.post(
        this.$store.state.http_url+ this.$store.state.url_sing + "list/" + room_id ,
        JSON.stringify(credentials),
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(() => {
        // console.log(res.data)
        this.$store.dispatch('Set_SearchMusic', null)
        this.$store.dispatch('Set_MusicList', this.$store.state.enterroom.id)
        alert("노래 추가 성공!")
      })
      .catch(() => {
        alert("노래를 추가하지 못하였습니다.")
      })
    }
  },

  created: function() {
    
  }
  
}
</script>

<style>

</style>