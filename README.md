# Java高并发秒杀API
## 项目简介
使用SSM框架整合实现高并发秒杀项目，完成秒杀商品的列表展示、用户信息验证、秒杀商品详情与倒计时后秒杀接口暴露后对商品进行秒杀下单的基本流程。

## 技术
Jdk1.8、Spring、SpringMVC、Mybatis、Mysql、Redis、Restful

## 高并发优化分析

（红色部分标识可能出现高并发的地方）

![项目流程图](http://pr4gg6olg.bkt.clouddn.com/seckill4-1%E9%A1%B9%E7%9B%AE%E6%B5%81%E7%A8%8B.png)
## 优化点
* CDN部署，使用户在不直接访问后台服务器的情况下对静态资源的获取
* 秒杀接口隐藏，防止用户利用脚本恶意刷单
* 使用Redis缓存秒杀商品信息，减低Mysql服务器的压力
* 减少行级锁持有的时间，把事务中减库存的操作放在后面
* 使用存储过程，把简单逻辑在Mysql端执行，屏蔽网络延迟和GC影响
## 大型项目部署架构
![大型项目部署架构](http://pr4gg6olg.bkt.clouddn.com/seckill4-20%E7%B3%BB%E7%BB%9F%E9%83%A8%E7%BD%B2%E6%9E%B6%E6%9E%84.png)
