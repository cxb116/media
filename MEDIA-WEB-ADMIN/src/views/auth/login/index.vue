<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('login.title') }}</h3>
          <p class="sub-title">{{ $t('login.subTitle') }}</p>
          <ElForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            :key="formKey"
            @keyup.enter="handleSubmit"
            style="margin-top: 25px"
          >
            <ElFormItem prop="account">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.username')"
                v-model.trim="formData.account"
              />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.password')"
                v-model.trim="formData.password"
                type="password"
                autocomplete="off"
                show-password
              />
            </ElFormItem>

            <!-- 推拽验证 -->
            <div class="relative pb-5 mt-6">
              <div
                class="relative z-[2] overflow-hidden select-none rounded-lg border border-transparent tad-300"
                :class="{ '!border-[#FFE4E4]': !isPassing && isClickPass }"
              >
                <ArtDragVerify
                  ref="dragVerify"
                  v-model:value="isPassing"
                  :text="$t('login.sliderText')"
                  textColor="var(--art-gray-700)"
                  :successText="$t('login.sliderSuccessText')"
                  progressBarBg="var(--main-color)"
                  :background="isDark ? '#26272F' : '#F1F1F4'"
                  handlerBg="var(--default-box-color)"
                />
              </div>
              <p
                class="absolute top-0 z-[1] px-px mt-2 text-xs text-[#f56c6c] tad-300"
                :class="{ 'translate-y-10': !isPassing && isClickPass }"
              >
                {{ $t('login.placeholder.slider') }}
              </p>
            </div>



            <div style="margin-top: 30px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                v-ripple
              >
                {{ $t('login.btnText') }}
              </ElButton>
            </div>

            <div class="mt-5 text-sm text-gray-600">
              <span>{{ $t('login.noAccount') }}</span>
              <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                $t('login.register')
              }}</RouterLink>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import AppConfig from '@/config'
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { fetchMediaLogin, fetchMediaSsoLogin } from '@/api/mediaAuth'
  import { ElNotification, type FormInstance, type FormRules } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'Login' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const { t, locale } = useI18n()
  const formKey = ref(0)

  watch(locale, () => {
    formKey.value++
  })

  const dragVerify = ref()
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const isPassing = ref(false)
  const isClickPass = ref(false)
  const systemName = AppConfig.systemInfo.name
  const formRef = ref<FormInstance>()
  const loading = ref(false)

  const formData = reactive({
    account: '',
    password: '',
    rememberPassword: true
  })

  const rules = computed<FormRules>(() => ({
    account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }))

  const completeLogin = async (data: any) => {
    const { token, enable } = data || {}
    if (!token) throw new Error('登录失败 - 未收到 token')
    if (enable === 0) throw new Error('账号已被禁用')
    if (enable === 2) throw new Error('账号正在审核中，请耐心等待')
    if (enable === 3) throw new Error('账号审核未通过，请联系管理员')

    userStore.setToken(token, '')
    userStore.setLoginStatus(true)
    localStorage.setItem('media_user_info', JSON.stringify(data))
    showLoginSuccessNotice(data.name || data.account)
    await router.push((route.query.redirect as string) || '/')
  }

  const decryptSsoCredentials = async (token: string) => {
    const secret = import.meta.env.VITE_MEDIA_SSO_SECRET
    if (!secret) throw new Error('未配置 SSO 密钥')
    const normalized = token.replace(/-/g, '+').replace(/_/g, '/')
    const binary = Uint8Array.from(atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')), c => c.charCodeAt(0))
    if (binary.length <= 12) throw new Error('SSO token 格式无效')
    const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(secret))
    const key = await crypto.subtle.importKey('raw', digest, 'AES-GCM', false, ['decrypt'])
    const payload = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: binary.slice(0, 12), tagLength: 128 }, key, binary.slice(12))
    return JSON.parse(new TextDecoder().decode(payload)) as { account: string; password: string }
  }

  const handleSsoLogin = async (ssoToken: string) => {
    loading.value = true
    try {
      // SSO 切换媒体账号时，先清除当前媒体账号的本地登录状态，避免沿用旧账号
      userStore.setToken('', '')
      userStore.setLoginStatus(false)
      localStorage.removeItem('media_user_info')
      const credentials = await decryptSsoCredentials(ssoToken)
      await completeLogin(await fetchMediaLogin(credentials))
    } catch (error: any) {
      ElNotification({ title: 'SSO 登录失败', message: error?.message || '登录票据无效或已过期', type: 'error' })
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    const ssoToken = route.query.token
    if (typeof ssoToken === 'string' && ssoToken) {
      // 读取后立即从地址栏移除票据，避免刷新或复制历史地址时重复提交。
      const { token: _token, ...queryWithoutToken } = route.query
      router.replace({ path: route.path, query: queryWithoutToken })
      handleSsoLogin(ssoToken)
    }
  })

  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      const valid = await formRef.value.validate()
      if (!valid) return

      if (!isPassing.value) {
        isClickPass.value = true
        return
      }

      loading.value = true

      const { account, password } = formData

      const data = await fetchMediaLogin({ account, password })

      await completeLogin(data)
    } catch (error: any) {
      ElNotification({
        title: '登录失败',
        message: error?.message || '账号或密码错误',
        type: 'error',
        duration: 3000,
        zIndex: 10000
      })
      console.error('[Login] Error:', error)
    } finally {
      loading.value = false
      resetDragVerify()
    }
  }

  const resetDragVerify = () => {
    dragVerify.value?.reset()
  }

  const showLoginSuccessNotice = (userName: string) => {
    setTimeout(() => {
      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `欢迎回来，${userName}！`
      })
    }, 1000)
  }
</script>

<style scoped>
  @import './style.css';
</style>
