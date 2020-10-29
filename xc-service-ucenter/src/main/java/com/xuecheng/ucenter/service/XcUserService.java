package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XcUserService {

    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    //根据账号查询用户信息
    public XcUser findUserByUsername(String username) {
        return xcUserRepository.findXcUserByUsername(username);
    }

    public XcUserExt getUserExt(String username) {
        //查询用户的基本信息
        XcUser xcUser = this.findUserByUsername(username);
        if (xcUser == null) {
            return null;
        }
        String userId = xcUser.getId();
        //根据用户id查询用户公司
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        String companyId = null;
        if (xcCompanyUser != null) {
            companyId = xcCompanyUser.getCompanyId();
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(companyId);
        //xcUserExt.setPermissions(null);
        return xcUserExt;
    }
}
