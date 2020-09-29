package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SysDictionaryControllerApi;
import com.xuecheng.api.course.CategoryControllerApi;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/dictionary")
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    private SysDictionaryService sysDictionaryService;

    /** 
     * 根据DType查找sysDictionary
     * description
     * @param dType	
     * @return com.xuecheng.framework.domain.system.SysDictionary
    */
    @Override
    @GetMapping("/get/{dType}")
    public SysDictionary getSysDictionaryByType(@PathVariable("dType") String dType) {
        return sysDictionaryService.getSysDictionaryByType(dType);
    }
}
