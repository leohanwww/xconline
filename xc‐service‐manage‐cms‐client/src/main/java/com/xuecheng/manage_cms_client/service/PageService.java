package com.xuecheng.manage_cms_client.service;

import com.google.common.io.ByteStreams;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;

    //保存html页面到服务器的物理路径
    public void savePageToServerPath(String pageId) {
        CmsPage cmsPage = this.findCmsPageById(pageId);
        if (cmsPage == null) {
            LOGGER.error("cmsPage null pageId:", pageId);
            return;
        }
        String htmlFileId = cmsPage.getHtmlFileId();
        //从GridFs查询html文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if (inputStream == null) {
            LOGGER.error("getFileById inputStream is null fileId:", htmlFileId);
            return;
        }
        //保存html页面到服务器物理路径 页面物理路径=站点物理路径+页面物理路径+页面名称。
        String siteId = cmsPage.getSiteId();
        //查询站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        if (cmsSite == null) {
            LOGGER.error("findCmsSiteById site is null siteId:", siteId);
            return;
        }
        //站点物理路径
        String siteWebPath = cmsSite.getSitePhysicalPath();
        //页面物理路径
        String pagePhysicalPath = cmsPage.getPagePhysicalPath();
        //页面名称
        String pageName = cmsPage.getPageName();
        //页面真实的物理路径
        String pagePath = siteWebPath + pagePhysicalPath + pageName;
        //将页面写到服务器上
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //根据页码id查询CmsPage
    private CmsPage findCmsPageById(String pageId) {
        //获取html文件id,从CmsPage中获取
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        return optional.orElse(null);
    }

    //根据站点id得到站点信息
    private CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    //根据文件id从GridFs查询文件
    public InputStream getFileById(String fileId) {
        GridFSFile gridFSFile =
                gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream =
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        try {
            //获取流中的数据
            return gridFsResource.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
