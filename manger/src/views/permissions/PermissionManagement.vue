<template>
  <div class="permission-management">
    <div class="page-header">
      <h2>权限管理</h2>
      <p class="subtitle">管理系统角色和权限配置</p>
    </div>

    <el-card>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="角色管理" name="roles">
          <div class="roles-section">
            <div class="toolbar">
              <el-button 
                type="primary" 
                @click="showCreateRoleDialog"
                v-permission="'admin:permission:role'"
              >
                <el-icon><Plus /></el-icon>
                新增角色
              </el-button>
            </div>

            <el-table :data="roles" v-loading="rolesLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="roleName" label="角色名称" />
              <el-table-column prop="roleCode" label="角色代码" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="editRole(row)"
                    v-permission="'admin:permission:role'"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    type="warning" 
                    size="small" 
                    @click="assignPermissions(row)"
                    v-permission="'admin:permission:role'"
                  >
                    分配权限
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="菜单权限" name="menus">
          <div class="menus-section">
            <div class="toolbar">
              <el-button 
                type="primary" 
                @click="refreshMenus"
                v-permission="'admin:permission:menu'"
              >
                <el-icon><Refresh /></el-icon>
                刷新权限
              </el-button>
            </div>

            <el-tree
              :data="menuTree"
              :props="treeProps"
              show-checkbox
              node-key="id"
              default-expand-all
              v-loading="menusLoading"
            >
              <template #default="{ node, data }">
                <span class="custom-tree-node">
                  <span>{{ data.menuName }}</span>
                  <span class="node-extra">
                    <el-tag v-if="data.permission" size="small" type="info">
                      {{ data.permission }}
                    </el-tag>
                    <el-tag 
                      size="small" 
                      :type="getMenuTypeTag(data.menuType)"
                      style="margin-left: 8px;"
                    >
                      {{ getMenuTypeText(data.menuType) }}
                    </el-tag>
                  </span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog 
      v-model="roleDialogVisible" 
      :title="isEditMode ? '编辑角色' : '新增角色'"
      width="500px"
    >
      <el-form 
        :model="currentRole" 
        :rules="roleRules" 
        ref="roleFormRef"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="currentRole.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色代码" prop="roleCode">
          <el-input v-model="currentRole.roleCode" placeholder="请输入角色代码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="currentRole.description" 
            type="textarea" 
            placeholder="请输入角色描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch 
            v-model="currentRole.status" 
            :active-value="1" 
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="saveRole"
          :loading="saving"
        >
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog 
      v-model="permissionDialogVisible" 
      title="分配权限"
      width="600px"
    >
      <div v-if="selectedRole">
        <p><strong>角色：</strong>{{ selectedRole.roleName }}</p>
        <el-divider />
        
        <el-tree
          :data="menuTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          ref="permissionTreeRef"
          :default-checked-keys="selectedRole.permissions || []"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ data.menuName }}</span>
              <span class="node-extra">
                <el-tag v-if="data.permission" size="small" type="info">
                  {{ data.permission }}
                </el-tag>
              </span>
            </span>
          </template>
        </el-tree>
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="savePermissions"
          :loading="saving"
        >
          保存权限
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { permissionAPI } from '@/api/permission'

// 响应式数据
const activeTab = ref('roles')
const roles = ref([])
const menuTree = ref([])
const rolesLoading = ref(false)
const menusLoading = ref(false)
const saving = ref(false)

// 对话框相关
const roleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEditMode = ref(false)
const selectedRole = ref(null)

// 表单相关
const roleFormRef = ref()
const permissionTreeRef = ref()
const currentRole = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const roleRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { 
      pattern: /^[A-Z_]+$/, 
      message: '角色代码只能包含大写字母和下划线', 
      trigger: 'blur' 
    }
  ]
}

// 树形组件属性
const treeProps = {
  children: 'children',
  label: 'menuName'
}

// 页面加载
onMounted(() => {
  loadRoles()
  loadMenus()
})

// 标签切换
const handleTabClick = (tab) => {
  if (tab.name === 'menus' && menuTree.value.length === 0) {
    loadMenus()
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    rolesLoading.value = true
    const response = await permissionAPI.getRoles()
    
    if (response.code === 200) {
      roles.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取角色列表失败')
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    rolesLoading.value = false
  }
}

// 加载菜单权限
const loadMenus = async () => {
  try {
    menusLoading.value = true
    const response = await permissionAPI.getAllMenus()
    
    if (response.code === 200) {
      menuTree.value = buildMenuTree(response.data || [])
    } else {
      ElMessage.error(response.message || '获取菜单权限失败')
    }
  } catch (error) {
    console.error('加载菜单权限失败:', error)
    ElMessage.error('获取菜单权限失败')
  } finally {
    menusLoading.value = false
  }
}

// 构建菜单树
const buildMenuTree = (menus) => {
  const menuMap = {}
  const rootMenus = []
  
  // 创建菜单映射
  menus.forEach(menu => {
    menuMap[menu.id] = { ...menu, children: [] }
  })
  
  // 构建树结构
  menus.forEach(menu => {
    if (menu.parentId === 0) {
      rootMenus.push(menuMap[menu.id])
    } else if (menuMap[menu.parentId]) {
      menuMap[menu.parentId].children.push(menuMap[menu.id])
    }
  })
  
  return rootMenus
}

// 显示创建角色对话框
const showCreateRoleDialog = () => {
  isEditMode.value = false
  Object.assign(currentRole, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  roleDialogVisible.value = true
}

// 编辑角色
const editRole = (role) => {
  isEditMode.value = true
  Object.assign(currentRole, role)
  roleDialogVisible.value = true
}

// 保存角色
const saveRole = async () => {
  try {
    await roleFormRef.value.validate()
    
    saving.value = true
    let response
    
    if (isEditMode.value) {
      response = await permissionAPI.updateRole(currentRole.id, currentRole)
    } else {
      response = await permissionAPI.createRole(currentRole)
    }
    
    if (response.code === 200) {
      ElMessage.success(isEditMode.value ? '更新角色成功' : '创建角色成功')
      roleDialogVisible.value = false
      loadRoles()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存角色失败:', error)
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    saving.value = false
  }
}

// 分配权限
const assignPermissions = (role) => {
  selectedRole.value = role
  permissionDialogVisible.value = true
  
  // 等待对话框渲染完成后设置选中状态
  nextTick(() => {
    if (permissionTreeRef.value && role.permissions) {
      permissionTreeRef.value.setCheckedKeys(role.permissions)
    }
  })
}

// 保存权限分配
const savePermissions = async () => {
  try {
    saving.value = true
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    
    const response = await permissionAPI.assignRolePermissions(
      selectedRole.value.id, 
      checkedKeys
    )
    
    if (response.code === 200) {
      ElMessage.success('权限分配成功')
      permissionDialogVisible.value = false
      loadRoles()
    } else {
      ElMessage.error(response.message || '权限分配失败')
    }
  } catch (error) {
    console.error('权限分配失败:', error)
    ElMessage.error('权限分配失败')
  } finally {
    saving.value = false
  }
}

// 刷新菜单权限
const refreshMenus = () => {
  loadMenus()
  ElMessage.success('权限信息已刷新')
}

// 获取菜单类型标签
const getMenuTypeTag = (type) => {
  const typeMap = {
    1: 'success',  // 目录
    2: 'primary',  // 菜单
    3: 'info'      // 功能权限
  }
  return typeMap[type] || 'default'
}

// 获取菜单类型文本
const getMenuTypeText = (type) => {
  const typeMap = {
    1: '目录',
    2: '菜单', 
    3: '权限'
  }
  return typeMap[type] || '未知'
}
</script>

<style scoped>
.permission-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar {
  margin-bottom: 16px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-extra {
  display: flex;
  align-items: center;
}

.roles-section,
.menus-section {
  min-height: 400px;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
}
</style> 