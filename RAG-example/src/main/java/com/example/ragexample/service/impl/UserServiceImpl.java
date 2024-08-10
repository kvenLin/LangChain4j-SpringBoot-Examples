package com.example.ragexample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ragexample.domain.User;
import com.example.ragexample.service.UserService;
import com.example.ragexample.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author louye
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-08-10 13:38:39
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




