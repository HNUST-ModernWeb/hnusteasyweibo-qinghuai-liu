<template>
  <div class="home">
          <div class="post-box">
      <textarea
              v-model="newPostContent"
              placeholder="请输入内容（支持 Win+. 或 Mac+Cmd+Ctrl+Space 输入表情）"
              maxlength="200"
      ></textarea>
              <div class="post-actions">
                  <span class="emoji-hint">😊 表情: Win+.</span>
                  <button @click="publishPost">发送</button>
              </div>
          </div>

    <div class="post-list">
      <div v-for="post in posts" :key="post.id" class="post-item">
        <img :src="post.avatarUrl" class="avatar" />
        <div class="post-content">
          <div class="post-header">
            <span class="username">{{ post.username }}</span>
            <span class="time">{{ formatTime(post.createTime) }}</span>
          </div>
          <p class="content">{{ post.content }}</p>
            <div class="post-footer">
              <span class="like-btn" @click="toggleLike(post)">
                {{ post.liked ? '❤️' : '🤍' }} {{ post.likeCount || 0 }}
              </span>
              <span>评论数({{ post.commentCount }})</span>
              <router-link :to="`/post/${post.id}`">查看详情</router-link>
              <button v-if="currentUser?.role === 'ADMIN'" class="delete-btn" @click="deletePost(post.id)">删除</button>
            </div>
        </div>
      </div>
    </div>

    <button v-if="hasMore" @click="loadMore" class="load-more">查看更多...</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { inject } from 'vue'

const currentUser = inject('currentUser')
const newPostContent = ref('')
const posts = ref([])
const page = ref(0)
const hasMore = ref(true)

const deletePost = async (postId) => {
  if (!confirm('管理员确认删除该帖子？')) return
  try {
    await api.delete(`/post/admin/post/${postId}`)
    // 刷新列表
    fetchPosts(true)
  } catch (e) {
    alert(e.response?.data || '删除失败')
  }
}

// 点赞切换
const toggleLike = async (post) => {
  if (!currentUser.value) {
    window.dispatchEvent(new CustomEvent('require-login'))
    return
  }
  try {
    const res = await api.post('/like/toggle', null, { params: { postId: post.id } })
    post.liked = res.data.liked
    post.likeCount = res.data.likeCount
  } catch (e) {
    console.error(e)
  }
}
const fetchPosts = async (reset = false) => {
  if (reset) page.value = 0
  try {
    const res = await api.get('/post/list', {
      params: {
        page: page.value,
        size: 10
        // 不需要手动传 userId，后端从 session 取
      }
    })
    const data = res.data
    if (reset) {
      posts.value = data.content
    } else {
      posts.value.push(...data.content)
    }
    hasMore.value = !data.last
  } catch (err) {
    console.error(err)
  }
}

const loadMore = () => {
  page.value++
  fetchPosts()
}

const publishPost = async () => {
  if (!currentUser.value) {
    window.dispatchEvent(new CustomEvent('require-login'))
    return
  }
  if (!newPostContent.value.trim()) return
  try {
    await api.post('/post/create', null, { params: { content: newPostContent.value } })
    newPostContent.value = ''
    // 刷新列表
    page.value = 0
    fetchPosts(true)
  } catch (err) {
    alert(err.response?.data || '发布失败')
  }
}

const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleString()
}

onMounted(() => fetchPosts(true))
</script>

<style scoped>
.home {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  padding: 16px;
}

.post-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e6eb;
}

.post-box textarea {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 1px solid #ccd0d5;
  border-radius: 20px;
  resize: none;
  height: 80px;
  background-color: #f0f2f5;
  transition: background 0.2s;
}

.post-box textarea:focus {
  outline: none;
  background-color: #fff;
  border-color: #1877f2;
}


.post-box button:hover {
  background-color: #166fe5;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e6eb;
}

.post-item .avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.post-content {
  flex: 1;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-weight: 600;
  color: #050505;
}

.time {
  font-size: 13px;
  color: #65676b;
}

.content {
  margin: 8px 0;
  line-height: 1.4;
  word-break: break-word;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 8px;
  font-size: 14px;
  color: #65676b;
}

.post-footer a {
  color: #1877f2;
  text-decoration: none;
  font-weight: 500;
}

.post-footer a:hover {
  text-decoration: underline;
}

.load-more {
  width: 100%;
  padding: 12px;
  margin-top: 16px;
  background-color: #e4e6eb;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  color: #050505;
  cursor: pointer;
  transition: background 0.2s;
}

.load-more:hover {
  background-color: #d8dadf;
}


/* 点赞按钮样式 */
.like-btn {
  cursor: pointer;
  user-select: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.like-btn:hover {
  opacity: 0.8;
}

/* 新增：发布框操作栏样式 */
.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.emoji-hint {
  font-size: 14px;
  color: #65676b;
  background-color: #f0f2f5;
  padding: 6px 12px;
  border-radius: 20px;
}

/* 覆盖原有的按钮样式，保证靠右 */
.post-actions button {
  background-color: #1877f2;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  margin-left: auto;
}

.post-actions button:hover {
  background-color: #166fe5;
}
.delete-btn {
  background: none;
  border: none;
  color: #f02849;
  cursor: pointer;
  font-size: 14px;
  margin-left: 12px;
}
.delete-btn:hover {
  text-decoration: underline;
}

</style>