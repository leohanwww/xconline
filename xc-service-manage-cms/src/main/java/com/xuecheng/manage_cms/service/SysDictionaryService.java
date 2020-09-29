package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SysDictionaryService {
    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    //根据dtype查找参数词典
    public SysDictionary getSysDictionaryByType(String dType) {
        //参数判断
        if (StringUtils.isBlank(dType)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        SysDictionary sysDictionaryByDType = sysDictionaryRepository.findSysDictionaryByDType(dType);
        //判断结果
        if (sysDictionaryByDType==null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return sysDictionaryByDType;
    }
}
