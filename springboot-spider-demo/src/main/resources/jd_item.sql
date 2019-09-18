CREATE TABLE `jd_item` (
	`id` BIGINT(10) NOT NULL auto_increment COMMENT '主键id',
	`spu` BIGINT(15) DEFAULT NULL COMMENT '商品集合id',
	`sku` BIGINT(15) DEFAULT NULL COMMENT '商品最小品类单元id',
	`title` VARCHAR(100) DEFAULT NULL COMMENT '商品标题',
	`price` BIGINT(10) DEFAULT NULL COMMENT '商品价格',
	`pic` VARCHAR(200) DEFAULT NULL COMMENT '商品图片',
	`url` VARCHAR(200) DEFAULT NULL COMMENT '商品详情地址',
	`created` datetime DEFAULT NULL COMMENT '创建时间',
	`uodated` datetime DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `sku` (`sku`) USING BTREE
 ) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='京东商品表';