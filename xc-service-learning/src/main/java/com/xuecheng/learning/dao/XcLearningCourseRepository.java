package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {

    //查询用户的选课记录
    public XcLearningCourse findByUserIdAndCourseId(String id,String courseId);
}
