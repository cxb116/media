# 媒体数据查询接口文档

## 分页查询

请求地址：`GET /api/media/data/list`

认证：`Authorization: Bearer <token>`。未传 `mediaId` 时，后端从 Token 获取当前媒体 ID。

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| current | Integer | 否 | 页码，默认 `1` |
| size | Integer | 否 | 每页条数，默认 `10` |
| mediaId | Long | 否 | 媒体 ID |
| appId | Long | 否 | 应用 ID |
| name | String | 否 | 广告位名称，模糊查询 |
| sspSlotId | Long | 否 | 广告位 ID |
| osType | Integer | 否 | `1` Android，`2` iOS |
| adScene | Integer | 否 | 广告场景 |
| startDate | Long | 否 | 开始日期，格式 `yyyyMMdd` |
| endDate | Long | 否 | 结束日期，格式 `yyyyMMdd` |
| sortField | String | 否 | 排序字段，见下表 |
| sortOrder | String | 否 | `asc` 升序、`desc` 降序，默认 `desc` |

### 排序字段白名单

| sortField | 数据库字段 |
| --- | --- |
| date | `d.date` |
| reqPv | `d.req_pv` |
| retPv | `d.ret_pv` |
| showPv | `d.show_pv` |
| clickPv | `d.click_pv` |
| discard | `d.discard` |
| dplsuccPv | `d.dplsucc_pv` |
| completePv | `d.complete_pv` |
| installPv | `d.install_pv` |
| activatePv | `d.activate_pv` |
| spend | `d.spend` |

必须使用白名单映射，禁止直接拼接 `sortField`。未传排序字段时默认 `d.date DESC, d.id DESC`。

请求示例：

```http
GET /api/media/data/list?current=1&size=20&startDate=20260801&endDate=20260831&sortField=reqPv&sortOrder=desc
Authorization: Bearer <token>
```

响应 `data` 至少包含 `records`、`total`、`current`、`size`。`records` 中包含 `date`、`reqPv`、`retPv`、`showPv`、`clickPv`、`discard`、`dplsuccPv`、`completePv`、`installPv`、`activatePv`、`spend` 等字段。

## 汇总查询

请求地址：`GET /api/media/data/sum`

使用相同筛选参数，不需要 `current`、`size`、`sortField`、`sortOrder`。返回 `data.records` 汇总对象。
