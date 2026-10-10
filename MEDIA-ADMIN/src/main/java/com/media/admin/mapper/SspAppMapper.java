package com.media.admin.mapper;
import com.media.admin.entity.SspApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface SspAppMapper {
  SspApp selectById(Long id); List<SspApp> selectByMediaId(Long mediaId); List<SspApp> selectByMediaIdAndEnable(@Param("mediaId") Long mediaId,@Param("enable") Integer enable);
  int insert(SspApp row); int update(SspApp row); int deleteById(Long id); int deleteByMediaId(Long mediaId);
}
