package com.media.admin.mapper;

import com.media.admin.entity.SspMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SspMediaMapper {
    SspMedia selectById(@Param("id") Long id);
    SspMedia selectByAccount(@Param("account") String account);
    int countByAccount(@Param("account") String account);
    int insert(SspMedia media);
    int update(SspMedia media);
}
