package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryRepositoryTest {
    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void findSysDictionaryByDType(){
        String dType = "200";
        SysDictionary sysDictionaryByDType = sysDictionaryRepository.findSysDictionaryByDType(dType);
        System.out.println(sysDictionaryByDType);
    }
}