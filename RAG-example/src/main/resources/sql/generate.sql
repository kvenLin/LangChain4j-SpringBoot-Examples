DROP table IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `name`       varchar(255)              DEFAULT NULL COMMENT '用户名',
    `email`      varchar(255)              DEFAULT NULL COMMENT '邮箱',
    `password`   varchar(255)              DEFAULT NULL COMMENT '密码',
    `created_at` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 添加测试数据
INSERT INTO `user` (`id`, `name`, `email`, `password`)
VALUES (1, 'admin', 'admin@admin', '123456');
INSERT INTO `user` (`id`, `name`, `email`, `password`)
VALUES (2, 'test', 'test@test', '123456');

DROP table IF EXISTS `message`;
CREATE TABLE `message`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`    int(10)                   DEFAULT NULL COMMENT '用户ID',
    `content`    text                      DEFAULT NULL COMMENT '内容',
    `created_at` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';
