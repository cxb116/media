<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-50 object-cover" src="@imgs/user/bg.webp" />
          <img
            class="relative z-10 w-20 h-20 mt-30 mx-auto object-cover border-2 border-white rounded-full"
            src="@imgs/user/avatar.webp"
          />
          <h2 class="mt-5 text-xl font-normal">{{ mediaUserInfo.name || mediaUserInfo.account }}</h2>
          <p class="mt-5 text-sm">媒体用户</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ mediaUserInfo.contactEmail || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ mediaUserInfo.name || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:building-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ mediaUserInfo.mediaCompanyName || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:phone-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ mediaUserInfo.contactPhone || '-' }}</span>
            </div>
          </div>

          <div class="mt-10">
            <h3 class="text-sm font-medium">账号状态</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <div class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded">
                状态：{{ getStatusText(mediaUserInfo.enable) }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            :model="form"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            ref="ruleFormRef"
            :rules="rules"
            label-width="100px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="姓名" prop="realName">
                <ElInput v-model="form.realName" :disabled="true" />
              </ElFormItem>
              <ElFormItem label="账号" prop="account" class="ml-5">
                <ElInput v-model="mediaUserInfo.account" :disabled="true" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="联系邮箱" prop="email">
                <ElInput v-model="form.email" :disabled="!isEdit" />
              </ElFormItem>
              <ElFormItem label="联系电话" prop="mobile" class="ml-5">
                <ElInput v-model="form.mobile" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="公司名称" prop="companyName">
                <ElInput v-model="mediaUserInfo.mediaCompanyName" :disabled="true" />
              </ElFormItem>
              <ElFormItem label="公司地址" prop="address" class="ml-5">
                <ElInput v-model="form.address" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElFormItem label="备注" prop="des" class="h-32">
              <ElInput type="textarea" :rows="4" v-model="form.des" :disabled="!isEdit" />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="edit" :loading="submitting">
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">更改密码</h1>

          <ElForm :model="pwdForm" class="box-border p-5" label-width="86px" label-position="top">
            <ElFormItem label="当前密码" prop="password">
              <ElInput
                v-model="pwdForm.password"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="editPwd">
                {{ isEditPwd ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { fetchUpdateMediaInfo } from '@/api/mediaAuth'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'UserCenter' })

  const userStore = useUserStore()
  const userInfo = computed(() => userStore.getUserInfo)

  // 获取媒体用户信息
  const mediaUserInfo = ref<any>({})

  const isEdit = ref(false)
  const isEditPwd = ref(false)
  const date = ref('')
  const ruleFormRef = ref<FormInstance>()
  const submitting = ref(false)

  /**
   * 用户信息表单
   */
  const form = reactive({
    realName: '',
    nikeName: '',
    email: '',
    mobile: '',
    address: '',
    sex: '1',
    des: ''
  })

  /**
   * 密码修改表单
   */
  const pwdForm = reactive({
    password: '',
    newPassword: '',
    confirmPassword: ''
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    mobile: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ]
  })

  /**
   * 获取状态文本
   */
  const getStatusText = (enable: number) => {
    switch (enable) {
      case 0: return '禁用'
      case 1: return '正常'
      case 2: return '审核中'
      case 3: return '拒绝'
      default: return '未知'
    }
  }

  onMounted(() => {
    loadMediaUserInfo()
    getDate()
  })

  /**
   * 加载媒体用户信息
   */
  const loadMediaUserInfo = () => {
    const userInfoStr = localStorage.getItem('media_user_info')
    if (userInfoStr) {
      try {
        mediaUserInfo.value = JSON.parse(userInfoStr)

        // 填充表单数据
        form.realName = mediaUserInfo.value.name || ''
        form.nikeName = mediaUserInfo.value.name || ''
        form.email = mediaUserInfo.value.contactEmail || ''
        form.mobile = mediaUserInfo.value.contactPhone || ''
        form.address = mediaUserInfo.value.mediaCompanyAddress || ''
        form.des = mediaUserInfo.value.remark || ''
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  }

  /**
   * 根据当前时间获取问候语
   */
  const getDate = () => {
    const h = new Date().getHours()

    if (h >= 6 && h < 9) date.value = '早上好'
    else if (h >= 9 && h < 11) date.value = '上午好'
    else if (h >= 11 && h < 13) date.value = '中午好'
    else if (h >= 13 && h < 18) date.value = '下午好'
    else if (h >= 18 && h < 24) date.value = '晚上好'
    else date.value = '很晚了，早点睡'
  }

  /**
   * 切换用户信息编辑状态并保存
   */
  const edit = async () => {
    if (isEdit.value) {
      // 保存操作
      await saveUserInfo()
    } else {
      // 进入编辑模式
      isEdit.value = !isEdit.value
    }
  }

  /**
   * 保存用户信息
   */
  const saveUserInfo = async () => {
    if (!ruleFormRef.value) return

    try {
      await ruleFormRef.value.validate()
      submitting.value = true

      await fetchUpdateMediaInfo({
        id: mediaUserInfo.value.id,
        name: form.realName,
        contactEmail: form.email,
        contactPhone: form.mobile,
        mediaCompanyAddress: form.address,
        remark: form.des
      })

      // 更新本地存储的用户信息
      const updatedUserInfo = {
        ...mediaUserInfo.value,
        name: form.realName,
        contactEmail: form.email,
        contactPhone: form.mobile,
        mediaCompanyAddress: form.address,
        remark: form.des
      }
      localStorage.setItem('media_user_info', JSON.stringify(updatedUserInfo))
      mediaUserInfo.value = updatedUserInfo

      ElMessage.success('保存成功')
      isEdit.value = false
    } catch (error: any) {
      console.error('保存失败', error)
      ElMessage.error(error?.message || '保存失败')
    } finally {
      submitting.value = false
    }
  }

  /**
   * 切换密码编辑状态
   */
  const editPwd = () => {
    isEditPwd.value = !isEditPwd.value
  }
</script>
