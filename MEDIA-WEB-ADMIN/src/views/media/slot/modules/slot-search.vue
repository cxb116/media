<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :is-expand="true"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { fetchGetAppListByMediaId } from '@/api/media-manage'

  interface Props {
    modelValue: Record<string, any>
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const appList = ref<Api.MediaManage.AppListItem[]>([])
  const loadingApps = ref(false)

  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const enableOptions = ref([
    { label: '正常', value: 1 },
    { label: '禁用', value: 0 },
    { label: '审核中', value: 2 },
    { label: '拒绝', value: 3 }
  ])

  const osTypeOptions = [
    { label: 'Android', value: 1 },
    { label: 'iOS', value: 2 }
  ]

  const sceneOptions = [
    { label: '原生信息流', value: 1 },
    { label: '开屏', value: 2 },
    { label: '插屏', value: 3 },
    { label: '激励视频', value: 4 },
    { label: 'banner', value: 5 },
    { label: '长视频', value: 6 }
  ]

  const formItems = computed(() => [
    {
      label: '应用名称',
      key: 'appId',
      type: 'select',
      props: {
        placeholder: '请选择应用名称',
        options: appList.value.map((app) => ({ label: app.name, value: app.id })),
        clearable: true,
        filterable: true,
        loading: loadingApps.value
      },
      labelWidth: '100px'
    },
    {
      label: '广告位ID',
      key: 'id',
      type: 'input',
      placeholder: '请输入广告位ID',
      clearable: true,
      labelWidth: '100px'
    },
    {
      label: '广告位名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入广告位名称',
      clearable: true,
      labelWidth: '120px'
    },
    {
      label: '操作系统',
      key: 'osType',
      type: 'select',
      props: {
        placeholder: '请选择操作系统',
        options: osTypeOptions,
        clearable: true
      }
    },
    {
      label: '广告场景',
      key: 'adScene',
      type: 'select',
      props: {
        placeholder: '请选择广告场景',
        options: sceneOptions,
        clearable: true
      }
    },
    {
      label: '状态',
      key: 'enable',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        options: enableOptions.value,
        clearable: true
      },
      labelWidth: '80px'
    }
  ])

  async function loadAppList() {
    try {
      const userInfo = JSON.parse(localStorage.getItem('media_user_info') || '{}')
      if (!userInfo.id) return

      loadingApps.value = true
      appList.value = (await fetchGetAppListByMediaId(userInfo.id)) || []
    } catch (error) {
      console.error('加载应用列表失败:', error)
    } finally {
      loadingApps.value = false
    }
  }

  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    emit('search', formData.value)
  }

  onMounted(loadAppList)
</script>
