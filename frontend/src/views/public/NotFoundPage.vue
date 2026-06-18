<script setup lang="ts">
import EmptyState from '../../components/EmptyState.vue'
</script>

<template>
  <EmptyState
    title="页面不存在"
    description="当前地址没有匹配的公开页面，可以回到首页继续浏览，或进入搜索页查找文章。"
    action-label="返回首页"
    action-to="/"
    secondary-label="搜索文章"
    secondary-to="/search"
  />
</template>
