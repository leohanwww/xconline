package com.xuecheng.manage_cms.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {
    @Autowired
    private CmsPageService pageService;

    @Test
    public void testGetPageHtml() throws IOException {
        String pageHtml = pageService.getPageHtml("5f685d0a712b960de890398b");
        System.out.println(pageHtml);
    }
}
