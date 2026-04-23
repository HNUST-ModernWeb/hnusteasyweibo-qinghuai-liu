<template>
  <div v-if="visible" class="modal">
    <div class="modal-content">
      <span class="close" @click="close">&times;</span>
      <div class="tabs">
        <button :class="{ active: tab === 'login' }" @click="tab = 'login'">登录</button>
        <button :class="{ active: tab === 'register' }" @click="tab = 'register'">注册</button>
      </div>

      <!-- 登录表单 -->
      <form v-if="tab === 'login'" @submit.prevent="handleLogin">
        <input v-model="loginForm.username" placeholder="用户名" required />
        <input v-model="loginForm.password" type="password" placeholder="密码" required />
        <button type="submit">登录</button>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="handleRegister">
        <div class="avatar-upload">
          <img :src="avatarPreview || '/uploads/default.png'" class="avatar-preview" />
          <input type="file" accept="image/*" @change="onAvatarChange" />
        </div>
        <input v-model="registerForm.username" placeholder="用户名" required />
        <input v-model="registerForm.password" type="password" placeholder="密码" required />
        <input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" required />
        <button type="submit">注册</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import api from '../api'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const tab = ref('login')
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })
const avatarFile = ref(null)
const avatarPreview = ref('')

const onAvatarChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    avatarFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => avatarPreview.value = e.target.result
    reader.readAsDataURL(file)
  }
}

const handleLogin = async () => {
  try {
    const res = await api.post('/user/login', null, {
      params: loginForm
    })
    emit('success', res.data.user)
    resetForms()
  } catch (err) {
    alert(err.response?.data || '登录失败')
  }
}

const handleRegister = async () => {
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次密码不一致')
    return
  }
  const formData = new FormData()
  formData.append('username', registerForm.username)
  formData.append('password', registerForm.password)
  if (avatarFile.value) {
    formData.append('avatar', avatarFile.value)
  }
  try {
    const res = await api.post('/user/register', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    alert('注册成功，请登录')
    tab.value = 'login'
    loginForm.username = registerForm.username
    resetForms()
  } catch (err) {
    alert(err.response?.data || '注册失败')
  }
}

const resetForms = () => {
  loginForm.username = loginForm.password = ''
  registerForm.username = registerForm.password = registerForm.confirmPassword = ''
  avatarFile.value = null
  avatarPreview.value = ''
}

const close = () => { visible.value = false }
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
  padding: 24px;
  position: relative;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.close {
  position: absolute;
  top: 16px;
  right: 20px;
  font-size: 28px;
  cursor: pointer;
  color: #65676b;
}

.close:hover {
  color: #050505;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.tabs button {
  background: none;
  border: none;
  font-size: 18px;
  font-weight: 500;
  padding: 6px 0;
  margin-right: 20px;
  color: #65676b;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.tabs button.active {
  color: #1877f2;
  border-bottom-color: #1877f2;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

input {
  padding: 12px 16px;
  border: 1px solid #ccd0d5;
  border-radius: 8px;
  font-size: 16px;
  background-color: #f0f2f5;
}

input:focus {
  outline: none;
  background-color: #fff;
  border-color: #1877f2;
}

button[type="submit"] {
  background-color: #1877f2;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: background 0.2s;
}

button[type="submit"]:hover {
  background-color: #166fe5;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
}

.avatar-upload input {
  width: 100%;
}
</style>