package com.dazhuangyuan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dazhuangyuan.entity.AdmissionData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdmissionDataMapper extends BaseMapper<AdmissionData> {

    /**
     * 查询院校历年录取数据（含院校和专业组信息）
     */
    List<Map<String, Object>> selectCollegeAdmissionWithDetail(
            @Param("collegeId") Long collegeId,
            @Param("province") String province,
            @Param("category") String category
    );
}
