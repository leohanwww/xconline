package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    //查找分类
    public CategoryNode findList() {
        CategoryNode node = categoryMapper.findList();
        if (node==null){
            ExceptionCast.cast(CommonCode.FAIL);
            return null;
        }
        return node;

    }
}
