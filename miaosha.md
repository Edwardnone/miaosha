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

## 三.项目总结

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220620231844497.png" alt="image-20220620231844497" style="zoom: 33%;" />

<center>项目架构图</center>

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

前端秒杀页面加载不同步问题

- 如何支撑亿级秒杀流量
- 如何发现容量问题
- 如何使得系统水平扩展
- 查询效率低下
- 活动开始前页面被疯狂刷新
- 库存行锁问题
- 下单操作多，缓慢
- 浪涌流量如何解决

## 四.云端部署

- 安装jdk，配置jdk环境变量

- 安装mariadb (centos7使用mariadb替代mysql)

- 导出导入数据库

- maven打包项目

- 上传项目jar包

- 编写deploy脚本 deploy.sh

  **nohup** 英文全称 no hang up（不挂起），用于在系统后台不挂断地运行命令，退出终端不会影响程序的运行。

  nohup 命令，在默认情况下（非重定向时），会输出一个名叫 nohup.out 的文件到当前目录下，如果当前目录的 nohup.out 文件不可写，输出重定向到 $HOME/nohup.out 文件中。

  ```shell
  nohup java -Xms400m -Xmx400m -XX:NewSize=200m -XX:MaxNewSize=200m -jar miaosha.jar --spring.config.addition-location=/var/www/miaosha/application.properties
  ```

## 五.jmeter压力测试

查看线程数量

```shell
ps -ef | grep java
#查看进程的线程数量
pstree -p 19299 | wc -l 
#查看cpu使用情况
top -H
```

![image-20220625151350232](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220625151350232.png)

load average 表示过去1分钟、5分钟、15分钟的运行进程队列中的平均进程数量（不包括：等待IO、主动wait、被kill的进程）

### 5.2.发现容量问题

#### 5.2.1.默认内嵌Tomcat配置（springboot 2.7.0）

C:\jar\m2\repository\org\springframework\boot\spring-boot-autoconfigure\2.7.0\spring-boot-autoconfigure-2.7.0.jar!\META-INF\spring-configuration-metadata.json

- server.tomcat.accept-count:等待队列长度，默认100

- server.tomcat.max-connections:最大可被连接数，默认8192
- server.tomcat.max-threads:最大工作线程数，默认200
- server.tomcat.min-spare-threads:最小工作线程数，默认10

- 默认配置下，连接超过8192后出现拒绝连接情况
- 默认配置下，触发的请求超过200+100后拒绝处理

上线前一定要将容器的配置、线程池的配置、连接数的配置调优，以保证再生产环境下是最优的。

![image-20220627142535785](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220627142535785.png)

#### 5.2.2.定制化内嵌Tomcat开发

- keepAliveTimeOut: 多少毫秒后不响应断开keepalive
- maxKeepAliveRequests：多少次请求后keepalive断开失效
- 使用WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> 定制化内嵌tomcat配置
- 

#### 5.2.3.单Web容器上限

- 线程数量：4核cpu 8G内存单进程调度线程数8000-1000以上后即花费巨大的时间在cpu调度上
- 等待队列长度：队列做缓冲池用，但也不能无限长，消耗内存，出队入队也耗cpu（一般1000-2000，再超过就要使用集群）

#### 5.2.4.Mysql数据库QPS容量问题

- 主键查询：千万级别数据 = 1-10ms
- 唯一索引查询：千万级别数据 = 10-100ms
- 非唯一索引查询：千万级别数据 = 100-1000ms
- 无索引：百万条数据 = 1000ms+

#### 5.2.5.Mysql数据库TPS容量问题

- 非插入更新删除操作：同查询
- 插入操作：1w ~ 10w tps （依赖配置优化，后续讲解）

##  六.分布式扩展

#### 6.3.1.单机容量问题，水平扩展

改进之前的部署结构：

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220627171559454.png" alt="image-20220627171559454" style="zoom:50%;" />

- 表象：单击cpu使用率增高，memory占用增加，网络带宽使用增加

- cpu us：用户空间的cpu使用情况（用户层代码）

- cpu sy：内核空间的cpu使用情况（系统调用）

- load average：1,5，15分钟load平均值，跟着核数系数，0代表通常，1台标打满，1+代表等待阻塞

- memery：free空闲内存，used使用内存。

  直接访问三合一数据库服务器（带宽20M）性能：

  <img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628101039758.png" alt="image-20220628101039758" style="zoom:150%;" />

  数据库服务器：cpu使用率：80%， load average>2

改进之后的部署结构：

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220627171703855.png" alt="image-20220627171703855" style="zoom:50%;" />



- mysql数据库开放远端连接

- 服务端水平对称部署

- 验证访问

  访问nginx反向代理服务器（25M带宽）

  ![image-20220628102018699](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628102018699.png)

  数据库服务器：cpu使用率：<20%，load average < 0.3

#### 6.3.2.nginx反向代理负载均衡

- 单击容量问题，水平扩展

- nginx反向代理

- 负载均衡配置

  

- 使用nginx作为web服务器

  - location节点path: 指定url映射key

  - location节点内容：root指定location path后对应的根路径，index指定默认的访问页
  - sbin/nginx -c conf/nginx.conf 启动
  - 修改配置后直接 sbin/nginx -s reload 无缝重启

- 使用nginx作为动静分离服务器

  - location节点path特定resources：静态资源路径 (nginx.conf)

    ```
    location /resources/ {
                alias /usr/local/openresty/nginx/html/resources/;
                index  index.html index.htm;
            }
    
    ```

  - location节点其他路径：动态资源用

- 使用nginx作为反向代理服务器

  - nginx.conf 设置upstream server

    ```nginx.conf
    upstream backend_server{
            server 172.17.16.3 weight=1;
            server 172.17.16.15 weight=1;
        }
    ```

    

  - nginx.conf  设置动态请求location为proxy pass路径

    ```
    location / {
                proxy_pass http://backend_server;
                proxy_set_header Host $http_host:$proxy_port;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            }
    ```

    

  - 开启tomcat access log验证(application.properties)

    ```properties
    server.tomcat.accesslog.enabled=true
    
    server.tomcat.accesslog.directory=/var/www/miaosha/tomcat
    
    server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D
    
    ```

    

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220627172324648.png" alt="image-20220627172324648" style="zoom:50%;" />

<center>修改后的服务器部署结构</center>

这里使用封装了nginx的openResty框架

**改进nginx反向代理服务器与应用服务器之间的连接，默认nginx使用http1.0，不保持连接**

改进：

```
upstream backend_server{
        server 172.17.16.3 weight=1;
        server 172.17.0.13 weight=1;
        keepalive 30;
    }



location / {
            proxy_pass http://backend_server;
            proxy_set_header Host $http_host:$proxy_port;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_http_version 1.1; #默认http1.0，改为http1.1协议
            proxy_set_header Connection ""; //默认为close；置Connection为空，表示用完之后不close
        }
```

改进后结果：

平均时延从300多降到了200多ms

![image-20220628104141400](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628104141400.png)

采用nginx反向代理，并设置了与后端upstream server的一个长连接，解决了单机容量的瓶颈。并且采用长连接的方式，保证了对应的分布式扩展网络连接之后所产生的一个网络建联的一个消耗，使用了keepalive方式解决了nginx和upstream server之间的网络连接消耗；使用了秒杀server自带的一个druid数据库连接池，解决了跟数据源连接时建联的一个连接消耗。

#### 6.3.3.nginx高性能原因

- epoll多路复用

  - java bio模型，阻塞进程式

    <img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628111926603.png" alt="image-20220628111926603" style="zoom:50%;" />

  - linux select模型，变更触发轮询查找，有1024数量上限

    <img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628112046416.png" alt="image-20220628112046416" style="zoom:50%;" />

  - epoll模型，变更触发回调直接读取，理论上没有上限（linux 2.6会把select变为epoll）用于实现Dubbo RPC的Netty框架就是基于epoll模型完成的多路复用机制

    <img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628112238287.png" alt="image-20220628112238287" style="zoom:50%;" />

- master worker进程模型

  <img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220628113223050.png" alt="image-20220628113223050" style="zoom:67%;" />

  每一个worker进程中只有一个线程（没有阻塞操作，有阻塞操作都要交给epoll处理）

- 协程机制

  - 依附于线程的内存模型，切换开销小（不需要cpu切换开销，只需要内存切换开销）
  - 遇阻塞及归还执行权，代码同步
  - 无需加锁

  

​	总结：

 - 依靠epoll多路复用机制，解决了IO阻塞回调通知的问题

 - 依靠master worker进程模型可以完成平滑地过度，平滑地重启，并且结合worker单线程模型结合epoll多路复用机制完成高效操作

 - 基于协程的机制，将每个用户的请求对应到线程的某一个协程中，在协程中使用epoll多路复用机制完成对应的一个同步调用的开发，完成高性能操作。

   Lua操作也是基于协程的一个脚本的操作方式，nginx的lua模块和原本的nginx协程机制是不谋而合的设计，因此做nginx开发很少使用c、c++，而是使用lua的协程机制。


#### 6.3.4.分布式会话

一般会话形式----->分布式会话

- 基于cookie传输sessionid：java tomcat容器session ---->   迁移到redis

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
  </dependency>
  <!-- https://mvnrepository.com/artifact/org.springframework.session/spring-session-data-redis -->
  <dependency>
      <groupId>org.springframework.session</groupId>
      <artifactId>spring-session-data-redis</artifactId>
      <version>2.7.0</version>
  </dependency>
  ```

  ```java
  @Component
  @EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
  public class RedisConfig {
  }
  ```

- 基于token传输类似sessionid：java代码session --------->   迁移到redis

  ```java
  //生成登录凭据（token）
  String token = UUID.randomUUID().toString().replace("-", "");
  //将登录凭据存入redis中
  redisTemplate.opsForValue().set(token, userModel, 1, TimeUnit.HOURS);
  ```

## 七.查询性能优化技术之多级缓存

### 7.1.本章目标

- 掌握多级缓存定义

- 掌握redis缓存，本地缓存

- 掌握热点nginx lua缓存

  

缓存设计

- 用快速存取设备，用内存

- 将缓存推到离用户最近的地方

  

多级缓存

 - redis缓存

 - 热点内存本地缓存

 - nginx proxy cache缓存

 - nginx lua缓存

   

### 7.2.Redis集中式缓存介绍

与mysql是完全对等的，只不过是使用了key-value形式存储数据，且数据易丢失。

模式：

- 单机版
- sentinal哨兵模式
- 集群cluster模式

### 7.3.商品详情页动态内容实现

```java
//首先读取redis缓存
ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
if (itemModel == null){
    itemModel = itemService.getItemById(id);
}
ItemVO itemVO = convertItemVOFromItemModel(itemModel);
//存入redis缓存
redisTemplate.opsForValue().set("item_" + id, itemModel);
redisTemplate.expire("item_" + id, 10, TimeUnit.MINUTES);
```

配置redis的key和value的序列化方式，设置jodaDateTime的序列化和反序列化方式，在json序列化方式中加入类的信息

```
@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        //指定要序列化的类
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(DateTime.class, new JodaDateTimeJsonSerializer());
        simpleModule.addDeserializer(DateTime.class, new JodaDateTimeJsonDeserializer());
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(simpleModule);
        //设置key的序列化方式
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        //设置value的序列化方式
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        return redisTemplate;
    }
}
```

优化后的查询性能：

<img src="C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220630083015630.png" alt="image-20220630083015630" style="zoom:150%;" />

### 7.4.本地热点缓存

- 热点数据

- 脏读不敏感

- 内存可控

  

Guava cache（本质上也是一个可并发的HashMap）

- 可控制的大小和超时时间
- 可配置的lru策略（最近最少访问的key优先被淘汰）
- 线程安全

引入依赖

```xml
<!-- https://mvnrepository.com/artifact/com.google.guava/guava 
是google想要重新定义java的一些基础封装类，不只是可以做本地缓存，是非常强大的生态，如限流模块，空指针optional-->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>31.1-jre</version>
</dependency>
```

```java
@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String,Object> commonCache = null;

    @PostConstruct
    public void init(){
        this.commonCache = CacheBuilder.newBuilder()
                //设置缓存容器的初始容量为10
                .initialCapacity(10)
                //设置缓存中最大存储容量，超过按照LRU规则移除缓存项
                .maximumSize(100)
                //设置过期时间为1分钟
                .expireAfterWrite(1, TimeUnit.MINUTES).build();
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }
}
```

引入本地缓存之后：平均访问耗时80ms，显著降低，且redis服务器毫无压力。

![image-20220630102757285](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220630102757285.png)

![image-20220630102506488](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220630102506488.png)

![image-20220701224821362](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220701224821362.png)



### 7.6.OpenResty实践

- OpenResty hello world

- shared dic:共享内存字典

- openResty redis支持

  itemredis.lua

  ```lua
  local args = ngx.req.get_uri_args()
  local id = args["id"]
  local redis = require "resty.redis"
  local cache = redis:new()
  local ok,err = cache:connect("172.17.16.15", 6379)
  cache:auth("fesi!#FSS32$@#@.22s")
  cache:select(10)
  local item_model = cache:get("item_"..id)
  if item_model == ngx.null or item_model == nil then
          local resp = ngx.location.capture("/item/getItem?id="..id)
          item_model = resp.body
  end
  
  ngx.say(item_model)
  ```

  ```conf
  location /luaitem/get{
              default_type "application/json";
              content_by_lua_file ../lua/itemredis.lua;
          }
  ```

  平均访问耗时远超没有访问redis，可能原因:缺乏高效的redis连接池，大量时间消耗在redis连接上

  ![image-20220701224551807](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220701224551807.png)

  ![image-20220701225811666](C:\Users\12195\AppData\Roaming\Typora\typora-user-images\image-20220701225811666.png)
