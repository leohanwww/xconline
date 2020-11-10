package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CategoryMapper categoryMapper;


    @Test
    public void testFindTeachplan(){
        String courseId = "4028e581617f945f01617f9dabc40000";
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        System.out.println(teachplanNode);
    }

    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }

    @Test
    public void testPageHelper(){
        PageHelper.startPage(1,10);
        //mybatis拦截器拦截请求的thread线程(从controller开启的线程),加入分页条件
        Page<CourseInfo> page = courseMapper.findCourseListPage(null);
        List<CourseInfo> result = page.getResult();
        System.out.println(result);
    }

    @Test
    public void testCategoryFindList(){
        CategoryNode list = categoryMapper.findList();
        System.out.println(list);

    }


}
