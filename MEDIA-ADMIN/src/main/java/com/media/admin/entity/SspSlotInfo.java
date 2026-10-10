package com.media.admin.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * SSP广告位实体类
 * <p>
 * 用于管理SSP平台的广告位信息，包括广告位名称、类型、尺寸、结算方式等配置信息
 * </p>
 *
 * @author Media Admin Team
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "ssp_slot_info")
public class SspSlotInfo {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 媒体ID，关联ssp_media表
     */
    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    /**
     * 应用ID，关联ssp_app表
     */
    @Column(name = "app_id", nullable = false)
    private Long appId;

    /**
     * 广告位名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 广告位别名
     */
    @Column(name = "name_alise")
    private String nameAlise;

    /**
     * 广告场景ID
     */
    @Column(name = "ad_scene")
    private Long adScene;



    /**
     * SSP结算方式：1=分成，2=RTB
     */
    @Column(name = "ssp_pay_type", nullable = false)
    private Integer sspPayType;

    /**
     * SSP分成比例，范围0-100，单位为百分比
     */
    @Column(name = "ssp_deal_ratio")
    private Integer sspDealRatio;

    /**
     * 广告位宽度（像素）
     */
    @Column(name = "width")
    private Integer width;

    /**
     * 广告位高度（像素）
     */
    @Column(name = "height")
    private Integer height;

    /**
     * 广告位示例图片地址
     */
    @Column(name = "ad_image")
    private String adImage;



    /**
     * 启用状态：1=启用，2=审核中，3=禁用
     */
    @Column(name = "enable", nullable = false)
    private Integer enable = 2;

    /**
     * 创建人
     */
    @Column(name = "create_by", length = 64)
    private String createBy;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Column(name = "update_by", length = 64)
    private String updateBy;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "deleted", length = 500)
    private int deleted;

    @Column(name = "tenant_id", length = 500)
    private int tenantId;

}
