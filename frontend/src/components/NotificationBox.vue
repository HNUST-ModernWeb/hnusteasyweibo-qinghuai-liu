<template>
  <div class="notification-box">
    <div class="icon" @click="togglePanel" ref="iconRef">
      <span class="icon-symbol">{{ type === 'LIKE' ? '👍' : '💬' }}</span>
      <span v-if="unreadCount > 0" class="red-dot"></span>
    </div>
    <div v-if="showPanel" class="panel" ref="panelRef">
      <div class="panel-header">
        <h3>{{ type === 'LIKE' ? '点赞通知' : '评论通知' }}</h3>
        <button @click="closePanel">×</button>
      </div>
      <div class="notification-list">
        <div v-if="notifications.length === 0" class="empty">暂无通知</div>
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.read }"
          @click="readNotification(item)"
        >
          <img :src="item.senderAvatarUrl || '/uploads/default.png'" class="avatar" />
          <div class="content">
            <p>
              <strong>{{ item.senderUsername }}</strong>
              <span v-if="item.type === 'LIKE'"> 赞了你的帖子</span>
              <span v-else> 评论了你的帖子：{{ item.content }}</span>
            </p>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
      </div>
      <button v-if="hasMore" @click="loadMore" class="load-more">加载更多</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from '../api'

const props = defineProps({
  type: { type: String, required: true } // 'LIKE' 或 'COMMENT'
})

const showPanel = ref(false)
const notifications = ref([])
const page = ref(0)
const hasMore = ref(true)
const unreadCount = ref(0)

const iconRef = ref(null)
const panelRef = ref(null)

const togglePanel = async () => {
  showPanel.value = !showPanel.value
  if (showPanel.value) {
    page.value = 0
    await fetchNotifications(true)
    fetchUnreadCount()
  }
}

const closePanel = () => {
  showPanel.value = false
}

const fetchNotifications = async (reset = false) => {
  if (reset) page.value = 0
  try {
    const res = await api.get('/notification/list', { params: { page: page.value, size: 10 } })
    const data = res.data
    const filtered = data.content.filter(n => n.type === props.type)
    if (reset) {
      notifications.value = filtered
    } else {
      notifications.value.push(...filtered)
    }
    hasMore.value = !data.last
  } catch (e) {
    console.error(e)
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await api.get('/notification/unread-count', {
      params: { type: props.type }   // 传递类型
    })
    unreadCount.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const readNotification = async (item) => {
  if (!item.read) {
    try {
      await api.post(`/notification/read/${item.id}`)
      item.read = true
      fetchUnreadCount()
    } catch (e) {
      console.error(e)
    }
  }
  // 跳转到帖子详情
  if (item.postId) {
    window.location.hash = `#/post/${item.postId}`
    closePanel()
  }
}

const loadMore = () => {
  page.value++
  fetchNotifications()
}

const formatTime = (t) => new Date(t).toLocaleString()

const handleClickOutside = (event) => {
  if (panelRef.value && !panelRef.value.contains(event.target) && !iconRef.value.contains(event.target)) {
    showPanel.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  fetchUnreadCount()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-box {
  position: relative;
}
.icon {
  position: relative;
  cursor: pointer;
  font-size: 26px;
  margin: 0 4px;
}
.red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  background-color: #f02849;
  border-radius: 50%;
  border: 1px solid white;
}
.panel {
  position: absolute;
  top: 45px;
  right: -20px;
  width: 380px;
  max-width: 90vw;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
  z-index: 2000;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e6eb;
}
.panel-header h3 {
  margin: 0;
  font-size: 18px;
}
.panel-header button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #65676b;
}
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}
.empty {
  padding: 20px;
  text-align: center;
  color: #65676b;
}
.notification-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}
.notification-item.unread {
  background-color: #e7f3ff;
}
.notification-item:hover {
  background-color: #f0f2f5;
}
.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}
.content {
  flex: 1;
}
.content p {
  margin: 0 0 4px;
  font-size: 14px;
  word-break: break-word;
}
.time {
  font-size: 12px;
  color: #65676b;
}
.load-more {
  width: 100%;
  padding: 10px;
  border: none;
  background: #f0f2f5;
  cursor: pointer;
  font-weight: 500;
}
.load-more:hover {
  background: #e4e6eb;
}
</style>