# RAG-example
## 搭建 ElasticSearch 环境
### 新建文件夹

需要在宿主机上挂载配置文件与数据文件。

![](https://cdn.nlark.com/yuque/0/2023/png/28087079/1683566405458-adbd9da3-a31b-4db3-bbaf-0a696c1d163c.png)

```shell
mkdir -p /Users/louye/data/learn-data/elastic/config
mkdir -p /Users/louye/data/learn-data/elastic/data
```

### 修改配置文件

```shell
cd /Users/louye/data/learn-data/elastic/config
vim elasticsearch.yml
```

```yaml
network.host: 0.0.0.0   
network.bind_host: 0.0.0.0  #外网可访问

http.cors.enabled: true
http.cors.allow-origin: "*"
xpack.security.enabled: true # 这条配置表示开启xpack认证机制 spring boot连接使用
xpack.security.transport.ssl.enabled: false
```

> xpack.security 配置后，elasticsearch 需要账号密码使用，建议安排上。如果使用 springboot 查询，那一定要设置，否者会报错！

### 启动

```shell
docker run -p 9200:9200 --name elasticsearch --net elastic \
-e  "discovery.type=single-node" \
-e ES_JAVA_OPTS="-Xms1g -Xmx2g" \
-v /Users/louye/data/learn-data/elastic/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
-v /Users/louye/data/learn-data/elastic/data:/usr/share/elasticsearch/data \
-v /Users/louye/data/learn-data/elastic/plugins:/usr/share/elasticsearch/plugins \
-d elasticsearch:8.2.3
```

### 初始化密码

```shell
docker exec -it elasticsearch /bin/bash
bin/elasticsearch-setup-passwords interactive
#user: elastic
#password: 123456
```

## kibana配置
主要是为了可以方便看数据, 也可以不用装
### 启动异常

可以跳过

```shell
docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" -e "ELASTICSEARCH_USERNAME=elastic" -e "ELASTICSEARCH_PASSWORD=123456" kibana:8.2.3
```

提示不能用默认的Elastic账号否则报错:
![image.png](https://cdn.nlark.com/yuque/0/2023/png/28087079/1683569230357-22c0f33c-4e63-44d9-a707-649e6a69b9e6.png#averageHue=%23151415&clientId=ufab6af08-2168-4&from=paste&height=261&id=ua4bfef9f&originHeight=522&originWidth=2944&originalType=binary&ratio=2&rotation=0&showTitle=false&size=234843&status=done&style=none&taskId=u2c4a81b9-a09f-46e9-a4ea-361a92309ce&title=&width=1472)

### es新建账号

```shell
docker exec -it elasticsearch /bin/bash
bin/elasticsearch-users useradd logadmin
```

输入密码
![image.png](https://cdn.nlark.com/yuque/0/2024/png/28087079/1723225753567-fa212b90-a9d8-4ed3-a7e5-a00f0e5f1745.png#averageHue=%23161516&clientId=ud64a4014-8c64-4&from=paste&height=93&id=u51a2e33c&originHeight=186&originWidth=1512&originalType=binary&ratio=2&rotation=0&showTitle=false&size=47342&status=done&style=none&taskId=ubba7bd0f-9ea3-4e2f-9419-cb8e9040dd5&title=&width=756)

### 添加角色和授权

```shell
#增加授权：
#superuser能正常打开es的9200端口，kibana_system配置后才可以正常对接kb和es
bin/elasticsearch-users roles -a superuser logadmin
bin/elasticsearch-users roles -a kibana_system logadmin

#移除授权：
bin/elasticsearch-users roles -r kibana_admin logadmin

#查看授权：
bin/elasticsearch-users roles -v logadmin
```

![image.png](https://cdn.nlark.com/yuque/0/2024/png/28087079/1723225777712-64254247-92aa-45c3-a0d5-5a285ec2e3e1.png#averageHue=%23171617&clientId=ud64a4014-8c64-4&from=paste&height=96&id=ue79311aa&originHeight=192&originWidth=1694&originalType=binary&ratio=2&rotation=0&showTitle=false&size=94296&status=done&style=none&taskId=u3a07b66b-9880-421e-9528-62b6402e071&title=&width=847)

### 再次启动Kibana

```shell
docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" -e "ELASTICSEARCH_USERNAME=logadmin" -e "ELASTICSEARCH_PASSWORD=123456" kibana:8.2.3
```

查看日志提示密码必须是字符串类型:
![image.png](https://cdn.nlark.com/yuque/0/2023/png/28087079/1683569929206-33d95c2b-d253-4fde-a095-4c83893f1892.png#averageHue=%23151415&clientId=ufab6af08-2168-4&from=paste&height=225&id=u1c252d04&originHeight=450&originWidth=2930&originalType=binary&ratio=2&rotation=0&showTitle=false&size=185588&status=done&style=none&taskId=u31c149aa-d1ee-4780-9bd0-d712d4be3fe&title=&width=1465)
两种方式, 要么改密码改为字符串,不使用数字; 要么使用下面的格式,添加""的转义格式

```shell
docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" -e "ELASTICSEARCH_USERNAME=logadmin" -e "ELASTICSEARCH_PASSWORD=\"123456\"" kibana:8.2.3
```
## 使用示例
### 说明
用到的sql文件: [sql脚本](src/main/resources/sql/generate.sql)
### 具体实现场景
* 使用 `ElasticsearchEmbeddingStore` 做为向量存储库
* 基于 `CompressingQueryTransformer` 对历史回答进行压缩提问
* 动态根据用户 `MemoryId` 过滤知识库
* 动态上传文档根据 `MetaData` 标签归属到所属用户
* 历史对话持久化存储到 `MySQL` 中
* PDF 解析(持续完善中...)
* Excel 解析(持续完善中...)
### 使用 Elasticsearch 进行文档向量存储
pom.xml配置:  
需要注意的一点在当前0.33.0的版本中, langchian4j-elasticsearch-spring-boot-starter使用的elasticsearch-java版本有问题,
需要手动指定版本为8.9.0, 否则编译会有问题。
```xml
<dependencies>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchian4j-elasticsearch-spring-boot-starter</artifactId>
        <exclusions>
            <!-- 这里需要排除依赖, 使用下面的8.9.0版本 -->
            <exclusion>
                <groupId>co.elastic.clients</groupId>
                <artifactId>elasticsearch-java</artifactId>
            </exclusion>
        </exclusions>
        <version>0.33.0</version>
    </dependency>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
        <version>0.33.0</version>
    </dependency>
    
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>8.9.0</version>
    </dependency>
</dependencies>
```
`langchian4j-elasticsearch-spring-boot-starter` 会默认帮我们配置好`EmbeddingStore` 使用 `ElasticsearchEmbeddingStore`实现。
properties配置:
```properties
spring.data.elasticsearch.repositories.enabled=true
langchain4j.elasticsearch.server-url=${your-elasticsearch-server-url:http://localhost:9200}
langchain4j.elasticsearch.user-name=${your-elasticsearch-user-name:elastic}
langchain4j.elasticsearch.password=${your-elasticsearch-password:123456}
langchain4j.elasticsearch.dimension=384
```
### 配置 CompressingQueryTransformer
```java
    //用于压缩查询语句
    @Bean
    public CompressingQueryTransformer compressingQueryTransformer(ChatLanguageModel chatLanguageModel) {
        return new CompressingQueryTransformer(chatLanguageModel);
    }
```
### 配置检索过滤器
配置对应的检索增强器, 并添加对应的过滤逻辑:
```java
    @Bean
    public ContentRetriever filterContentRetriever(EmbeddingStore embeddingStore,
                                            EmbeddingModel embeddingModel) {
        Function<Query, Filter> filterByUserId =
                (query) -> metadataKey("userId").isEqualTo(query.metadata().chatMemoryId().toString());

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                // 动态过滤, 只检索MemoryId等于当前用户id的文档数据
                .dynamicFilter(filterByUserId)
                .build();
        return contentRetriever;
    }
```
在 `@AiService` 中指定对应的检索增强器 `contentRetriever = "filterContentRetriever"`, 这样可以对其他AiService进行隔离分别使用不同的检索增强器:
```java
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel", contentRetriever = "filterContentRetriever", chatMemoryProvider = "chatMemoryProvider")
public interface ChatWithFileService {

    @SystemMessage("""
            you are a file assistant, when user choose a file: {{file}},
            you can answer user's question about the file content.
            """)
    String chatWithFile(@MemoryId Long userId, @V("file") String file, @UserMessage String question);
}
```
### 历史对话持久化存储
继承 `ChatMemoryStore` 实现自己的存储逻辑, 比如使用 `MySQL` 存储:
```java
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
```
配置 `ChatMemoryProvider` 的bean, 在可以根据需要定义不同的 `ChatMemoryProvider`:
```java
    @Bean
    public ChatMemoryProvider chatMemoryProvider(PersistentChatMemoryStore persistentChatMemoryStore) {
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();
        return chatMemoryProvider;
    }
```
这样在 `@AiService` 可以指定具体Provider:
```java
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel", contentRetriever = "filterContentRetriever", chatMemoryProvider = "chatMemoryProvider")
public interface ChatWithFileService {

    @SystemMessage("""
            you are a file assistant, when user choose a file: {{file}},
            you can answer user's question about the file content.
            """)
    String chatWithFile(@MemoryId Long userId, @V("file") String file, @UserMessage String question);
}
```
















