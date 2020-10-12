package com.xuecheng.manage_course;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFeign {
    @Autowired
    private CmsPageClient cmsPageClient;

    @Test
    public void testFeign() {
        CmsPage cmsPage = cmsPageClient.queryById("5a92141cb00ffc5a448ff1a0");
        System.out.println(cmsPage);
    }

}
