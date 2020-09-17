package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository repository;

    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        //自定义查询
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        //设置精确匹配查询
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        if (page <= 1) {
            page = 1;
        }

        page = page - 1;

        if (size <= 10) {
            size = 10;
        }

        //分页条件
        PageRequest pageable = PageRequest.of(page, size);
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        Page<CmsPage> cmsPages = repository.findAll(example, pageable);

        //解析结果
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(cmsPages.getContent());
        queryResult.setTotal(cmsPages.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    public CmsPageResult addPage(CmsPage cmsPage) {
        //校验页面名称、站点Id、页面webpath唯一
        CmsPage page = repository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page == null) {
            //执行新增操作
            cmsPage.setPageId(null);
            CmsPage save = repository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, save);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    public CmsPage queryById(String id) {
        Optional<CmsPage> byId = repository.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;

    }

    public CmsPageResult update(String id, CmsPage cmsPage) {
        //查询id
        CmsPage one = this.queryById(id);
        if (one != null) {
            //更新站点id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            repository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS, one);
        }
        return new CmsPageResult(CommonCode.FAIL, null);

    }

    public ResponseResult delete(String id) {
        //根据id查找是不是存在
        CmsPage cmsPage = this.queryById(id);
        if (cmsPage != null) {
            repository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);

        }
        return new ResponseResult(CommonCode.FAIL);
    }
}
