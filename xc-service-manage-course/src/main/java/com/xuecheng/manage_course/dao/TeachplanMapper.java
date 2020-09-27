package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeachplanMapper {
    //课程计划查询
   // @Select("SELECT a.id one_id,a.pname one_pname, b.id two_id, b.pname two_pname, c.id three_id, c.pname three_pname FROM `teachplan` a LEFT JOIN `teachplan` b ON b.parentid = a.id LEFT JOIN `teachplan` c ON c.parentid = b.id WHERE a.parentid = '0' AND a.courseid = #{courseId} ORDER BY a.orderby, b.orderby, c.orderby;")
    public TeachplanNode selectList(String courseId);
}
