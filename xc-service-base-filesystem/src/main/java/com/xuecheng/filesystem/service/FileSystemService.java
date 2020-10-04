package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONWriter;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@Service
public class FileSystemService {

    @Autowired
    private FileSystemRepository fileSystemRepository;
    @Autowired
    private FastFileStorageClient storageClient;

    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata) {

        //上传文件,得到文件id
        String toFastDfs = uploadToFastDfs(multipartFile);
        if (toFastDfs == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        String dst = toFastDfs;

        //写入mongodb
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(dst);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFilePath(dst);
        fileSystem.setFileType(multipartFile.getContentType());
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        if (StringUtils.isNotEmpty(metadata)){
            fileSystem.setMetadata(JSON.parseObject(metadata, HashMap.class));
        }

        FileSystem saved = fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,saved);
    }


    /**
     * 上传文件到fastdfs
     * description
     *
     * @param multipartFile 文件
     * @return java.lang.String 文件存储服务器路径
     */
    private String uploadToFastDfs(MultipartFile multipartFile) {
        //检查文件合法性
        //检验文件名
        //检验后缀
        //检验大小
        if (multipartFile.isEmpty() ||
                multipartFile.getSize() == 0 ||
                StringUtils.isBlank(multipartFile.getName())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //上传文件
        try {
            StorePath storePath = storageClient.uploadFile(multipartFile.getInputStream(),
                    multipartFile.getSize(),
                    StringUtils.substringAfterLast(multipartFile.getOriginalFilename(), "."),
                    null);
            return storePath.getFullPath();
        } catch (IOException e) {
//            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        return null;
    }
}
