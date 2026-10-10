<template>
  <div class="data-query-page art-full-height">
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
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetDayDataByMonth } from '@/api/media-manage'
  import DataSearch from './modules/data-search.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'
  import { exportToExcel, formatDataForExport } from '@/utils/export'

  defineOptions({ name: 'MediaDataQuery' })

  type DataListItem = Api.MediaManage.DataListItem

  // 获取当前月份，格式为 yyyyMM
  const getCurrentMonth = () => {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    return `${year}${month}`
  }

  const endDate = new Date()
  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - 6)
  const formatDate = (date: Date) => date.toISOString().slice(0, 10)
  const searchForm = ref({
    month: getCurrentMonth(),
    appId: undefined as number | undefined,
    sspSlotId: undefined as number | undefined,
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  })

  // 存储完整的查询结果（用于导出）
  const allData = ref<DataListItem[]>([])

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
        // 从 searchForm 获取月份
        const month = searchForm.value.month || getCurrentMonth()
        const response = await fetchGetDayDataByMonth({
          startDate: searchForm.value.startDate,
          endDate: searchForm.value.endDate,
          appId: searchForm.value.appId,
          sspSlotId: searchForm.value.sspSlotId
        })

        // 保存完整数据用于导出
        if (response?.records) {
          allData.value = response.records
        }

        // 转换为标准分页格式
        return {
          records: response?.records || [],
          current: params.current || 1,
          size: params.size || 20,
          total: response?.total || 0
        }
      },
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 60 },
        {
          prop: 'dateStr',
          label: '日期',
          width: 120,
          formatter: (row: DataListItem) => row.dateStr || '-'
        },
        {
          prop: 'appName',
          label: '应用名称',
          width: 200,
          formatter: (row: DataListItem) => row.appName || '-'
        },
        {
          prop: 'sspSlotId',
          label: '媒体广告位',
          minWidth: 150,
          formatter: (row: DataListItem) => row.sspSlotId || '-'
        },
        {
          prop: 'slotName',
          label: '广告位名称',
          minWidth: 200,
          formatter: (row: DataListItem) => row.slotName || '-'
        },
        {
          prop: 'reqPv',
          label: '请求(次数)',
          width: 120,
          formatter: (row: DataListItem) => formatNumber(row.reqPv)
        },
        {
          prop: 'discard',
          label: '请求丢弃(次数)',
          width: 120,
          formatter: (row: DataListItem) => formatNumber(row.discard)
        },
        {
          prop: 'retPv',
          label: '返回PV(次数)',
          width: 120,
          formatter: (row: DataListItem) => formatNumber(row.retPv)
        },
        {
          prop: 'showPv',
          label: '展示(次数)',
          width: 120,
          formatter: (row: DataListItem) => formatNumber(row.showPv)
        },
        {
          prop: 'clickPv',
          label: '点击(次数)',
          width: 120,
          formatter: (row: DataListItem) => formatNumber(row.clickPv)
        },

        {
          prop: 'fillRate',
          label: '填充率(%)',
          width: 120,
          formatter: (row: DataListItem) => {
            if (row.fillRate === null || row.fillRate === undefined) return '0.00'
            return row.fillRate.toFixed(2)
          }
        },
        {
          prop: 'showRate',
          label: '展现率(%)',
          width: 120,
          formatter: (row: DataListItem) => {
            if (row.showRate === null || row.showRate === undefined) return '0.00'
            return row.showRate.toFixed(2)
          }
        },

        {
          prop: 'ctr',
          label: 'CTR(%)',
          width: 120,
          formatter: (row: DataListItem) => {
            if (!row.showPv || row.showPv === 0) return '0.00'
            const ctr = (row.clickPv / row.showPv) * 100
            return ctr.toFixed(2)
          }
        },
        {
          prop: 'ecpm',
          label: 'eCPM(元)',
          width: 120,
          formatter: (row: DataListItem) => {
            if (row.ecpm === null || row.ecpm === undefined) return '0.00'
            return (row.ecpm / 10).toFixed(2)
          }
        },
        // {
        //   prop: 'ecpm',
        //   label: 'eCPM(元)',
        //   width: 120,
        //   formatter: (row) => {
        //     if (row.ecpm === null || row.ecpm === undefined) return '0.00'
        //     return row.ecpm.toFixed(2)
        //   }
        // },
        {
          prop: 'spend',
          label: '收入(元)',
          width: 120,
          formatter: (row: DataListItem) => {
            if (!row.spend) return '0.00'
            return (row.spend / 1000).toFixed(2)
          }
        },
      ]
    }
  })

  const formatNumber = (num: number | undefined | null) => {
    if (num === undefined || num === null) return '0'
    return num.toLocaleString()
  }

  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    getData()
  }

  const exportData = () => {
    if (!allData.value || allData.value.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }

    ElMessageBox.confirm(
      `确定要导出 ${allData.value.length} 条数据吗？`,
      '导出数据',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      try {
        // 格式化数据
        const formattedData = formatDataForExport(allData.value)

        // 生成文件名：数据报表_20260317.xlsx
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const monthStr = searchForm.value.month || getCurrentMonth()
        const filename = `数据报表_${monthStr}_${dateStr}`

        // 导出Excel
        exportToExcel(formattedData, filename, '数据报表')

        ElMessage.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败，请重试')
      }
    }).catch(() => {
      // 用户取消导出
    })
  }
</script>

<style scoped lang="scss">

</style>
