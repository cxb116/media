package com.media.admin.repository;

import com.media.admin.dto.DataSspSlotPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class DynamicTableRepository {

    private static final String DATA_TABLE = "data_ssp_slot_day";

    @Autowired
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<DataSspSlotPageResponse> queryDayTableByMonth(String month, Long mediaId, Long appId, Long sspSlotId) {
        return queryDynamicTable(DATA_TABLE, mediaId, appId, sspSlotId, null);
    }

    @SuppressWarnings("unchecked")
    public List<DataSspSlotPageResponse> queryDayTableByMonth(String month, List<Long> sspSlotIds) {
        return queryDynamicTableBySlotIds(DATA_TABLE, sspSlotIds);
    }

    @SuppressWarnings("unchecked")
    public List<DataSspSlotPageResponse> queryHourTableByMonth(String month, Long mediaId, Long appId, Long sspSlotId) {
        return queryDynamicTable(DATA_TABLE, mediaId, appId, sspSlotId, null);
    }

    @SuppressWarnings("unchecked")
    public List<DataSspSlotPageResponse> queryHourTableByMonth(String month, List<Long> sspSlotIds) {
        return queryDynamicTableBySlotIds(DATA_TABLE, sspSlotIds);
    }

    private List<DataSspSlotPageResponse> queryDynamicTable(String tableName, Long mediaId, Long appId, Long sspSlotId, List<Long> sspSlotIds) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE 1=1");

        // 如果提供了广告位ID列表，使用IN查询
        if (sspSlotIds != null && !sspSlotIds.isEmpty()) {
            sql.append(" AND ssp_slot_id IN (");
            for (int i = 0; i < sspSlotIds.size(); i++) {
                if (i > 0) sql.append(",");
                sql.append(":slotId").append(i);
            }
            sql.append(")");
        } else if (mediaId != null) {
            // 兼容旧逻辑：如果mediaId不为空，使用meida_id（注意表中的拼写错误）
            sql.append(" AND meida_id = :mediaId");
        }

        if (appId != null) {
            sql.append(" AND app_id = :appId");
        }
        if (sspSlotId != null) {
            sql.append(" AND ssp_slot_id = :sspSlotId");
        }

        try {
            Query query = entityManager.createNativeQuery(sql.toString());

            if (sspSlotIds != null && !sspSlotIds.isEmpty()) {
                for (int i = 0; i < sspSlotIds.size(); i++) {
                    query.setParameter("slotId" + i, sspSlotIds.get(i));
                }
            } else if (mediaId != null) {
                query.setParameter("mediaId", mediaId);
            }

            if (appId != null) {
                query.setParameter("appId", appId);
            }
            if (sspSlotId != null) {
                query.setParameter("sspSlotId", sspSlotId);
            }

            List<Object[]> results = query.getResultList();
            return mapToResponse(results);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<DataSspSlotPageResponse> queryDynamicTableBySlotIds(String tableName, List<Long> sspSlotIds) {
        if (sspSlotIds == null || sspSlotIds.isEmpty()) {
            log.warn("广告位ID列表为空，返回空结果");
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ssp_slot_id IN (");
        for (int i = 0; i < sspSlotIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append(":slotId").append(i);
        }
        sql.append(")");

        log.info("执行SQL查询: {}", sql.toString());
        log.info("查询参数 sspSlotIds: {}", sspSlotIds);

        try {
            Query query = entityManager.createNativeQuery(sql.toString());

            for (int i = 0; i < sspSlotIds.size(); i++) {
                query.setParameter("slotId" + i, sspSlotIds.get(i));
            }

            List<Object[]> results = query.getResultList();
            log.info("查询结果数量: {}", results.size());
            return mapToResponse(results);
        } catch (Exception e) {
            log.error("查询数据表失败: tableName={}, error={}", tableName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<DataSspSlotPageResponse> mapToResponse(List<Object[]> results) {
        List<DataSspSlotPageResponse> responses = new ArrayList<>();
        for (Object[] row : results) {
            Long reqPv = row[10] != null ? ((BigInteger) row[10]).longValue() : 0L;
            Long discard = row[12] != null ? ((BigInteger) row[12]).longValue() : 0L;
            Long retPv = row[13] != null ? ((BigInteger) row[13]).longValue() : 0L;
            Long showPv = row[6] != null ? ((BigInteger) row[6]).longValue() : 0L;
            Long income = row[16] != null ? ((BigInteger) row[16]).longValue() : 0L;

            // 计算请求丢弃率：discard/req_pv × 100%
            Double discardRate = null;
            if (reqPv != null && reqPv > 0) {
                discardRate = (discard != null ? discard : 0.0) * 100.0 / reqPv;
            }

            // 计算填充率：ret_pv/req_pv × 100%
            Double fillRate = null;
            if (reqPv != null && reqPv > 0) {
                fillRate = (retPv != null ? retPv : 0.0) * 100.0 / reqPv;
            }

            // 计算展现率：show_pv/ret_pv × 100%
            Double showRate = null;
            if (retPv != null && retPv > 0) {
                showRate = (showPv != null ? showPv : 0.0) * 100.0 / retPv;
            }

            // 计算eCPM：income/show_pv × 1000
            Double ecpm = null;
            if (showPv != null && showPv > 0) {
                ecpm = (income != null ? income : 0.0) * 1000.0 / showPv / 100.0;
            }

            DataSspSlotPageResponse response = DataSspSlotPageResponse.builder()
                    .id(((BigInteger) row[0]).longValue())
                    .mediaId(row[1] != null ? ((BigInteger) row[1]).longValue() : null)
                    .appId(row[2] != null ? ((BigInteger) row[2]).longValue() : null)
                    .sspSlotId(row[3] != null ? ((BigInteger) row[3]).longValue() : null)
                    .dspSlotId(row[4] != null ? ((BigInteger) row[4]).longValue() : null)
                    .dspSlotCode((String) row[5])
                    .showPv(showPv)
                    .showUv(row[7] != null ? ((BigInteger) row[7]).longValue() : 0L)
                    .clickPv(row[8] != null ? ((BigInteger) row[8]).longValue() : 0L)
                    .clickUv(row[9] != null ? ((BigInteger) row[9]).longValue() : 0L)
                    .reqPv(reqPv)
                    .reqUv(row[11] != null ? ((BigInteger) row[11]).longValue() : 0L)
                    .discard(discard)
                    .retPv(retPv)
                    .retUv(row[14] != null ? ((BigInteger) row[14]).longValue() : 0L)
                    .spend(row[15] != null ? ((BigInteger) row[15]).longValue() : 0L)
                    .income(income)
                    .discountClickPv(row[17] != null ? ((BigInteger) row[17]).longValue() : 0L)
                    .discountShowPv(row[18] != null ? ((BigInteger) row[18]).longValue() : 0L)
                    .dplsuccPv(row[19] != null ? ((BigInteger) row[19]).longValue() : 0L)
                    .completePv(row[20] != null ? ((BigInteger) row[20]).longValue() : 0L)
                    .installPv(row[21] != null ? ((BigInteger) row[21]).longValue() : 0L)
                    .activatePv(row[22] != null ? ((BigInteger) row[22]).longValue() : 0L)
                    .date(row[23] != null ? ((BigInteger) row[23]).longValue() : null)
                    .dateStr(formatDate(row[23] != null ? ((BigInteger) row[23]).longValue() : null))
                    .discardRate(discardRate)
                    .fillRate(fillRate)
                    .showRate(showRate)
                    .ecpm(ecpm)
                    .build();
            responses.add(response);
        }
        return responses;
    }

    private String formatDate(Long date) {
        if (date == null) return "-";
        String dateStr = String.valueOf(date);
        if (dateStr.length() == 8) {
            return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
        }
        return dateStr;
    }

    public boolean tableExists(String tableName) {
        try {
            Query query = entityManager.createNativeQuery("SHOW TABLES LIKE :tableName");
            query.setParameter("tableName", tableName);
            return query.getResultList().size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
