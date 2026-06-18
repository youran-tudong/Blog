import { createPinia } from 'pinia'
import { createApp } from 'vue'
import { router } from './router'
import './styles/index.css'

createApp({
  template: '<RouterView />',
})
  .use(createPinia())
  .use(router)
  .mount('#root')

