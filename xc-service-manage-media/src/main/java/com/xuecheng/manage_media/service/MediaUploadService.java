package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String uploadLocation;

    //文件上传前的检验,检查文件是否存在
    public ResponseResult register(String fileMd5,
                                   String fileName,
                                   Long fileSize,
                                   String mimetype,
                                   String fileExt) {
        //检查文件在磁盘上存在
        //文件所属目录的路径
        String fileFolderPath = getFileFolderPath(fileMd5);
        String filePath = getFilePath(fileMd5, fileExt);
        //检查文件信息在mongodb中是否存在
        File file = new File(filePath);
        boolean exists = file.exists();
        //检查文件在mongodb中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()) {
            //文件存在,不用上传
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //其余都视为文件不存在,检查文件目录存在,不存在就创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    //分块检查
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //找到分块文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件
        File chunkFile = new File(chunkFileFolderPath + chunk);
        if (chunkFile.exists()) {
            return new CheckChunkResult(CommonCode.SUCCESS, true);
        } else {
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }
    }

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     *
     * @param fileMd5 文件md5值
     * @return 文件路径
     */
    private String getFileFolderPath(String fileMd5) {
        //md5的第一个字符+md5的第二个字符 = F:/develop/video/0/5/md5/ 目录的地址
        return uploadLocation + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + fileMd5 + '/';
    }

    private String getFilePath(String fileMd5, String fileExt) {
        //md5的第一个字符+md5的第二个字符 = F:/develop/video/0/5/md5/md5.ext 目录的地址
        return uploadLocation + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + fileMd5 + '/' + fileMd5
                + '.' + fileExt;
    }

    //得到文件目录相对路径，路径中去掉根目录
    private String getFileFolderRelativePath(String fileMd5,String fileExt){
        String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" +
                fileMd5 + "/";
        return filePath;
    }

    //块文件所属目录
    private String getChunkFileFolderPath(String fileMd5) {
        //md5的第一个字符+md5的第二个字符 = F:/develop/video/0/5/md5/md5.ext 目录的地址
        return getFileFolderPath(fileMd5) + "/" + "chunk" + "/";
    }

    //上传分块
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查分块目录存在,不存在就创建
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        String chunkFilePath = chunkFileFolderPath + chunk;

        //创建输入输出流
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //合并所有分块
        //获取块文件的路径
        String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkfileFolder = new File(chunkfileFolderPath);
        if (!chunkfileFolder.exists()) {
            chunkfileFolder.mkdirs();
        }
        //分块文件列表
        File[] files = chunkfileFolder.listFiles();

        File mergeFile = null;
        try {
            //创建合并目标文件
            mergeFile = new File(getFilePath(fileMd5, fileExt));
            if (mergeFile.exists()) {
                mergeFile.delete();
            }
            mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //执行合并,得到合并后的文件
        File afterMergeFile = mergeFile(Arrays.asList(files), mergeFile);
        if (afterMergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //校验md5和前端是否一样
        boolean isMd5Match = checkFileMd5(afterMergeFile, fileMd5);
        if (!isMd5Match){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将文件写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+'.'+fileExt);
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //合并文件
    private File mergeFile(List<File> chunFileList, File mergeFile) {
        try {
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                mergeFile.createNewFile();
            }
            //d对块文件排序
            Collections.sort(chunFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });
            //开始合并
            byte[] b = new byte[1024];
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            for (File chunkFile : chunFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "rw");
                int len = -1;
                //一直读取chunkFile直到没得读
                while ((len = raf_read.read(b)) != -1) {
                    //边读边写入
                    raf_write.write(b, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private boolean checkFileMd5(File mergeFile, String md5) {
        if (mergeFile == null || StringUtils.isEmpty(md5)) {
            return false;
        }
        //进行md5校验
        FileInputStream mergeFileInputstream = null;
        try {
            mergeFileInputstream = new FileInputStream(mergeFile);
            //得到文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);
            //比较md5
            if (md5.equalsIgnoreCase(mergeFileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                mergeFileInputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
