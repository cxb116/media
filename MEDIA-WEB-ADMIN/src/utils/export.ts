/**
 * Excel导出工具函数
 *
 * 使用 xlsx 和 file-saver 库实现Excel文件导出功能
 */

import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

/**
 * 导出数据为Excel文件
 *
 * @param data - 要导出的数据数组
 * @param filename - 导出的文件名（不含扩展名）
 * @param sheetName - 工作表名称，默认为 'Sheet1'
 */
export function exportToExcel<T = any>(
  data: T[],
  filename: string,
  sheetName: string = 'Sheet1'
): void {
  if (!data || data.length === 0) {
    console.warn('导出数据为空')
    return
  }

  try {
    // 1. 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(data)
    const workbook = XLSX.utils.book_new()

    // 2. 添加工作表
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)

    // 3. 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })

    // 4. 创建Blob并保存文件
    const blob = new Blob([excelBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    // 添加文件扩展名
    const fullFilename = filename.endsWith('.xlsx') ? filename : `${filename}.xlsx`
    saveAs(blob, fullFilename)
  } catch (error) {
    console.error('导出Excel失败:', error)
    throw error
  }
}

/**
 * 导出数据为CSV文件
 *
 * @param data - 要导出的数据数组
 * @param filename - 导出的文件名（不含扩展名）
 */
export function exportToCSV<T = any>(data: T[], filename: string): void {
  if (!data || data.length === 0) {
    console.warn('导出数据为空')
    return
  }

  try {
    // 1. 创建工作表
    const worksheet = XLSX.utils.json_to_sheet(data)

    // 2. 转换为CSV格式
    const csv = XLSX.utils.sheet_to_csv(worksheet)

    // 3. 创建Blob并保存文件
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })

    // 添加文件扩展名
    const fullFilename = filename.endsWith('.csv') ? filename : `${filename}.csv`
    saveAs(blob, fullFilename)
  } catch (error) {
    console.error('导出CSV失败:', error)
    throw error
  }
}

/**
 * 格式化数据用于导出
 * 将后端返回的数据转换为更适合Excel显示的格式
 *
 * @param records - 原始数据记录
 * @returns 格式化后的数据
 */
export function formatDataForExport(records: Api.MediaManage.DataListItem[]) {
  return records.map((item) => ({
    '日期': item.dateStr || '-',
    '应用名称': item.appName || '-',
    '广告位名称': item.slotName || '-',
    '广告位': item.sspSlotId || '-',
    '请求': item.reqPv || 0,
    '请求用户': item.reqUv || 0,
    '请求丢弃率(%)': item.reqPv && item.reqPv > 0 ? ((item.discard / item.reqPv) * 100).toFixed(2) : '0.00',
    '填充率(%)': item.reqPv && item.reqPv > 0 ? ((item.retPv / item.reqPv) * 100).toFixed(2) : '0.00',
    '返回': item.retPv || 0,
    '返回用户': item.retUv || 0,
    '展现率(%)': item.retPv && item.retPv > 0 ? ((item.showPv / item.retPv) * 100).toFixed(2) : '0.00',
    '展示': item.showPv || 0,
    '展示用户': item.showUv || 0,
    '点击': item.clickPv || 0,
    '点击用户': item.clickUv || 0,
    'CTR(%)': item.showPv && item.showPv > 0 ? ((item.clickPv / item.showPv) * 100).toFixed(2) : '0.00',
    '收入(元)': item.income ? (item.income / 100).toFixed(2) : '0.00'
  }))
}
