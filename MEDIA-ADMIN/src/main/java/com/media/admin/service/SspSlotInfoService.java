package com.media.admin.service;

import com.media.admin.common.Result;
import com.media.admin.dto.*;
import com.media.admin.entity.SspSlotInfo;
import com.media.admin.repository.SspSlotInfoRepository;
import com.media.admin.mapper.SspSlotInfoMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SspSlotInfoService {

    @Autowired
    private SspSlotInfoRepository sspSlotInfoRepository;

    @Autowired
    private SspSlotInfoMapper sspSlotInfoMapper;

    @Autowired
    private com.media.admin.repository.SspAppRepository sspAppRepository;

    @Autowired
    private com.media.admin.repository.DspAdTypeRepository dspAdTypeRepository;

    @Autowired
    private com.media.admin.repository.DspAdSceneRepository dspAdSceneRepository;

    @Autowired
    private com.media.admin.repository.DspAdSizeRepository dspAdSizeRepository;

    public Result<Map<String, Object>> getPage(SlotPageRequest request) {
        Pageable pageable = PageRequest.of(request.getCurrent() - 1, request.getSize(), Sort.by(Sort.Direction.DESC, "createTime"));
        List<SspSlotInfo> content = sspSlotInfoMapper.selectPage(request.getId(), request.getMediaId(), request.getAppId(), request.getName(), request.getEnable(), request.getAdScene(), request.getOsType(), (int) pageable.getOffset(), pageable.getPageSize());
        Page<SspSlotInfo> page = new PageImpl<>(content, pageable, sspSlotInfoMapper.countPage(request.getId(), request.getMediaId(), request.getAppId(), request.getName(), request.getEnable(), request.getAdScene(), request.getOsType()));
        List<SlotPageResponse> records = page.getContent().stream().map(this::convertToResponse).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records); data.put("current", page.getNumber() + 1); data.put("size", page.getSize()); data.put("total", page.getTotalElements());
        return Result.success(data);
    }

    /* Legacy implementation retained below during migration. */
    private Result<Map<String, Object>> getPageLegacy(SlotPageRequest request) {
        SspSlotInfo probe = new SspSlotInfo();
        if (request.getId() != null) {
            probe.setId(request.getId());
        }
        if (request.getMediaId() != null) {
            probe.setMediaId(request.getMediaId());
        }
        if (request.getAppId() != null) {
            probe.setAppId(request.getAppId());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            probe.setName(request.getName());
        }

        // 仅查询当前租户且未逻辑删除的广告位
        probe.setTenantId(1);
        probe.setDeleted(0);
        // 只有当明确传递了 enable 参数时，才按 enable 过滤
        // 否则设置为 null，让 ExampleMatcher 忽略该字段
        if (request.getEnable() != null) {
            probe.setEnable(request.getEnable());
        } else {
            probe.setEnable(null);
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnoreNullValues();

        Example<SspSlotInfo> example = Example.of(probe, matcher);

        Pageable pageable = PageRequest.of(
                request.getCurrent() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<SspSlotInfo> page = sspSlotInfoRepository.findAll(example, pageable);

        List<SlotPageResponse> records = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("current", page.getNumber() + 1);
        data.put("size", page.getSize());
        data.put("total", page.getTotalElements());

        return Result.success(data);
    }

    public Result<SlotPageResponse> getById(Long id) {
        return sspSlotInfoRepository.findById(id)
                .map(slot -> Result.success(convertToResponse(slot)))
                .orElse(Result.error("广告位不存在"));
    }

    @Transactional
    public Result<SlotPageResponse> create(SlotCreateRequest request) {
        SspSlotInfo slot = new SspSlotInfo();
        BeanUtils.copyProperties(request, slot);
        slot.setEnable(2);
//        slot.setCreateBy(String.valueOf(request.getMediaId()));
        slot.setSspPayType(2);
        LocalDateTime now = LocalDateTime.now();
        slot.setCreateTime(now);
        slot.setUpdateTime(now);
        slot.setDeleted(0);
        slot.setTenantId(1);

        SspSlotInfo savedSlot = sspSlotInfoRepository.save(slot);
        return Result.success("创建成功", convertToResponse(savedSlot));
    }

    @Transactional
    public Result<SlotPageResponse> update(SlotUpdateRequest request) {
        return sspSlotInfoRepository.findById(request.getId())
                .map(slot -> {
                    if (request.getName() != null) {
                        slot.setName(request.getName());
                    }
                    if (request.getAppId() != null) {
                        slot.setAppId(request.getAppId());
                    }

                    if (request.getEnable() != null) {
                        slot.setEnable(request.getEnable());
                    }

                    slot.setUpdateTime(LocalDateTime.now());
                    SspSlotInfo updatedSlot = sspSlotInfoRepository.save(slot);
                    return Result.success("更新成功", convertToResponse(updatedSlot));
                })
                .orElse(Result.error("广告位不存在"));
    }

    @Transactional
    public Result<Void> delete(Long id) {
        if (!sspSlotInfoRepository.existsById(id)) {
            return Result.error("广告位不存在");
        }
        sspSlotInfoRepository.deleteById(id);
        return Result.success();
    }

    @Transactional
    public Result<Void> batchDelete(List<Long> ids) {
        List<SspSlotInfo> slots = sspSlotInfoRepository.findAllById(ids);
        sspSlotInfoRepository.deleteAll(slots);
        return Result.success();
    }

    public Result<List<SlotPageResponse>> listByAppId(Long appId, Long mediaId) {
        List<SspSlotInfo> slots = sspSlotInfoRepository.findByAppIdAndMediaId(appId, mediaId);
        List<SlotPageResponse> responses = slots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    public Result<List<SlotPageResponse>> listByAppId(Long appId) {
        List<SspSlotInfo> slots = sspSlotInfoRepository.findByAppId(appId);
        List<SlotPageResponse> responses = slots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    public Result<List<SlotPageResponse>> listByMediaId(Long mediaId) {
        List<SspSlotInfo> slots = sspSlotInfoRepository.findByMediaId(mediaId);
        List<SlotPageResponse> responses = slots.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    private SlotPageResponse convertToResponse(SspSlotInfo slot) {
        // 查询应用信息
        String appName = "-";
        Integer appOsType = null;
        String appOsTypeName = "-";

        if (slot.getAppId() != null) {
            java.util.Optional<com.media.admin.entity.SspApp> appOpt = sspAppRepository.findById(slot.getAppId());
            if (appOpt.isPresent()) {
                com.media.admin.entity.SspApp app = appOpt.get();
                appName = app.getName();
                appOsType = app.getOsType();
                appOsTypeName = getAppOsTypeName(app.getOsType());
            }
        }



        return SlotPageResponse.builder()
                .id(slot.getId())
                .mediaId(slot.getMediaId())
                .appId(slot.getAppId())
                .appName(appName)
                .appOsType(appOsType)
                .appOsTypeName(appOsTypeName)
                .name(slot.getName())
                .adScene(slot.getAdScene())
                .enable(slot.getEnable())
                .enableName(getEnableName(slot.getEnable()))
                .createTime(slot.getCreateTime())
                .updateTime(slot.getUpdateTime())

                .build();
    }

    private String getSspPayTypeName(Integer sspPayType) {
        if (sspPayType == null) return "-";
        switch (sspPayType) {
            case 1: return "分成";
            case 2: return "RTB";
            default: return "未知";
        }
    }

    private String getInteractionTypeName(Integer interactionType) {
        if (interactionType == null) return "-";
        StringBuilder names = new StringBuilder();
        if ((interactionType & 1) != 0) names.append("打开网页 ");
        if ((interactionType & 2) != 0) names.append("Deeplink ");
        if ((interactionType & 4) != 0) names.append("直接下载 ");
        if ((interactionType & 8) != 0) names.append("广点通 ");
        if ((interactionType & 16) != 0) names.append("小程序 ");
        if ((interactionType & 32) != 0) names.append("应用商店 ");
        if ((interactionType & 64) != 0) names.append("快应用 ");
        return names.length() > 0 ? names.toString().trim() : "无";
    }

    private String getEnableName(Integer enable) {
        if (enable == null) return "-";
        switch (enable) {
            case 1: return "正常";
            case 0: return "禁用";
            case 2: return "审核中";
            case 3: return "拒绝";
            default: return "未知";
        }
    }

    private String getAppOsTypeName(Integer osType) {
        if (osType == null) return "-";
        switch (osType) {
            case 1: return "Android";
            case 2: return "iOS";
            default: return "未知";
        }
    }
}
