package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    //分页查找媒体资源
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        //检查参数
        if (page<=0){
            page = 1;
        }
        page = page -1;
        if (size<=0){
            size = 10;
        }
        if (queryMediaFileRequest==null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }

        //构建查询条件
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setFileStatus(queryMediaFileRequest.getProcessStatus());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<MediaFile> example = Example.of(mediaFile,matcher);
        //构建分页条件
        Pageable pageable = PageRequest.of(page,size);
        //执行查询
        Page<MediaFile> mediaFiles = mediaFileRepository.findAll(example, pageable);
        //获得结果
        long total = mediaFiles.getTotalElements();
        List<MediaFile> list = mediaFiles.getContent();

        //设置返回值
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setTotal(total);
        queryResult.setList(list);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
}
