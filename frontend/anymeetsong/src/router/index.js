import Vue from 'vue'
import VueRouter from 'vue-router'
import LogHome from '../components/user/LogHome.vue'
import IdFind from '../components/user/IdFind.vue'
import PwFind from '../components/user/PwFind.vue'
import SignUp from '../components/user/SignUp.vue'
import NavbarHome from '../components/roomhome/NavbarHome.vue'
import MainRoom from '../components/singingroom/MainRoom.vue'
import CreateRoom from '../components/roomhome/CreateRoom.vue'

import RoomHome from "../components/roomhome/RoomHome"
import RoomPassword from "../components/roomhome/RoomPassword"
import Profile from "../components/user/Profile"
import EditMyInfo from "../components/user/EditMyInfo"
import Admin from "../components/admin/Admin"
import ChangePw from "../components/user/ChangePw"


Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'LogHome',
    component: LogHome
  },
  {
    path: '/IdFind',
    name: 'IdFind',
    component: IdFind
  },
  {
    path: '/PwFind',
    name: 'PwFind',
    component: PwFind
  },
  {
    path: '/SignUp',
    name: 'SignUp',
    component: SignUp,
  },
  {
    path: '/NavbarHome',
    name: 'NavbarHome',
    component: NavbarHome,
    children:[
      {
        path: '/RoomHome',
        name: 'RoomHome',
        component: RoomHome,
      },
      {
        path: '/RoomPassword',
        name: 'RoomPassword',
        component: RoomPassword,
      },
      {
        path: '/Profile',
        name: 'Profile',
        component: Profile,
      },
      {
        path: '/EditMyInfo',
        name: 'EditMyInfo',
        component: EditMyInfo,
      },
      {
        path: '/ChangePw',
        name: 'ChangePw',
        component: ChangePw,
      },
      {
        path: '/Admin',
        name: 'Admin',
        component: Admin,
      },
      {
        path: '/CreateRoom',
        name: 'CreateRoom',
        component: CreateRoom,
      },
    ]
  },
  {
    path: '/MainRoom/:roomid',
    name: 'MainRoom',
    component: MainRoom,
  },
  
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
export default router
