<template>
    <div v-if="post" class="detail">
        <div class="post-detail">
            <img :src="post.avatarUrl || '/uploads/default.png'" class="avatar" />
            <div class="post-info">
                <h2>{{ post.username }}</h2>
                <p class="post-content">{{ post.content }}</p>
                <div class="post-meta">
                    <span>{{ formatTime(post.createTime) }}</span>
                    <span>评论数 {{ post.commentCount }}</span>
                    <span class="like-btn" @click="toggleLike(post)">
            {{ post.liked ? '❤️' : '🤍' }} {{ post.likeCount || 0 }}
          </span>
                </div>
                <!-- 管理员删除按钮 -->
                <div v-if="currentUser?.role === 'ADMIN'" class="admin-actions">
                    <button class="delete-btn" @click="deletePost">🗑️ 删除帖子</button>
                </div>
            </div>
        </div>

        <div class="comment-section">
            <h3>评论</h3>

            <!-- 评论输入区域 -->
            <div class="comment-input-area">
        <textarea
                v-model="newComment"
                placeholder="写下你的评论（支持 Win+. 输入表情）"
                maxlength="200"
        ></textarea>

                <!-- 图片上传按钮 - 醒目可见 -->
                <div class="upload-row">
                    <label for="comment-image" class="upload-btn">📷 选择图片</label>
                    <input
                            type="file"
                            id="comment-image"
                            accept="image/*"
                            @change="onImageChange"
                            ref="imageInput"
                    />
                    <span v-if="selectedImage" class="file-name">{{ selectedImage.name }}</span>
                    <button v-if="selectedImage" class="clear-btn" @click="clearImage">×</button>
                </div>

                <!-- 图片预览 -->
                <div v-if="imagePreview" class="image-preview">
                    <img :src="imagePreview" alt="预览" />
                </div>

                <!-- 发送按钮 -->
                <div class="submit-row">
                    <button class="submit-btn" @click="submitComment">发表评论</button>
                </div>
            </div>

            <!-- 评论列表 -->
            <div v-if="comments.length === 0" class="empty">暂无评论，快来抢沙发~</div>
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <img :src="comment.avatarUrl || '/uploads/default.png'" class="avatar-small" />
                <div class="comment-content">
                    <div class="comment-header">
                        <strong>{{ comment.username }}</strong>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <p class="comment-text">{{ comment.content }}</p>
                    <img
                            v-if="comment.imageUrl"
                            :src="comment.imageUrl"
                            class="comment-image"
                            @click="previewImage(comment.imageUrl)"
                    />
                </div>
            </div>

            <button v-if="commentHasMore" @click="loadMoreComments" class="load-more">
                查看更多评论...
            </button>
        </div>
    </div>
</template>

<script setup>
    import { ref, onMounted, inject } from 'vue'
    import { useRoute, useRouter } from 'vue-router'
    import api from '../api'

    const route = useRoute()
    const router = useRouter()
    const postId = route.params.id
    const currentUser = inject('currentUser')

    const post = ref(null)
    const comments = ref([])
    const commentPage = ref(0)
    const commentHasMore = ref(true)
    const newComment = ref('')
    const selectedImage = ref(null)
    const imagePreview = ref('')
    const imageInput = ref(null)

    const fetchPost = async () => {
      try {
        const res = await api.get(`/post/detail/${postId}`)
        post.value = res.data
      } catch (e) {
        console.error(e)
      }
    }

    const fetchComments = async (reset = false) => {
      if (reset) commentPage.value = 0
      try {
        const res = await api.get(`/comment/list/${postId}`, {
          params: { page: commentPage.value, size: 10 }
        })
        const data = res.data
        if (reset) {
          comments.value = data.content
        } else {
          comments.value.push(...data.content)
        }
        commentHasMore.value = !data.last
      } catch (e) {
        console.error(e)
      }
    }

    const loadMoreComments = () => {
      commentPage.value++
      fetchComments()
    }

    const submitComment = async () => {
      if (!currentUser.value) {
        window.dispatchEvent(new CustomEvent('require-login'))
        return
      }
      if (!newComment.value.trim() && !selectedImage.value) {
        alert('评论内容或图片不能同时为空')
        return
      }
      const formData = new FormData()
      formData.append('postId', postId)
      formData.append('content', newComment.value.trim() || '')
      if (selectedImage.value) {
        formData.append('image', selectedImage.value)
      }
      try {
        await api.post('/comment/create', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        newComment.value = ''
        clearImage()
        fetchComments(true)
        fetchPost()
      } catch (err) {
        alert(err.response?.data || '评论失败')
      }
    }

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

    const deletePost = async () => {
      if (!confirm('管理员确认删除该帖子？')) return
      try {
        await api.delete(`/post/admin/post/${postId}`)
        alert('删除成功')
        router.push('/')
      } catch (e) {
        alert(e.response?.data || '删除失败')
      }
    }

    const onImageChange = (e) => {
      const file = e.target.files[0]
      if (file) {
        selectedImage.value = file
        const reader = new FileReader()
        reader.onload = (e) => (imagePreview.value = e.target.result)
        reader.readAsDataURL(file)
      }
    }

    const clearImage = () => {
      selectedImage.value = null
      imagePreview.value = ''
      if (imageInput.value) {
        imageInput.value.value = ''
      }
    }

    const previewImage = (url) => {
      window.open(url, '_blank')
    }

    const formatTime = (timeStr) => {
      return new Date(timeStr).toLocaleString()
    }

    onMounted(() => {
      fetchPost()
      fetchComments(true)
    })
</script>

<style scoped>
    .detail {
      background: white;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }

    .post-detail {
      display: flex;
      gap: 16px;
      padding-bottom: 24px;
      border-bottom: 1px solid #e4e6eb;
    }

    .avatar {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      object-fit: cover;
    }

    .post-info {
      flex: 1;
    }

    .post-info h2 {
      margin: 0 0 8px;
      font-size: 20px;
    }

    .post-content {
      font-size: 16px;
      line-height: 1.5;
      margin: 12px 0;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .post-meta {
      display: flex;
      align-items: center;
      gap: 20px;
      font-size: 14px;
      color: #65676b;
    }

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

    .admin-actions {
      margin-top: 16px;
    }

    .delete-btn {
      background-color: #f02849;
      color: white;
      border: none;
      padding: 8px 20px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }

    .delete-btn:hover {
      background-color: #d41e3c;
    }

    .comment-section {
      margin-top: 24px;
    }

    .comment-section h3 {
      font-size: 18px;
      margin-bottom: 16px;
    }

    .comment-input-area {
      margin-bottom: 24px;
    }

    .comment-input-area textarea {
      width: 100%;
      padding: 12px;
      border: 1px solid #ccd0d5;
      border-radius: 12px;
      resize: none;
      height: 80px;
      background-color: #f0f2f5;
      font-size: 16px;
      margin-bottom: 12px;
    }

    .comment-input-area textarea:focus {
      outline: none;
      background-color: #fff;
      border-color: #1877f2;
    }

    .upload-row {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
    }

    .upload-btn {
      display: inline-block;
      background-color: #42b72a;
      color: white;
      padding: 8px 18px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: bold;
      cursor: pointer;
      transition: background 0.2s;
      border: none;
    }

    .upload-btn:hover {
      background-color: #36a420;
    }

    #comment-image {
      display: none;
    }

    .file-name {
      font-size: 13px;
      color: #555;
      max-width: 180px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .clear-btn {
      background: none;
      border: none;
      color: #f02849;
      font-size: 22px;
      cursor: pointer;
      line-height: 1;
    }

    .image-preview {
      margin-top: 12px;
    }

    .image-preview img {
      max-width: 100%;
      max-height: 150px;
      border-radius: 8px;
      border: 1px solid #ddd;
    }

    .submit-row {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }

    .submit-btn {
      background-color: #1877f2;
      color: white;
      border: none;
      padding: 10px 28px;
      border-radius: 24px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }

    .submit-btn:hover {
      background-color: #166fe5;
    }

    .comment-item {
      display: flex;
      gap: 12px;
      padding: 16px 0;
      border-bottom: 1px solid #e4e6eb;
    }

    .avatar-small {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
    }

    .comment-content {
      flex: 1;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }

    .comment-header strong {
      font-size: 15px;
      color: #050505;
    }

    .comment-time {
      font-size: 12px;
      color: #65676b;
    }

    .comment-text {
      margin: 6px 0;
      word-break: break-word;
    }

    .comment-image {
      max-width: 100%;
      max-height: 200px;
      border-radius: 8px;
      margin-top: 8px;
      cursor: pointer;
      border: 1px solid #e4e6eb;
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

    .empty {
      text-align: center;
      padding: 24px;
      color: #65676b;
    }
</style>