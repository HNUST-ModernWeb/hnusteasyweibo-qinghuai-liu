import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import PostDetail from '../views/PostDetail.vue'
import Profile from '../views/Profile.vue'   // 确保导入

const routes = [
  { path: '/', component: Home },
  { path: '/post/:id', component: PostDetail },
  { path: '/profile', component: Profile }    // 确保存在
]

export default createRouter({
  history: createWebHistory(),
  routes
})