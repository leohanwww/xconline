package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.google.common.io.ByteStreams;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.rabbitmq.tools.json.JSONUtil;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository pageRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private CmsConfigRepository configRepository;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    //根据id查询cms_config
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optional = configRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }


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
        Page<CmsPage> cmsPages = pageRepository.findAll(example, pageable);

        //解析结果
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(cmsPages.getContent());
        queryResult.setTotal(cmsPages.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

/*    public CmsPageResult addPage(CmsPage cmsPage) {
        //校验页面名称、站点Id、页面webpath唯一
        CmsPage page = repository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page == null) {
            //执行新增操作
            cmsPage.setPageId(null);
            CmsPage save = repository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, save);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }*/

    public CmsPageResult addPage(CmsPage cmsPage) {
        if (cmsPage == null) {
            //抛出非法参数异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //校验页面名称、站点Id、页面webpath唯一
        CmsPage page = pageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page != null) {
            //页面已经存在,抛出页面存在异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //执行新增操作
        cmsPage.setPageId(null);
        CmsPage save = pageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, save);

    }


    public CmsPage queryById(String id) {
        Optional<CmsPage> byId = pageRepository.findById(id);
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
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            pageRepository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS, one);
        }
        return new CmsPageResult(CommonCode.FAIL, null);

    }

    public ResponseResult delete(String id) {
        //根据id查找是不是存在
        CmsPage cmsPage = this.queryById(id);
        if (cmsPage != null) {
            pageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);

        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 页面静态化
     * 1、填写页面DataUrl
     * 在编辑cms页面信息界面填写DataUrl，将此字段保存到cms_page集合中。
     * 2、静态化程序获取页面的DataUrl
     * 3、静态化程序远程请求DataUrl获取数据模型。
     * 4、静态化程序获取页面的模板信息
     * 5、执行页面静态化
     */
    public String getPageHtml(String pageId) throws IOException {
        //获取页面数据model
        Map model = getModelByPageId(pageId);
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //获取模板template
        String template = getPageTemplate(pageId);
        if (StringUtils.isEmpty(template)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String generateHtml = generateHtml(model, template);
        return generateHtml;
    }

    //创建静态化页面
    private String generateHtml(Map model, String templateContent) {
        try {
            //创建freemarker配置对象
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template", templateContent);
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板内容
            Template template1 = configuration.getTemplate("template");
            //调用api进行静态化
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getPageTemplate(String pageId) {
        CmsPage cmsPage = this.queryById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面模板id
        String templateId = cmsPage.getTemplateId();
        if (templateId.isEmpty()) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查找模板
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //根据cmsTemp查询templateFileId,再后来去fs找到文件
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从gridFS取
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            try {
                //获取流中的数据
                InputStream inputStream = gridFsResource.getInputStream();
                // String content = IOUtils.toString(inputStream);
                String content = new String(ByteStreams.toByteArray(inputStream));
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    //获取数据模型
    private Map getModelByPageId(String pageId) {
        //取出页面信息
        CmsPage cmsPage = this.queryById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (dataUrl.isEmpty()) {
            //页面dataUrl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //用restTemplate远程请求dataUrl数据
        ResponseEntity<Map> entity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = entity.getBody();
        return body;
    }

    //页面发布
    public ResponseResult post(String pageId) {
        try {
            //执行页面静态化
            String pageHtml = this.getPageHtml(pageId);
            //存储到GridFS
            CmsPage cmsPage = saveHtml(pageId, pageHtml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //发送消息到mq
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存页面到GridFS
    private CmsPage saveHtml(String pageId, String htmlContent) {
        CmsPage cmsPage = this.queryById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //存储前先检查有没有,有的话先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        InputStream inputStream = null;
        Object objectId = null;
        try {
            //存储文件到fs
            inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toString());
        pageRepository.save(cmsPage);
        return cmsPage;
    }

    //向m发送消息
    public void sendPostPage(String pageId) {
        //得到页面信息
        CmsPage cmsPage = this.queryById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String siteId = cmsPage.getSiteId();
        HashMap<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        String msg = JSON.toJSONString(map);
        amqpTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, msg);
    }

    //保存页面,有就更新,没有就添加
    public CmsPageResult save(CmsPage cmsPage) {
        //查询
        CmsPage one = pageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                cmsPage.getSiteId(), cmsPage.getPagePhysicalPath());
        if (one != null) {
            //更新
            return this.update(one.getPageId(), cmsPage);
        }
        return this.addPage(cmsPage);
    }

    //一键发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息存储到cms_page中
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage cmsPage1 = save.getCmsPage();
        String pageId = cmsPage1.getPageId();

        //静态化,保存到gridfs,发mq消息
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //页面Url= cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        String siteId = cmsPage1.getSiteId();
        //查询站点
        CmsSite cmsSite = findCmsSiteById(siteId);
        String siteDomain = cmsSite.getSiteDomain();
        String siteWebPath = cmsSite.getSiteWebPath();
        String pageWebPath = cmsPage1.getPageWebPath();
        String pageName = cmsPage1.getPageName();
        //页面url
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;

        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    //根据id查询站点信息
    public CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
