package com.xuecheng.manage_course.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;

    //查询课程计划列表
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     * 根据courseId获取课程根节点
     * description
     *
     * @param
     * @return
     */
   /* public String getTeachplanRoot(String courseId) {
        //先从课程大类查找
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();

        //取出课程计划根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,
                "0");
        if (teachplanList == null || teachplanList.size() == 0) {
            //新增一个根结点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setParentid("0");
            teachplanRoot.setGrade("1");//1级
            teachplanRoot.setStatus("0");//未发布
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }*/

    //查询课程根节点,查询不到就自动添加
    private String getTeachplanRoot(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CourseBase courseBase = optional.get();

        //查询课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            //查询不到,添加根节点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setParentid("0");
            teachplanRoot.setGrade("1");
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setStatus("0");
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        //返回根节点id
        return teachplanList.get(0).getId();
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //数据验证
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())
        ) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String courseId = teachplan.getCourseid();
        //获取页面传参的父节点id
        String parentId = teachplan.getParentid();
        if (StringUtils.isEmpty(parentId)) {
            //父节点为空,需要根据courseID获取父节点id
            parentId = getTeachplanRoot(courseId);
        }
        //获取父节点信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        Teachplan parentNode = optional.get();
        String parentGrade = parentNode.getGrade();
        //将页面提交的teachplan拷贝到teachplanNew中
        teachplan.setParentid(parentId);
        teachplan.setStatus("0");
        if (parentGrade.equals("1")) {
            teachplan.setGrade("2"); //级别,根据父节点级别设置
        } else if (parentGrade.equals("2")) {
            teachplan.setGrade("3");
        }
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分页查询我的课程
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        //验证参数
        if (page <= 1) {
            page = 1;
        }
        if (size <= 10) {
            size = 10;
        }
        String companyId = courseListRequest.getCompanyId();

        //设置分页条件
        PageHelper.startPage(page, size);
        //查询
        Page<CourseInfo> courseInfoPage = courseMapper.findCourseList(companyId);
        //判断结果
        if (CollectionUtils.isEmpty(courseInfoPage)) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //取得结果
        List<CourseInfo> list = courseInfoPage.getResult();
        long total = courseInfoPage.getTotal();

        //构建返回对象
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(total);
        QueryResponseResult<CourseInfo> responseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return responseResult;
    }

    public CourseBase getCourseBaseById(String courseId) {
        //判断参数
        if (StringUtils.isNotBlank(courseId)) {
            //查询
            Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
            if (!optional.isPresent()) {
                ExceptionCast.cast(CommonCode.FAIL);
            }
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseBase(CourseBase courseBase) {
        //参数判断
        if (courseBase == null) {
            //不存在
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询是不是存在coursebase
        CourseBase courseBaseById = this.getCourseBaseById(courseBase.getId());
        if (courseBaseById == null) {
            //不存在,则不能更新,报错
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //复制实体类属性
        BeanUtils.copyProperties(courseBase, courseBaseById);
        courseBaseRepository.save(courseBaseById);
        //构建返回值
        return new ResponseResult(CommonCode.SUCCESS);

    }

    public CourseMarket getCourseMarketById(String courseId) {
        //判断参数
        if (StringUtils.isBlank(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        //判断结果
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return optional.get();
    }

    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        //判断参数
        if (StringUtils.isBlank(id) || courseMarket == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //根据id查询是否存在
        Optional<CourseMarket> optional = courseMarketRepository.findById(id);
        if (!optional.isPresent()) {
            //不存在则新增
            courseMarketRepository.save(courseMarket);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //存在的话
        CourseMarket market = optional.get();
        BeanUtils.copyProperties(courseMarket, market);
        courseMarketRepository.save(market);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(pic)) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //先查询课程图片
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            coursePic = optional.get();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setPic(pic);
        coursePic.setCourseid(courseId);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePic(String courseId) {
        //参数判断
        if (StringUtils.isBlank(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //根据id查询
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //查询
        if (StringUtils.isBlank(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            //查到了删除
            coursePicRepository.deleteById(courseId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //查不到返回删除失败
        return new ResponseResult(CommonCode.FAIL);
    }
}
