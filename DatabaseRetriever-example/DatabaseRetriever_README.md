# DatabaseRetriever-example
DatabaseRetriever 这个类是一个实验性的类, 后续可能会有比较大的变更, 
官方不建议在生产中使用, 但是可以参考这个思路实现一个自己的DatabaseRetriever。
## 主要思路
1. 根据配置的数据源获取表结构信息
2. 构建检索增强器, 提示LLM模型回答sql, 根据sql触发检索信息再反馈给LLM
3. 最后得到大模型的包含数据的回答
> 需要注意 这里的SqlDatabaseContentRetriever的实现内部是直接把当前数据源的
> 所有库信息都加载到内存中, 提示给大模型, 这样会导致加载的很多无用的表

优化方式: 在构建`SqlDatabaseContentRetriever`时, 使用自定义的`generateDDL`方法, 具体查看: [AiConfig.java](src/main/java/com/example/databaseretrieverexample/config/AiConfig.java)
```java
@Bean
public ContentRetriever contentRetriever(DataSource dataSource, ChatLanguageModel chatLanguageModel) {
    List<String> tables = MetaUtil.getTables(dataSource);
    return SqlDatabaseContentRetriever.builder()
            .dataSource(dataSource)
            .databaseStructure(CollUtil.isEmpty(tables) ? null : generateDDL(dataSource, tables))
            .chatLanguageModel(chatLanguageModel)
            .build();
}
```
