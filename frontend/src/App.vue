<template>
  <div class="app">
    <header class="header">
      <h1 @click="$router.push('/')">简易微博</h1>
      <div class="user-area">
        <!-- 通知邮箱（仅登录后显示） -->
        <NotificationBox v-if="currentUser" type="LIKE" />
        <NotificationBox v-if="currentUser" type="COMMENT" />
        <!-- 用户信息 -->
        <template v-if="currentUser">
          <router-link to="/profile" class="avatar-link">
            <img :src="avatarUrl" class="avatar-small" />
          </router-link>
          <span>{{ currentUser.username }}</span>
          <button @click="logout">退出</button>
        </template>
        <button v-else @click="showLoginDialog = true">登录</button>
      </div>
    </header>
    <router-view />
    <LoginDialog v-model="showLoginDialog" @success="onLoginSuccess" />
  </div>
</template>

<script setup>
import { ref, inject, computed } from 'vue'
import LoginDialog from './components/LoginDialog.vue'
import NotificationBox from './components/NotificationBox.vue'
import api from './api'

const currentUser = inject('currentUser')
const setCurrentUser = inject('setCurrentUser')
const showLoginDialog = ref(false)

const avatarUrl = computed(() => {
  return currentUser.value?.avatarUrl || '/uploads/default.png'
})

const logout = async () => {
  await api.post('/user/logout')
  setCurrentUser(null)
}

const onLoginSuccess = (user) => {
  setCurrentUser(user)
  showLoginDialog.value = false
}

window.addEventListener('require-login', () => {
  if (!currentUser.value) showLoginDialog.value = true
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background-color: #f0f2f5;
  color: #1c1e21;
}

.app {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ddd;
  background-color: #ffffff;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1877f2;
  cursor: pointer;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ddd;
}

.user-area button {
  background-color: #e4e6eb;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.user-area button:hover {
  background-color: #d8dadf;
}
.avatar-link {
  display: inline-block;
  line-height: 0;
}

.avatar-link:hover .avatar-small {
  opacity: 0.8;
  transition: opacity 0.2s;
}
</style>