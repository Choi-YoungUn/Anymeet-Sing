<template>
  <div id="MusicList">
    <div style="width:60%; color:white; display:inline-block;">{{music.title}}</div>
    <div style="width:20%; color:white; display:inline-block;">{{music.nickname}}</div>
    
    <button class="btn btn-danger" type="submit" style="width:15%;" @click="clickDeleteMusic">삭제</button>
  </div>
</template>

<script>
import axios from 'axios'


export default {
  name: "MusicList",
  props: {
    // 상위에서 썸네일 데이터를 받는다.
    music: {
      type: Object,
    }
  },
  methods: {
    clickDeleteMusic: function(){
      const token = localStorage.getItem('jwt')
      axios.delete(
        this.$store.state.http_url+ this.$store.state.url_sing + "list/" + this.$store.state.enterroom.id +
        "/" + this.music.songId,
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(() => {
        // console.log("정상 노래삭제  ", res.data)
        this.$store.dispatch('Set_MusicList', this.$store.state.enterroom.id)
      })
      .catch(() => {
        // console.log(err)
      })

    }
  }
}
</script>

<style>

</style>