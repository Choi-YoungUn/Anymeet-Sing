<template>
  <div>
    <Room
      id="roomList"
      v-for="roomitem, index in roomlist"
      :index = index
      :key="roomitem.id"
      :roomitem="roomitem"
      class="room-list-div"
      >
    </Room>
  </div>      
</template>

<script>
import Room from "@/components/roomhome/Room"

export default {
  name:"RoomList",
  components: {
    Room,
  },
  props: {
    roomlist: {
      type: Array,
    }
  },
  methods: {
    clickEnterRoom: function() {
      
      this.$router.push({name: 'RoomPassword'})
      this.$store.dispatch('Enter_Room', this.roomitem)
    },
  }
}
</script>

<style>

</style>