package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CmsPageRepositoryTest {
    @Autowired
    private CmsPageRepository repository;

    @Test
    public void testFindAll(){
        List<CmsPage> all = repository.findAll();
        Assert.assertEquals(22l,all.size());
    }

    //分页查询
    @Test
    public void testFindByPage(){
        Pageable pageable = PageRequest.of(0,10);
        Page<CmsPage> page = repository.findAll(pageable);
        List<CmsPage> list = page.stream().collect(Collectors.toList());
        Assert.assertEquals(10,list.size());
    }

    //条件查询
    @Test
    public void FindByExample(){
        Pageable pageable = PageRequest.of(0,10);
        //条件值
        CmsPage cmsPage = new CmsPage();
       // cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //模板id条件
       // cmsPage.setTemplateId("5a962b52b00ffc514038faf7");
        cmsPage.setPageAliase("轮播");
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
       // matching.withMatcher("pageName", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> pages = repository.findAll(example, pageable);
        List<CmsPage> content = pages.getContent();
        System.out.println(content);
    }

    //修改
    @Test
    public void testUpdate(){
        //查询
        Optional<CmsPage> byId = repository.findById("5a751fab6abb5044e0d19ea1");
        if (byId.isPresent()){
            CmsPage cmsPage = byId.get();
            //设置
            cmsPage.setPageName("测试");
            //插入
            CmsPage save = repository.save(cmsPage);
        }
    }

    @Test
    public void findByPageName() {
        CmsPage byPageName = repository.findByPageName("index.html");
        System.out.println(byPageName.getPageName());
    }
}