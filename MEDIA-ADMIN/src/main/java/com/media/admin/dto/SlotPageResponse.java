package com.media.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotPageResponse {
    private Long id;
    private Long mediaId;
    private Long appId;
    private String appName;
    private Integer appOsType;
    private String appOsTypeName;
    private String name;
    private Long adScene;
    private String adSceneName;
    private Integer enable;
    private String enableName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
}
