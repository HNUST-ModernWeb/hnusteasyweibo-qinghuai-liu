import { createApp, ref } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局用户状态
const currentUser = ref(null)
app.provide('currentUser', currentUser)
app.provide('setCurrentUser', (user) => { currentUser.value = user })

app.use(router).mount('#app')