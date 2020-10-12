package com.xuecheng.manage_cms;

import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.manage_cms.config.MongoConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;//GridFSBucket用于打开下载流对象


    @Test
    public void testStore() throws IOException {
        FileInputStream inputStream = FileUtils.openInputStream(new File("c:/course.ftl"));
        //mongodb自带gridfs,安装mongodb后可以直接上传
        ObjectId objectId = gridFsTemplate.store(inputStream, "course.ftl");
        System.out.println(objectId);//5f7e990f712b9647602454ed
    }

    @Test
    public void queryFile() throws IOException {
        String fileId = "5f69a3c9712b9635682810bc";
        //根据id查询文件
        GridFSFile gridFSFile =
                gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream =
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //获取流中的数据
       // String s = IOUtils.toString(gridFsResource.getInputStream(), "utf‐8");
        InputStream inputStream = gridFsResource.getInputStream();
        String s = IOUtils.toString(inputStream);
        System.out.println(s);
    }
}
