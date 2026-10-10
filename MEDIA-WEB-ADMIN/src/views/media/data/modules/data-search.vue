<template>
  <ElCard class="search-card" shadow="never">
    <ElForm :model="formData" label-width="100px" @submit.prevent="handleSearch">
      <ElRow :gutter="20">
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="媒体应用">
            <ElSelect v-model="formData.appId" placeholder="请选择媒体应用" clearable filterable style="width: 100%">
              <ElOption
                v-for="app in appList"
                :key="app.id"
                :label="app.name"
                :value="app.id"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="广告位">
            <ElSelect
              v-model="formData.sspSlotId"
              placeholder="全部广告位"
              clearable
              filterable
              style="width: 100%"
              :disabled="!formData.appId"
            >
              <ElOption
                v-for="slot in slotList"
                :key="slot.id"
                :label="slot.name"
                :value="slot.id"
              />
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
            <ElButton type="primary" native-type="submit" v-ripple>鏌ヨ</ElButton>
            <ElButton @click="handleReset" v-ripple>閲嶇疆</ElButton>
          </ElSpace>
        </ElCol>
      </ElRow>
    </ElForm>
  </ElCard>
</template>

<script setup lang="ts">
  import { fetchGetAppListByMediaId, fetchGetSlotListByAppId } from '@/api/media-manage'

  defineOptions({ name: 'DataSearch' })

  interface Props {
    modelValue: {
      month: string
      appId?: number
      sspSlotId?: number
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
  const slotList = ref<Api.MediaManage.SlotListItem[]>([])

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
      console.error('鍔犺浇搴旂敤鍒楄〃澶辫触:', error)
    }
  }

  // 加载广告位列表
  const loadSlotList = async (appId: number) => {
    try {
      const response = await fetchGetSlotListByAppId(appId)
      slotList.value = response || []
    } catch (error) {
      console.error('鍔犺浇骞垮憡浣嶅垪琛ㄥけ璐?', error)
    }
  }

  // 鐩戝惉搴旂敤閫夋嫨鍙樺寲
  watch(
    () => formData.value.appId,
    (newAppId) => {
      formData.value.sspSlotId = undefined
      slotList.value = []
      if (newAppId) {
        loadSlotList(newAppId)
      }
    }
  )

  const handleSearch = () => {
    const params = {
      month: formData.value.month,
      appId: formData.value.appId,
      sspSlotId: formData.value.sspSlotId,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate
    }
    emit('search', params)
  }

  const handleReset = () => {    formData.value.appId = undefined
    formData.value.sspSlotId = undefined
    formData.value.startDate = undefined
    formData.value.endDate = undefined
    slotList.value = []
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


