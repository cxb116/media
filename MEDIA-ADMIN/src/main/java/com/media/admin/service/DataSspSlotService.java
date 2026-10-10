package com.media.admin.service;

import com.media.admin.common.Result;
import com.media.admin.dto.*;
import com.media.admin.entity.DataSspSlot;
import com.media.admin.entity.SspSlotInfo;
import com.media.admin.repository.DataSspSlotRepository;
import com.media.admin.repository.DynamicTableRepository;
import com.media.admin.repository.SspSlotInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataSspSlotService {

    @Autowired
    private DataSspSlotRepository dataSspSlotRepository;

    @Autowired
    private DynamicTableRepository dynamicTableRepository;

    @Autowired
    private SspSlotInfoRepository sspSlotInfoRepository;

    @Autowired
    private com.media.admin.repository.SspAppRepository sspAppRepository;




}
