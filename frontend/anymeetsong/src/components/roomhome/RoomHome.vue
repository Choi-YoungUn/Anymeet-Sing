<template> 
  <div id="RoomHome" style="margin-top:150px;">
    
    <div class="room-buttons">

      <div class="search-div">
        <b-form-select
          v-model="filterSel"
          :options="options"
          class="select-type"
          value-field="item"
          text-field="name"
          disabled-field="notEnabled"
          style="cursor:pointer;"
        ></b-form-select>
        <input type="text" class="inputSelect mx-2" placeholder="방 번호 혹은 제목을 입력해주세요"  v-model="title" @keyup="get_RoomList">
        <b-button variant="primary" @click="get_RoomList">검색하기</b-button>
      </div>

      <button class="btn btn-primary" type="submit" @click="create_Room">노래방 생성</button>
      
    </div>

    <div>
      <ul class="room-list-div list-group list-group-horizontal">
        <li class="list-group-item room-list-name" style="width:45%;">방 이름</li>
        <li class="list-group-item room-list-name" style="width:20%;">호스트</li>
        <li class="list-group-item room-list-name" style="width:15%;">공개 여부</li>
        <li class="list-group-item room-list-name" style="width:10%;">인원</li>
        <li class="list-group-item room-list-name" type="submit" @click="get_RoomList" @mouseover="spinIcon" @mouseout="spinIcon" style="width:10%;">
          <b-icon icon="arrow-clockwise" font-scale="2" id="refreshIcon" v-if="isSpin"></b-icon>
          <b-icon icon="arrow-clockwise" animation="spin" font-scale="2" id="refreshIcon" v-else></b-icon>
        </li>
      </ul>
      <Room-List
        :roomlist="paginatedData"
        style="min-height:480px;"
        >
      </Room-List>

      <div class="btn-cover">
        <b-button :disabled="pageNum === 0" @click="prevPage" class="page-btn" variant="primary">
          이전
        </b-button>
        <span class="page-count mx-2" style="cursor:default;">{{ pageNum + 1 }} / {{ pageCount }} 페이지</span>
        <b-button :disabled="pageNum >= pageCount - 1" @click="nextPage" class="page-btn" variant="primary">
          다음
        </b-button>
      </div>
    </div>
  </div>
  
</template>

<script>
import RoomList from "@/components/roomhome/RoomList"


export default {
  name: "RoomHome",
  components:{
    RoomList,
  },
  data: function() {
    return {
      title: '',
      filterSel: '0',
      options: [
        { item: '0', name: '전체방' },
        { item: '1', name: '공개방' },
        { item: '2', name: '비 공개방'},
      ],
      isSpin: true,
      pageNum: 0,
      pageSize: 10,
    }
  },
  created: function(){
    let request = {
      filter : "0",
      title : ''
    }
    this.$store.dispatch('Get_RoomList', request)
  },
  
  methods: {
    get_RoomList: function() {
      let request = {
        title : this.title,
        filter : this.filterSel
      }
      this.$store.dispatch('Get_RoomList', request)
    },
    create_Room: function() {
      this.$router.push({name: 'CreateRoom'})
    },
    spinIcon: function() {
      this.isSpin = !this.isSpin
    },
    nextPage () {
      this.pageNum += 1;
    },
    prevPage () {
      this.pageNum -= 1;
    }
  },
  computed:{
    roomlist: function(){
      return this.$store.state.roomlist
    },
    pageCount () {
      let listLeng = this.roomlist.length,
          listSize = this.pageSize,
          page = Math.floor(listLeng / listSize);
      if (listLeng % listSize > 0) page += 1;
      
      /*
      아니면 page = Math.floor((listLeng - 1) / listSize) + 1;
      이런식으로 if 문 없이 고칠 수도 있다!
      */
      return page;
    },
    paginatedData () {
      const start = this.pageNum * this.pageSize,
            end = start + this.pageSize;
      return this.roomlist.slice(start, end);
    },
    // ...mapState([
    //   'roomlist',
    //   ])
  },

  watch: {
    filterSel: function(){
      let request = {
        title : this.title,
        filter : this.filterSel
      }
      this.$store.dispatch('Get_RoomList', request)
    }
  }

}
</script>

<style>
.back2 {
  min-width: 1000px;
  height: 936px;
  background-image: url("../../assets/images/bgimg2.jpg");
  background-size: cover;
}

.search-div {
  width: 680px;
  float: left;
  text-align: left;
}

.room-list-div {
  width:100%;
  min-width: 1000px;
  padding-left: 100px !important;
  padding-right: 100px;
}

.room-buttons {
  padding-left: 100px;
  padding-right: 100px;
  margin-bottom: 10px;
  text-align: end;
}

.select-type {
  display: inline-block;
  width: 100px;
  height: 38px;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
}

.room-list-name {
  height: 50px;
  padding-top: 12px !important;
  background-color:rgba(24, 23, 23, 0.692) !important;
  color: beige !important;
}
</style>