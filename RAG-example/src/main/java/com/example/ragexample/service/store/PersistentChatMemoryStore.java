package com.example.ragexample.service.store;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ragexample.domain.Message;
import com.example.ragexample.domain.User;
import com.example.ragexample.service.MessageService;
import com.example.ragexample.service.UserService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Resource
    private UserService userService;
    @Resource
    private MessageService messageService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        User user = userService.getById((Serializable) memoryId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }else {
            Message message = messageService.getOne(new QueryWrapper<Message>().eq("user_id", user.getId()));
            if (message == null) {
                return List.of();
            }
            String content = message.getContent();
            //to json list chat message
            return ChatMessageDeserializer.messagesFromJson(content);
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        User user = userService.getById((Serializable) memoryId);
        if (user != null) {
            Message message = messageService.getOne(new QueryWrapper<Message>().eq("user_id", user.getId()));
            if (message == null) {
                message = new Message();
                message.setUserId(user.getId());
            }
            if (CollUtil.isNotEmpty(messages)) {
                String content = ChatMessageSerializer.messagesToJson(messages);
                message.setContent(content);
                messageService.saveOrUpdate(message);
            }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        User user = userService.getById((Serializable) memoryId);
        if (user != null) {
            Message message = messageService.getOne(new QueryWrapper<Message>().eq("user_id", user.getId()));
            messageService.removeById(message.getId());
        }
    }
}
