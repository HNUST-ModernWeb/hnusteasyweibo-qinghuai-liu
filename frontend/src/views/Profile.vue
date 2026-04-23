<template>
  <div class="profile">
    <div class="profile-header">
      <img :src="user.avatarUrl || '/uploads/default.png'" class="avatar-large" />
      <div class="info">
        <h2>{{ user.username }}</h2>
        <p>注册时间：{{ formatTime(user.registerTime) }}</p>
        <button @click="showEditDialog = true">编辑资料</button>
      </div>
    </div>

    <div class="my-posts">
      <h3>我的帖子</h3>
      <div v-if="posts.length === 0" class="empty">暂无帖子</div>
      <div v-for="post in posts" :key="post.id" class="post-item">
        <div class="post-content">
          <p>{{ post.content }}</p>
          <span class="time">{{ formatTime(post.createTime) }}</span>
        </div>
        <div class="post-actions">
          <button @click="deletePost(post.id)">删除</button>
        </div>
      </div>
      <button v-if="hasMore" @click="loadMore" class="load-more">加载更多</button>
    </div>

    <!-- 编辑资料对话框 -->
    <div v-if="showEditDialog" class="modal">
      <div class="modal-content">
        <h3>编辑资料</h3>
        <form @submit.prevent="updateProfile">
          <div class="avatar-upload">
            <img :src="editForm.avatarPreview || user.avatarUrl" class="avatar-preview" />
            <input type="file" accept="image/*" @change="onAvatarChange" />
          </div>
          <input v-model="editForm.username" placeholder="用户名" />
          <input v-model="editForm.password" type="password" placeholder="新密码（留空则不修改）" />
          <button type="submit">保存</button>
          <button type="button" @click="showEditDialog = false">取消</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import api from '../api'

const currentUser = inject('currentUser')
const setCurrentUser = inject('setCurrentUser')
const user = ref({})
const posts = ref([])
const page = ref(0)
const hasMore = ref(true)
const showEditDialog = ref(false)
const editForm = ref({
  username: '',
  password: '',
  avatarPreview: '',
  avatarFile: null
})

const fetchProfile = async () => {
  const res = await api.get('/user/profile')
  user.value = res.data
  editForm.value.username = user.value.username
}

const fetchMyPosts = async (reset = false) => {
  if (reset) page.value = 0
  const res = await api.get('/user/my-posts', { params: { page: page.value, size: 10 } })
  const data = res.data
  if (reset) {
    posts.value = data.content
  } else {
    posts.value.push(...data.content)
  }
  hasMore.value = !data.last
}

const loadMore = () => {
  page.value++
  fetchMyPosts()
}

const deletePost = async (postId) => {
  if (!confirm('确定删除这条帖子吗？')) return
  await api.delete(`/user/post/${postId}`)
  fetchMyPosts(true)
}

const onAvatarChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    editForm.value.avatarFile = file
    const reader = new FileReader()
    reader.onload = (e) => editForm.value.avatarPreview = e.target.result
    reader.readAsDataURL(file)
  }
}

const updateProfile = async () => {
  const formData = new FormData()
  if (editForm.value.username !== user.value.username) {
    formData.append('username', editForm.value.username)
  }
  if (editForm.value.password) {
    formData.append('password', editForm.value.password)
  }
  if (editForm.value.avatarFile) {
    formData.append('avatar', editForm.value.avatarFile)
  }
  const res = await api.put('/user/profile', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  setCurrentUser(res.data)
  user.value = res.data
  showEditDialog.value = false
}

const formatTime = (t) => new Date(t).toLocaleString()

onMounted(() => {
  fetchProfile()
  fetchMyPosts(true)
})
</script>

<style scoped>
.profile {
  background: white;
  border-radius: 8px;
  padding: 24px;
}
.profile-header {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}
.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}
.info h2 {
  margin: 0 0 8px;
}
.my-posts {
  border-top: 1px solid #e4e6eb;
  padding-top: 24px;
}
.post-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}
.post-actions button {
  background: none;
  border: none;
  color: #f02849;
  cursor: pointer;
}
.modal {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
}
.avatar-preview {
  width: 80px; height: 80px; border-radius: 50%;
}
</style>