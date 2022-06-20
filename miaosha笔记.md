# 秒杀系统

## 一.系统设计和实现

拿到前端页面原型，首先考虑领域模型，而不是首先设计数据库。


### 交易问题
1.mysql原子操作使用 select ...for update 还是 update where条件判断

### 存在的问题

1.请求方式错误时，提示信息不友好：“查询商品信息失败，原因为{"timestamp":"2022-06-19T07:29:55.950+00:00","status":405,"error":"Method Not Allowed","path":"/item/listItem"}”

## 二.MYSQL

### 1.mysql优化：
尽可能让数据检索通过索引完成，避免 InnoDB 因为无法通过索引加行锁，而导致升级为表锁的情况。换句话说就是，多用行锁，少用表锁。
合理设计索引，尽量缩小锁的范围，加索引的时候尽量准确，避免造成不必要的锁定影响其他查询。
尽可能减少索引条件，避免间隙锁
尽量控制事务大小，减少锁定资源量和时间长度
尽量使用较低级别的事务隔离，减少 MySQL 因为事务隔离带来的成本。

### 2.行锁与表锁

InnoDB默认是行级别的锁，当有明确指定的主键时候，是行级锁。否则是表级别。

#for update的注意点
for update 仅适用于InnoDB，并且必须开启事务，在begin与commit之间才生效。

要测试for update的锁表情况，可以利用MySQL的Command Mode，开启二个视窗来做测试。

1、只根据主键进行查询，并且查询到数据，主键字段产生行锁。

begin;

select * from goods where id = 1 for update;

commit;

2、只根据主键进行查询，没有查询到数据，不产生锁。

begin;

select * from goods where id = 1 for update;

commit;



3、根据主键、非主键含索引（name）进行查询，并且查询到数据，主键字段产生行锁，name字段产生行锁。

begin;

select * from goods where id = 1 and name='prod11' for update;

commit;



4、根据主键、非主键含索引（name）进行查询，没有查询到数据，不产生锁。

begin;

select * from goods where id = 1 and name='prod12' for update;

commit;



5、根据主键、非主键不含索引（name）进行查询，并且查询到数据，如果其他线程按主键字段进行再次查询，则主键字段产生行锁，如果其他线程按非主键不含索引字段进行查询，则非主键不含索引字段产生表锁，如果其他线程按非主键含索引字段进行查询，则非主键含索引字段产生行锁，如果索引值是枚举类型，mysql也会进行表锁，这段话有点拗口，大家仔细理解一下。



begin;

select * from goods where id = 1 and name='prod11' for update;

commit;



6、根据主键、非主键不含索引（name）进行查询，没有查询到数据，不产生锁。



begin;

select * from goods where id = 1 and name='prod12' for update;

commit;



7、根据非主键含索引（name）进行查询，并且查询到数据，name字段产生行锁。



begin;

select * from goods where name='prod11' for update;

commit;



8、根据非主键含索引（name）进行查询，没有查询到数据，不产生锁。

begin;

select * from goods where name='prod11' for update;

commit;



9、根据非主键不含索引（stock）进行查询，并且查询到数据，stock字段产生表锁。

begin;

select * from goods where stock='1000' for update;

commit;



10、根据非主键不含索引（stock）进行查询，没有查询到数据，stock字段产生表锁。

begin;

select * from goods where stock='2000' for update;

commit;



11、只根据主键进行查询，查询条件为不等于，并且查询到数据，主键字段产生表锁。

begin;

select * from goods where id <> 1 for update;

commit;



12、只根据主键进行查询，查询条件为不等于，没有查询到数据，主键字段产生表锁。

begin;

select * from goods where id <> 1 for update;

commit;



13、只根据主键进行查询，查询条件为 like，并且查询到数据，主键字段产生表锁。

begin;

select * from goods where id like '1' for update;

commit;



14、只根据主键进行查询，查询条件为 like，没有查询到数据，主键字段产生表锁。



begin;

select * from goods where id like '1' for update;

commit;



测试环境

数据库版本：5.1.48-community

数据库引擎：InnoDB Supports transactions, row-level locking, and foreign keys

数据库隔离策略：REPEATABLE-READ（系统、会话）



总结

1、InnoDB行锁是通过给索引上的索引项加锁来实现的，只有通过索引条件检索数据，InnoDB才使用行级锁，否则，InnoDB将使用表锁。

2、由于MySQL的行锁是针对索引加的锁，不是针对记录加的锁，所以虽然是访问不同行的记录，但是如果是使用相同的索引键，是会出现锁冲突的。应用设计的时候要注意这一点。

3、当表有多个索引的时候，不同的事务可以使用不同的索引锁定不同的行，另外，不论是使用主键索引、唯一索引或普通索引，InnoDB都会使用行锁来对数据加锁。

4、即便在条件中使用了索引字段，但是否使用索引来检索数据是由MySQL通过判断不同执行计划的代价来决定的，如果MySQL认为全表扫描效率更高，比如对一些很小的表，它就不会使用索引，这种情况下InnoDB将使用表锁，而不是行锁。因此，在分析锁冲突时，别忘了检查SQL的执行计划，以确认是否真正使用了索引。

5、检索值的数据类型与索引字段不同，虽然MySQL能够进行数据类型转换，但却不会使用索引，从而导致InnoDB使用表锁。通过用explain检查两条SQL的执行计划，我们可以清楚地看到了这一点。

## 四.项目总结

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220620231844497.png" alt="image-20220620231844497" style="zoom:80%;" />

### 4.1.出错调试

- 先确认问题点：环境问题，ui展示问题，接口问题，服务问题，配置问题
- 调试：断点调试，日志调试
- 互联网寻找答案

### 4.2.资源

- Metronic框架：基于bootstrap的付费ui模板
- 视频代码内容：跟着老师的问题打一遍，融汇贯通原理

### 4.3.拓展思维

- 多商品、多库存、多活动模型怎么实现？

### 4.4.遗留问题

- 如何支撑亿级秒杀流量
- 如何发现容量问题
- 如何使得系统水平扩展
- 查询效率低下
- 活动开始前页面被疯狂刷新
- 库存行锁问题
- 下单操作多，缓慢
- 浪涌流量如何解决

