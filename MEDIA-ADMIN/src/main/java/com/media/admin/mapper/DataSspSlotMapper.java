package com.media.admin.mapper;

import com.media.admin.dto.DataSspSlotPageRequest;
import com.media.admin.dto.DataSspSlotPageResponse;

import java.util.List;

public interface DataSspSlotMapper
{
    List<DataSspSlotPageResponse> getDataSspSlotPage(DataSspSlotPageRequest request);

    long countDataSspSlot(DataSspSlotPageRequest request);

    // 算出
    DataSspSlotPageResponse getDataSspSlotSum(DataSspSlotPageRequest request);
}
