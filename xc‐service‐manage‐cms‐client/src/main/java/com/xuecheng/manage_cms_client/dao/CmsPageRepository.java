package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

    CmsPage findByPageName(String pageName);
    //根据页面名称、站点Id、页面webpath唯一查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
