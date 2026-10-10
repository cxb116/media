<template>
  <div class="art-full-height">
    <DataSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></DataSearch>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="exportData" v-ripple>导出数据</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        show-summary
        :summary-method="getSummaryData"
        @sort-change="handleSortChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetDataList, fetchGetDataSum } from '@/api/media-manage'
  import DataSearch from './modules/data-search.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import { exportToExcel } from '@/utils/export'

  defineOptions({ name: 'MediaDataQuery' })

  console.log('=== 鏁版嵁鏌ヨ椤甸潰宸插姞杞?===')

  type DataListItem = Api.MediaManage.DataListItem
  type DataSummary = Partial<
    Pick<
      DataListItem,
      | 'reqPv'
      | 'showPv'
      | 'clickPv'
      | 'discard'
      | 'retPv'
      | 'spend'
      | 'income'
      | 'dplsuccPv'
      | 'completePv'
      | 'installPv'
      | 'activatePv'
    >
  >

  // 鑾峰彇褰撳墠鏈堜唤锛屾牸寮忎负 yyyyMM
  const getCurrentMonth = () => {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    return `${year}${month}`
  }

  const endDate = new Date()
  const startDate = new Date(endDate.getFullYear(), endDate.getMonth(), 1)
  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  const searchForm = ref({
    month: getCurrentMonth(),
    appId: undefined as number | undefined,
    name: undefined as string | undefined,
    sspSlotId: undefined as number | undefined,
    osType: undefined as number | undefined,
    adScene: undefined as number | undefined,
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  })

  // 瀛樺偍瀹屾暣鐨勬煡璇㈢粨鏋滐紙鐢ㄤ簬瀵煎嚭锛?
  const allData = ref<DataListItem[]>([])
  const summaryData = ref<DataSummary>({})
  const sortState = reactive<{ sortField?: string; sortOrder?: 'asc' | 'desc' }>({})

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
      apiFn: async (params: any) => {
        // 浠?searchForm
        const userInfo = JSON.parse(localStorage.getItem('media_user_info') || '{}')
        const queryParams = {
          current: params.current || 1,
          size: params.size || 20,
          startDate: Number((searchForm.value.startDate || '').replace(/-/g, '')),
          endDate: Number((searchForm.value.endDate || '').replace(/-/g, '')),
          ...(userInfo.id ? { mediaId: userInfo.id } : {}),
          ...(searchForm.value.appId ? { appId: searchForm.value.appId } : {}),
          ...(searchForm.value.name ? { name: searchForm.value.name } : {}),
          ...(searchForm.value.sspSlotId ? { sspSlotId: searchForm.value.sspSlotId } : {}),
          ...(searchForm.value.osType ? { osType: searchForm.value.osType } : {}),
          ...(searchForm.value.adScene ? { adScene: searchForm.value.adScene } : {}),
          ...(sortState.sortField ? { sortField: sortState.sortField, sortOrder: sortState.sortOrder } : {})
        }
        const { current: _current, size: _size, ...summaryParams } = queryParams
        const [response, summary] = await Promise.all([
          fetchGetDataList(queryParams),
          fetchGetDataSum(summaryParams)
        ])
        summaryData.value = summary?.records || {}

        console.log('鏁版嵁鏌ヨ鎺ュ彛杩斿洖:', response)
        console.log('records:', response?.records)

        // 淇濆瓨瀹屾暣鏁版嵁鐢ㄤ簬瀵煎嚭
        if (response?.records) {
          allData.value = response.records
          console.log('allData 已赋值:', allData.value.length, '条')
        } else {
          console.warn('response.records 为空或不存在')
        }

        return response
      },
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'index', label: '#', width: 60 },
        {
          prop: 'date',
          label: '日期',
          width: 120,
          sortable: 'custom',
          formatter: (row) => {
            const value = String(row.date ?? row.dateStr ?? '')
            if (/^\d{8}$/.test(value)) {
              return `${value.slice(0, 4)}-${value.slice(4, 6)}-${value.slice(6, 8)}`
            }
            return value || '-'
          }
        },
        {
          prop: 'slotName',
          label: '广告位名称',
          minWidth: 280,
          formatter: (row) => row.slotName || '-'
        },
        {
          prop: 'sspSlotId',
          label: '广告位ID',
          width: 120,
          formatter: (row) => row.sspSlotId || '-'
        },
        {
          prop: 'appName',
          label: '应用名称',
          width: 150,
          formatter: (row) => row.appName || '-'
        },
        {
          prop: 'osType',
          label: '操作系统',
          width: 90,
          formatter: (row) => {
            if (row.osType === 1) return 'Android'
            if (row.osType === 2) return 'iOS'
            return row.osType || '-'
          }
        },
        {
          prop: 'adScene',
          label: '广告场景',
          width: 100,
          formatter: (row) => {
            const sceneMap: Record<number, string> = {
              1: '原生信息流',
              2: '开屏',
              3: '插屏',
              4: '激励视频',
              5: 'banner',
              6: '长视频'
            }
            return (row.adScene != null && sceneMap[row.adScene]) || row.adScene || '-'
          }
        },
        {
          prop: 'reqPv',
          label: '请求PV',
          width: 200,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.reqPv)
        },

        {
          prop: 'retPv',
          label: '返回PV',
          width: 200,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.retPv)
        },

        {
          prop: 'showPv',
          label: '展示PV',
          width: 200,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.showPv)
        },

        {
          prop: 'clickPv',
          label: '点击PV',
          width: 150,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.clickPv)
        },

        {
          prop: 'discard',
          label: '请求丢弃数',
          width: 130,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.discard)
        },
        {
          prop: 'fillRate',
          label: '填充率(%)',
          width: 110,
          formatter: (row) => {
            if (!row.reqPv || row.reqPv === 0) return '0.00'
            const fillRate = (row.retPv / row.reqPv) * 100
            return fillRate.toFixed(2)
          }
        },
        {
          prop: 'showRate',
          label: '展现率(%)',
          width: 110,
          formatter: (row) => {
            if (!row.retPv || row.retPv === 0) return '0.00'
            return (((row.showPv || 0) / row.retPv) * 100).toFixed(2)
          }
        },
        {
          prop: 'clickRate',
          label: '点击率(%)',
          width: 110,
          formatter: (row) => {
            if (!row.showPv || row.showPv === 0) return '0.00'
            return (((row.clickPv || 0) / row.showPv) * 100).toFixed(2)
          }
        },
        {
          prop: 'ecpm',
          label: 'eCPM',
          width: 100,
          formatter: (row) => {
            if (!row.showPv || row.showPv === 0) return '0.00'
            const ecpm = ((row.spend || 0) / row.showPv) * 1000 / 100 / 1000
            return ecpm.toFixed(2)
          }
        },

        {
          prop: 'dplsuccPv',
          label: '调起成功',
          width: 110,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.dplsuccPv)
        },
        {
          prop: 'completePv',
          label: '完成量',
          width: 100,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.completePv)
        },
        {
          prop: 'installPv',
          label: '安装量',
          width: 100,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.installPv)
        },
        {
          prop: 'activatePv',
          label: '激活量',
          width: 100,
          sortable: 'custom',
          formatter: (row) => formatNumber(row.activatePv)
        },

        {
          prop: 'spend',
          label: '收益(元)',
          width: 120,
          sortable: 'custom',
          formatter: (row) => {
            return (row.spend / 100000).toFixed(2)
          }
        }
      ]
    }
  })

  const formatNumber = (num: number | undefined | null) => {
    if (num === undefined || num === null) return '0'
    return num.toLocaleString()
  }

  const getSummaryData = ({ columns: tableColumns }: { columns: Array<{ property?: string }> }) => {
    const summary = summaryData.value
    const rate = (numerator?: number, denominator?: number) =>
      denominator ? (((numerator || 0) / denominator) * 100).toFixed(2) : '0.00'

    const values: Record<string, string> = {
      reqPv: formatNumber(summary.reqPv),
      retPv: formatNumber(summary.retPv),
      showPv: formatNumber(summary.showPv),
      clickPv: formatNumber(summary.clickPv),
      discard: formatNumber(summary.discard),
      fillRate: rate(summary.retPv, summary.reqPv),
      showRate: rate(summary.showPv, summary.retPv),
      clickRate: rate(summary.clickPv, summary.showPv),
      ecpm: summary.showPv ? (((summary.spend || 0) / summary.showPv) * 1000 / 100 / 1000).toFixed(2) : '0.00',
      dplsuccPv: formatNumber(summary.dplsuccPv),
      completePv: formatNumber(summary.completePv),
      installPv: formatNumber(summary.installPv),
      activatePv: formatNumber(summary.activatePv),
      spend: ((summary.spend || 0) / 100000).toFixed(2)
    }

    return tableColumns.map((column, index) => {
      if (index === 0) return '总计'
      return column.property ? values[column.property] || '' : ''
    })
  }

  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    getData()
  }

  const handleSortChange = ({ prop, order }: { prop?: string; order?: 'ascending' | 'descending' | null }) => {
    sortState.sortField = prop || undefined
    sortState.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : undefined
    ;(searchParams as Record<string, unknown>).current = 1
    getData()
  }

  const buildRequestParams = (current: number, size: number) => {
    const userInfo = JSON.parse(localStorage.getItem('media_user_info') || '{}')
    return {
      current,
      size,
      startDate: Number((searchForm.value.startDate || '').replace(/-/g, '')),
      endDate: Number((searchForm.value.endDate || '').replace(/-/g, '')),
      ...(userInfo.id ? { mediaId: userInfo.id } : {}),
      ...(searchForm.value.appId ? { appId: searchForm.value.appId } : {}),
      ...(searchForm.value.name ? { name: searchForm.value.name } : {}),
      ...(searchForm.value.sspSlotId ? { sspSlotId: searchForm.value.sspSlotId } : {}),
      ...(searchForm.value.osType ? { osType: searchForm.value.osType } : {}),
      ...(searchForm.value.adScene ? { adScene: searchForm.value.adScene } : {}),
      ...(sortState.sortField ? { sortField: sortState.sortField, sortOrder: sortState.sortOrder } : {})
    }
  }

  const fetchAllData = async () => {
    const pageSize = 500
    const records: DataListItem[] = []
    let current = 1
    let total = Number.MAX_SAFE_INTEGER
    while (records.length < total) {
      const response = await fetchGetDataList(buildRequestParams(current, pageSize))
      const pageRecords = response?.records || []
      records.push(...pageRecords)
      total = Number(response?.total ?? records.length)
      if (pageRecords.length === 0 || pageRecords.length < pageSize) break
      current += 1
    }
    return records
  }

  const formatDateValue = (value: unknown) => {
    const date = String(value ?? '')
    return /^\d{8}$/.test(date)
      ? `${date.slice(0, 4)}-${date.slice(4, 6)}-${date.slice(6, 8)}`
      : date || '-'
  }

  const formatOsType = (osType?: number) => {
    if (osType === 1) return 'Android'
    if (osType === 2) return 'iOS'
    return osType || '-'
  }

  const formatAdScene = (adScene?: number) => {
    const sceneMap: Record<number, string> = {
      1: '原生信息流',
      2: '开屏',
      3: '插屏',
      4: '激励视频',
      5: 'banner',
      6: '长视频'
    }
    return (adScene && sceneMap[adScene]) || adScene || '-'
  }

  const formatTableDataForExport = (records: DataListItem[]) =>
    records.map((row) => ({
      日期: formatDateValue(row.date ?? row.dateStr),
      广告位名称: row.slotName || '-',
      广告位ID: row.sspSlotId || '-',
      应用名称: row.appName || '-',
      操作系统: formatOsType(row.osType),
      广告场景: formatAdScene(row.adScene),
      请求PV: row.reqPv || 0,
      返回PV: row.retPv || 0,
      展示PV: row.showPv || 0,
      点击PV: row.clickPv || 0,
      '请求丢弃率(%)': row.reqPv ? (((row.discard || 0) / row.reqPv) * 100).toFixed(2) : '0.00',
      '填充率(%)': row.reqPv ? (((row.retPv || 0) / row.reqPv) * 100).toFixed(2) : '0.00',
      '展现率(%)': row.retPv ? (((row.showPv || 0) / row.retPv) * 100).toFixed(2) : '0.00',
      '点击率(%)': row.showPv ? (((row.clickPv || 0) / row.showPv) * 100).toFixed(2) : '0.00',
      eCPM: row.reqPv ? (((row.income || 0) / row.reqPv) * 1000).toFixed(2) : '0.00',
      调起成功: row.dplsuccPv || 0,
      完成量: row.completePv || 0,
      安装量: row.installPv || 0,
      激活量: row.activatePv || 0,
      '收益(元)': ((row.spend || 0) / 100).toFixed(2)
    }))

  const exportData = async () => {
    ElMessageBox.confirm('将导出当前日期范围内的全部查询结果，是否继续？', '导出数据', {
      confirmButtonText: '导出',
      cancelButtonText: '取消',
      type: 'info'
    })
      .then(async () => {
        try {
          const exportRecords = await fetchAllData()
          if (exportRecords.length === 0) {
            ElMessage.warning('暂无数据可导出')
            return
          }
          const formattedData = formatTableDataForExport(exportRecords)

          // 鐢熸垚鏂囦欢鍚嶏細鏁版嵁鎶ヨ〃_20260317.xlsx
          const now = new Date()
          const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
          const monthStr = searchForm.value.month || getCurrentMonth()
          const filename = `数据报表_${monthStr}_${dateStr}`

          // 瀵煎嚭Excel
          exportToExcel(formattedData, filename, '数据报表')

          ElMessage.success(`导出成功，共 ${exportRecords.length} 条`)
        } catch (error) {
          console.error('导出失败:', error)
          ElMessage.error('')
        }
      })
      .catch(() => {
        // 鐢ㄦ埛鍙栨秷瀵煎嚭
      })
  }
</script>

<style scoped lang="scss">
  .data-query-page {
  }
</style>
