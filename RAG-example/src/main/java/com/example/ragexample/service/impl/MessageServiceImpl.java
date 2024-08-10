package com.example.ragexample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ragexample.domain.Message;
import com.example.ragexample.service.MessageService;
import com.example.ragexample.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author louye
* @description 针对表【message(消息表)】的数据库操作Service实现
* @createDate 2024-08-10 13:37:54
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

}




