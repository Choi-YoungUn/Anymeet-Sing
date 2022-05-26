<template>

  <ul class="list-group list-group-horizontal">
    <li class="list-group-item list-item" style="width:45%;">{{roomitem.title}}</li>
    <li class="list-group-item list-item" style="width:20%;">{{roomitem.host_id}}</li>
    <li class="list-group-item list-item" style="width:15%;">{{isOpen}}</li>
    <li class="list-group-item list-item" style="width:10%;">
      <span class="badge bg-primary rounded-pill">{{roomitem.person_count}} / 8</span>
    </li>
    <button class="btn btn-primary" type="submit" @click="clickEnterRoom" style="width:10%; border: solid; border-color: rgba(8, 4, 58, 0.692);">입장</button>
  </ul>

</template>

<script>
export default {
  name:"Room",
  props: {
    roomitem: {
      type: Object,
    }
  },
  methods: {
    clickEnterRoom: function() {
      
      this.$router.push({name: 'RoomPassword'})
      this.$store.dispatch('Enter_Room', this.roomitem)
    },
  },
  computed: {
    isOpen: function() {
      if (this.roomitem.password){
        return '비공개'
      } else {
        return '공개'
      }
    }
  }
}
</script>

<style>
.list-item {
  background-color:rgba(255, 255, 255, 0.692) !important;
  
}
</style>