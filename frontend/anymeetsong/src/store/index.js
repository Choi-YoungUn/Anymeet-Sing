import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'
import createPersistedState from 'vuex-persistedstate'


Vue.use(Vuex)

export default new Vuex.Store({
  plugins: [createPersistedState()],
  state: {
    mynickname: '',
    myid: '',
    myprofile: '',
    password: '',
    search_music : [],
    music_list: [],
    
    //로컬 호스트용(개발용)
    // http_url : 'http://localhost:8080/anymeetnsing/',
    //서버용
    http_url : 'https://i6a505.p.ssafy.io:8443/anymeetnsing/',
    url_user : 'user/',
    url_room : 'room/',
    url_admin : 'admin/',
    url_sing : 'sing/',

    url_signup : 'user/regist',
    url_sendcode : 'user/regist/email',
    url_checkcode : 'user/regist',
    url_question : 'user/questions',
    url_profile : 'user/list',
    url_myinfo : 'user/list',
    url_useredit : 'user/update',
    url_adminroom : 'admin/room',
    url_adminuser : 'admin/user',
    url_adminuser2 : 'admin/user',
    url_banuser : 'admin/user/ban',
    url_admin_edit_user : 'admin/user/nickname',
    url_admin_pw: 'admin/user/password',
    url_admin_delete_user: 'admin/user/delete',
    url_admin_delete_room: 'admin/room',
    url_changepw: 'user/changepw',
    roomlist : [],
    setToken : function(){
      const token = localStorage.getItem('jwt')
        const config = {
          Authorization: `JWT ${token}`
        }
        return config
    },
    
    enterroom : {
      id : "",
      title : "",
      person_count: null,
      host_id : null,
      password : ""
    },

  },
  mutations: {
    GET_ROOMLIST: function(state, results){
      state.roomlist = results
    },
    ENTER_ROOM: function(state, results){
      state.enterroom = results
    },
    MY: function(state, results){
      state.is_admin = results.is_admin
      state.myid = results.id
      state.mynickname = results.nickname
    },
    EditMY: function(state, results){
      state.mynickname = results.nickname
    },
    GET_MYPROFILE: function(state, results){
      state.myprofile = results
    },
    SET_PASSWORD: function(state, results){
      state.password = results
    },
    SET_SEARCHMUSIC: function(state, results){
      state.search_music = results
    },
    SET_MUSICLIST: function(state, results){
      state.music_list = results
    },

  },
  actions: {
    Get_RoomList: function({commit}, request) {
      const token = localStorage.getItem('jwt')
      axios.post(
        this.state.http_url + this.state.url_room + "list",
        JSON.stringify(request),
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then(res =>{
        // console.log(res.data)
        commit('GET_ROOMLIST', res.data)

      })
      .catch(() => {
        // console.log(err)
      })
    },

    Enter_Room: function({commit}, request) {
      if (request.password){
        commit('ENTER_ROOM', request)
      } else {
        let res = {
          id : request.id,
          title : request.title,
          person_count: request.person_count,
          host_id : request.host_id,
          password : ""
        }
        commit('ENTER_ROOM', res)
      }
    },

    My: function({commit}, request){
      commit('MY', request)
    },

    EditMy: function({commit}, request){
      commit('EditMy', request)
    },

    Get_MyProfile: function({commit}){
      const token = localStorage.getItem('jwt')
      axios.get(
        this.state.http_url+ this.state.url_user + "list",
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then((res) => {
        // console.log(res.data)
        commit('GET_MYPROFILE', res.data)
      })
      .catch(() => {
        
      })
    },

    Set_Password: function({commit}, request){
      commit('SET_PASSWORD', request)
    },
    
    Set_SearchMusic: function({commit}, request){
      commit('SET_SEARCHMUSIC', request)
    },

    Set_MusicList: function({commit}, request){
      
      const token = localStorage.getItem('jwt')
      axios.get(
        this.state.http_url+ this.state.url_sing + "list/" + request,
        {
          headers: {
            "Content-Type": `application/json`,
            "access-token": token,
          },
        }
      )
      .then((res) => {
        // console.log("list  :  " ,res.data.list)
        commit('SET_MUSICLIST', res.data.list)
      })
      .catch(() => {
        
      })
    },
  },
  modules: {

  }
})