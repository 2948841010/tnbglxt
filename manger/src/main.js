import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 全局样式
import './styles/index.css'

// 权限指令
import { permission, role } from '@/directives/permission'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('permission', permission)
app.directive('role', role)

// 安装插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 初始化用户认证状态
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.initAuth().then(() => {
// 挂载应用
app.mount('#app') 
}) 