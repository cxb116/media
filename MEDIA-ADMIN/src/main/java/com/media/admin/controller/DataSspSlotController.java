package com.media.admin.controller;
import com.media.admin.common.Result;
import com.media.admin.dto.*;
import com.media.admin.mapper.DataSspSlotMapper;
import com.media.admin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
@RestController
@RequestMapping("/api/media/data")
public class DataSspSlotController {
    @Autowired private DataSspSlotMapper dataSspSlotMapper;
    @Autowired private JwtUtil jwtUtil;
    private Long getCurrentMediaId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            Long mediaId = jwtUtil.getMediaIdFromToken(token.substring(7));
            if (mediaId != null) return mediaId;
        }
        throw new RuntimeException("未登录或 Token 无效");
    }
    @GetMapping("/list")
    public Result<Map<String,Object>> getList(DataSspSlotPageRequest request, HttpServletRequest httpRequest) {
        if (request.getMediaId() == null) request.setMediaId(getCurrentMediaId(httpRequest));
        if (request.getCurrent() == null || request.getCurrent() < 1) request.setCurrent(1);
        if (request.getSize() == null || request.getSize() < 1) request.setSize(10);
        List<DataSspSlotPageResponse> records = dataSspSlotMapper.getDataSspSlotPage(request);
        Map<String,Object> data = new HashMap<>(); data.put("records", records); data.put("total", dataSspSlotMapper.countDataSspSlot(request)); data.put("current", request.getCurrent()); data.put("size", request.getSize());
        return Result.success(data);
    }
    @GetMapping("/sum")
    public Result<Map<String,Object>> getListSum(DataSspSlotPageRequest request, HttpServletRequest httpRequest) {
        if (request.getMediaId() == null) request.setMediaId(getCurrentMediaId(httpRequest));
        Map<String,Object> data = new HashMap<>(); data.put("records", dataSspSlotMapper.getDataSspSlotSum(request)); return Result.success(data);
    }
}
