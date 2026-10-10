<template>
  <ElCard class="search-card" shadow="never">
    <ElForm :model="formData" label-width="100px" @submit.prevent="handleSearch">
      <ElRow :gutter="20">
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="应用名称">
            <ElSelect
              v-model="formData.appId"
              placeholder="请选择应用名称"
              clearable
              filterable
              style="width: 100%"
            >
              <ElOption v-for="app in appList" :key="app.id" :label="app.name" :value="app.id" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="广告位名称">
            <ElInput
              v-model="formData.name"
              placeholder="请输入广告位名称"
              clearable
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="广告位ID">
            <ElInput
              v-model.number="formData.sspSlotId"
              placeholder="请输入广告位ID"
              clearable
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="操作系统">
            <ElSelect
              v-model="formData.osType"
              placeholder="请选择操作系统"
              clearable
              style="width: 100%"
            >
              <ElOption label="Android" :value="1" />
              <ElOption label="iOS" :value="2" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="广告场景">
            <ElSelect
              v-model="formData.adScene"
              placeholder="请选择广告场景"
              clearable
              style="width: 100%"
            >
              <ElOption label="原生信息流" :value="1" />
              <ElOption label="开屏" :value="2" />
              <ElOption label="插屏" :value="3" />
              <ElOption label="激励视频" :value="4" />
              <ElOption label="banner" :value="5" />
              <ElOption label="长视频" :value="6" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="日期范围">
            <ElDatePicker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow>
        <ElCol :span="24" class="text-right">
          <ElSpace>
            <ElButton type="primary" native-type="submit" v-ripple>查询</ElButton>
            <ElButton @click="handleReset" v-ripple>重置</ElButton>
          </ElSpace>
        </ElCol>
      </ElRow>
    </ElForm>
  </ElCard>
</template>

<script setup lang="ts">
  import { fetchGetAppListByMediaId } from '@/api/media-manage'

  defineOptions({ name: 'DataSearch' })

  interface Props {
    modelValue: {
      month: string
      appId?: number
      name?: string
      sspSlotId?: number
      osType?: number
      adScene?: number
      startDate?: string
      endDate?: string
    }
  }

  interface Emits {
    (e: 'update:modelValue', value: Props['modelValue']): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const appList = ref<Api.MediaManage.AppListItem[]>([])

  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const dateRange = computed({
    get: () => {
      if (formData.value.startDate && formData.value.endDate) {
        return [formData.value.startDate, formData.value.endDate]
      }
      return []
    },
    set: (value) => {
      if (value && value.length === 2) {
        formData.value.startDate = value[0]
        formData.value.endDate = value[1]
      } else {
        formData.value.startDate = undefined
        formData.value.endDate = undefined
      }
    }
  })

  // 鍔犺浇搴旂敤鍒楄〃
  const loadAppList = async () => {
    try {
      const userInfoStr = localStorage.getItem('media_user_info')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        if (userInfo && userInfo.id) {
          const response = await fetchGetAppListByMediaId(userInfo.id)
          appList.value = response || []
        }
      }
    } catch (error) {
      console.error('加载应用列表失败:', error)
    }
  }

  const handleSearch = () => {
    const params = {
      month: formData.value.month,
      appId: formData.value.appId,
      name: formData.value.name,
      sspSlotId: formData.value.sspSlotId,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate
    }
    emit('search', params)
  }

  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const handleReset = () => {
    const today = new Date()
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)

    formData.value.appId = undefined
    formData.value.name = undefined
    formData.value.sspSlotId = undefined
    formData.value.osType = undefined
    formData.value.adScene = undefined
    formData.value.startDate = formatDate(firstDayOfMonth)
    formData.value.endDate = formatDate(today)
    emit('reset')
  }

  onMounted(() => {
    loadAppList()
  })
</script>

<style scoped lang="scss">
  .search-card {
    margin-bottom: 16px;
  }
</style>
