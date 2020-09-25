package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cms/page")
public class CmsController implements CmsPageControllerApi {

    @Autowired
    private CmsPageService service;

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
       return service.post(pageId);
    }

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size,
                                        QueryPageRequest queryPageRequest) {

//        QueryResult<CmsPage> queryResult = new QueryResult<>();
//        List<CmsPage> list = new ArrayList<>();
//        CmsPage cmsPage = new CmsPage();
//        cmsPage.setPageName("测试页面");
//        list.add(cmsPage);
//        queryResult.setTotal(1);
//        queryResult.setList(list);
//        QueryResponseResult result = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        QueryResponseResult result = service.findList(page, size, queryPageRequest);
        return result;
    }

    @Override
    @PostMapping("/add")
    public CmsPageResult addPage(@RequestBody CmsPage cmsPage) {
        return service.addPage(cmsPage);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsPage queryById(@PathVariable("id") String id) {
        return service.queryById(id);
    }

    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult edit(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return service.update(id, cmsPage);
    }

    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {
        return service.delete(id);
    }
}
