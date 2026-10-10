<template>
  <div class="slot-page art-full-height">
    <SlotSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></SlotSearch>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple>新增广告位</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <SlotDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :slot-data="currentSlotData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetSlotList } from '@/api/media-manage'
  import SlotSearch from './modules/slot-search.vue'
  import SlotDialog from './modules/slot-dialog.vue'
  import { ElTag } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'MediaSlot' })

  type SlotListItem = Api.MediaManage.SlotListItem

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentSlotData = ref<Partial<SlotListItem>>({})

  const searchForm = ref({
    id: undefined as number | undefined,
    mediaId: undefined as number | undefined,
    appId: undefined as number | undefined,
    name: undefined,
    osType: undefined as number | undefined,
    adScene: undefined as number | undefined,
    enable: undefined as number | undefined
  })

  const ENABLE_CONFIG = {
    0: { type: 'danger' as const, text: '禁用' },
    1: { type: 'success' as const, text: '正常' },
    2: { type: 'warning' as const, text: '审核中' },
    3: { type: 'danger' as const, text: '拒绝' }
  } as const

  const getEnableConfig = (enable: number) =>
    ENABLE_CONFIG[enable as keyof typeof ENABLE_CONFIG] || { type: 'info' as const, text: '未知' }

  const AD_SCENE_DICT: Record<number, string> = {
    1: '原生信息流',
    2: '开屏',
    3: '插屏',
    4: '激励视频',
    5: 'banner',
    6: '长视频'
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchGetSlotList,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 60 },
        { prop: 'id', label: '广告位ID', width: 100 },
        { prop: 'name', label: '广告位名称', minWidth: 150 },
        {
          prop: 'appName',
          label: '应用名称',
          width: 120,
          formatter: (row) => row.appName || '-'
        },
        {
          prop: 'appOsTypeName',
          label: '系统',
          width: 80,
          formatter: (row) => {
            const osType = row.appOsType
            if (osType === 1) {
              return h(ElTag, { type: 'success', size: 'small' }, () => 'Android')
            } else if (osType === 2) {
              return h(ElTag, { type: 'primary', size: 'small' }, () => 'iOS')
            }
            return row.appOsTypeName || '-'
          }
        },
        {
          prop: 'adScene',
          label: '广告场景',
          width: 120,
          formatter: (row) => row.adSceneName || AD_SCENE_DICT[row.adScene] || '-'
        },
        {
          prop: 'enable',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const config = getEnableConfig(row.enable)
            return h(ElTag, { type: config.type }, () => config.text)
          }
        },
        { prop: 'createTime', label: '创建时间', width: 180 },
      ]
    }
  })

  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    getData()
  }

  const showDialog = (type: DialogType, row?: SlotListItem): void => {
    dialogType.value = type
    currentSlotData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = async () => {
    dialogVisible.value = false
    currentSlotData.value = {}
    refreshData()
  }

</script>
