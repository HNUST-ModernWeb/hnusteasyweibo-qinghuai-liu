<template>
    <div class="emoji-bar">
        <button type="button" @click="toggleBar" class="emoji-toggle-btn" title="表情">😊</button>
        <div v-if="showBar" class="emoji-panel">
            <emoji-picker @emoji-click="handleEmojiClick"></emoji-picker>
        </div>
    </div>
</template>

<script setup>
    import { ref } from 'vue'
    import 'emoji-picker-element'

    const emit = defineEmits(['select'])
    const showBar = ref(false)

    const toggleBar = () => {
      showBar.value = !showBar.value
    }

    const handleEmojiClick = (e) => {
      emit('select', e.detail.unicode)
    }
</script>

<style scoped>
    .emoji-bar {
      margin-top: 8px;
    }
    .emoji-toggle-btn {
      background: none;
      border: none;
      font-size: 22px;
      cursor: pointer;
    }
    .emoji-panel {
      margin-top: 8px;
      border: 1px solid #ddd;
      border-radius: 8px;
      overflow: hidden;
      background: white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      max-width: 100%;
    }
</style>