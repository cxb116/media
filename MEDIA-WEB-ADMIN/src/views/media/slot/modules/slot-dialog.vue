<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加广告位' : '编辑广告位'"
    width="600px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="应用名称" prop="appId">
        <ElSelect v-model="formData.appId" placeholder="请选择应用名称" style="width: 100%" :loading="loadingApps">
          <ElOption
            v-for="app in appList"
            :key="app.id"
            :label="app.name"
            :value="app.id"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="广告场景" prop="adScene">
        <ElSelect
          v-model="formData.adScene"
          placeholder="请选择广告场景"
          style="width: 100%"
        >
          <ElOption
            v-for="scene in AD_SCENE_OPTIONS"
            :key="scene.value"
            :label="scene.label"
            :value="scene.value"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="广告位名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入广告位名称" />
      </ElFormItem>
      
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchCreateSlot, fetchUpdateSlot, fetchGetAppListByMediaId } from '@/api/media-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    slotData?: Partial<Api.MediaManage.SlotListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)
  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  // 应用相关
  const loadingApps = ref(false)
  const appList = ref<Api.MediaManage.AppListItem[]>([])

  const AD_SCENE_OPTIONS = [
    { value: 1, label: '原生信息流' },
    { value: 2, label: '开屏' },
    { value: 3, label: '插屏' },
    { value: 4, label: '激励视频' },
    { value: 5, label: 'banner' },
    { value: 6, label: '长视频' }
  ] as const

  // 获取当前登录的媒体用户信息
  const getCurrentMediaId = (): number | undefined => {
    try {
      const mediaUserInfo = localStorage.getItem('media_user_info')
      if (mediaUserInfo) {
        const userInfo = JSON.parse(mediaUserInfo)
        return userInfo.id
      }
    } catch (error) {
      console.error('Failed to get media user info:', error)
    }
    return undefined
  }

  const getCurrentMediaName = (): string => {
    try {
      const userInfo = JSON.parse(localStorage.getItem('media_user_info') || '{}')
      return userInfo.name || userInfo.account || ''
    } catch {
      return ''
    }
  }

  const generateSlotName = () => {
    if (dialogType.value !== 'add' || !formData.appId || !formData.adScene) return
    const app = appList.value.find((item) => item.id === formData.appId)
    if (!app) return
    const osName = app.osTypeName || (app.osType === 1 ? 'Android' : app.osType === 2 ? 'iOS' : '')
    const sceneName = AD_SCENE_OPTIONS.find((item) => item.value === formData.adScene)?.label || ''
    const mediaName = getCurrentMediaName()
    if (mediaName && app.name && osName && sceneName) {
      formData.name = `${mediaName}-${app.name}-${osName}-${sceneName}`
    }
  }

  // 加载应用列表
  const loadAppList = async () => {
    const mediaId = getCurrentMediaId()
    if (!mediaId) {
      ElMessage.warning('未获取到媒体用户信息')
      return
    }

    loadingApps.value = true
    try {
      const data = await fetchGetAppListByMediaId(mediaId)
      appList.value = data || []
    } catch (error) {
      console.error('Failed to load app list:', error)
      ElMessage.error('加载应用列表失败')
    } finally {
      loadingApps.value = false
    }
  }

  const formData = reactive({
    id: undefined as number | undefined,
    appId: undefined as number | undefined,
    name: '',
    adScene: undefined as number | undefined,
    adImage: '',
    interactionType: 0,
    remark: ''
  })

  watch(
    () => [formData.appId, formData.adScene, appList.value.length],
    generateSlotName
  )

  const interactionTypes = ref<number[]>([])

  watch(interactionTypes, (vals) => {
    formData.interactionType = vals.reduce((sum, val) => sum + val, 0)
  })

  const rules: FormRules = {
    appId: [{ required: true, message: '请选择应用', trigger: 'change' }],
    adScene: [{ required: true, message: '请选择广告场景', trigger: 'change' }],
    name: [
      { required: true, message: '请输入广告位名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ]
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.slotData
    const row = props.slotData

    Object.assign(formData, {
      id: isEdit && row ? row.id : undefined,
      appId: isEdit && row ? row.appId : undefined,
      name: isEdit && row ? row.name || '' : '',
      adScene: isEdit && row ? row.adScene || undefined : undefined,
      adImage: isEdit && row ? row.adImage || '' : '',
      interactionType: isEdit && row ? row.interactionType || 0 : 0,
      remark: isEdit && row ? row.remark || '' : ''
    })

    // 兼容列表接口未返回 appId 的场景，按应用名称兜底回填
    if (isEdit && !formData.appId && row?.appName) {
      const matchedApp = appList.value.find((app) => app.name === row.appName)
      if (matchedApp) {
        formData.appId = matchedApp.id
      }
    }

    const types: number[] = []
    let interaction = formData.interactionType
    if (interaction & 1) types.push(1)
    if (interaction & 2) types.push(2)
    if (interaction & 4) types.push(4)
    if (interaction & 8) types.push(8)
    if (interaction & 16) types.push(16)
    if (interaction & 32) types.push(32)
    if (interaction & 64) types.push(64)
    interactionTypes.value = types
  }

  watch(
    () => [props.visible, props.type, props.slotData],
    async ([visible]) => {
      if (visible) {
        await loadAppList()
        await initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitting.value = true
        try {
          const mediaId = getCurrentMediaId()

          if (!mediaId) {
            ElMessage.error('未获取到媒体用户信息，请重新登录')
            return
          }

          if (dialogType.value === 'add') {
            await fetchCreateSlot({
              mediaId: mediaId,
              appId: formData.appId!,
              name: formData.name,
              adScene: formData.adScene,
              adImage: formData.adImage,
              interactionType: formData.interactionType,
              remark: formData.remark
            })
            ElMessage.success('添加成功')
          } else {
            const updatePayload = {
              id: formData.id!,
              appId: formData.appId!,
              name: formData.name,
              adScene: formData.adScene,
              adImage: formData.adImage,
              interactionType: formData.interactionType,
              remark: formData.remark
            } as Api.MediaManage.SlotUpdateParams
            await fetchUpdateSlot(updatePayload)
            ElMessage.success('更新成功')
          }
          dialogVisible.value = false
          emit('submit')
        } finally {
          submitting.value = false
        }
      }
    })
  }
</script>
