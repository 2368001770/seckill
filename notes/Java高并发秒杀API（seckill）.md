# Java高并发秒杀API（seckill）

标签（空格分隔）： 未分类

---

##1、Java高并发秒杀API之业务分析与DAO层
###1-1 创建工程
1、maven命令创建web骨架项目：mvn archetype:generate -DgroupId=org.seckill -DartifactId=seckill -DarchetypeArtifactId=maven-archetype-webapp
2、导入项目，设置Sources、Resources、Tests、Test Sources等目录
3、修改servlet版本，使jsp支持el表达，可参考tomcat/example/WEB_INF/web.xml

    <web-app xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                          http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
             version="3.0"
             metadata-complete="true">
             
    </web-app>

4、pom.xml补全项目依赖

    <!--补全项目依赖 -->
          <!--1.日志 java日志有:slf4j,log4j,logback,common-logging
          slf4j:是规范/接口
          日志实现:log4j,logback,common-logging
              使用:slf4j+logback -->
          <dependency>
              <groupId>org.slf4j</groupId>
              <artifactId>slf4j-api</artifactId>
              <version>1.7.12</version>
          </dependency>
          <dependency>
              <groupId>ch.qos.logback</groupId>
              <artifactId>logback-core</artifactId>
              <version>1.1.1</version>
          </dependency>
          <!--实现slf4j接口并整合 -->
          <dependency>
              <groupId>ch.qos.logback</groupId>
              <artifactId>logback-classic</artifactId>
              <version>1.1.1</version>
          </dependency>
    
          <!--2.数据库相关依赖 -->
          <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>5.1.35</version>
              <scope>runtime</scope>
          </dependency>
          <!--数据库连接池-->
          <dependency>
              <groupId>c3p0</groupId>
              <artifactId>c3p0</artifactId>
              <version>0.9.1.1</version>
          </dependency>
      
    
        <!--3.dao框架:MyBatis依赖 -->
          <dependency>
              <groupId>org.mybatis</groupId>
              <artifactId>mybatis</artifactId>
              <version>3.3.0</version>
          </dependency>
          <!--mybatis自身实现的spring整合依赖 -->
          <dependency>
              <groupId>org.mybatis</groupId>
              <artifactId>mybatis-spring</artifactId>
              <version>1.2.3</version>
          </dependency>
        
          <!--4.Servlet web相关依赖 -->
          <dependency>
              <groupId>taglibs</groupId>
              <artifactId>standard</artifactId>
              <version>1.1.2</version>
          </dependency>
          <dependency>
              <groupId>jstl</groupId>
              <artifactId>jstl</artifactId>
              <version>1.2</version>
          </dependency>
          <dependency>
              <groupId>com.fasterxml.jackson.core</groupId>
              <artifactId>jackson-databind</artifactId>
              <version>2.5.4</version>
          </dependency>
          <dependency>
              <groupId>javax.servlet</groupId>
              <artifactId>javax.servlet-api</artifactId>
              <version>3.1.0</version>
              <scope>provided</scope>
          </dependency>
        
          <!--5:spring依赖 -->
          <!--1)spring核心依赖 -->
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-core</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-beans</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-context</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <!--2)spring dao层依赖 -->
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-jdbc</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <!--spring事务相关的依赖-->
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-tx</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <!--3)springweb相关依赖 -->
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-web</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-webmvc</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>
          <!--4)spring test相关依赖 -->
          <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-test</artifactId>
              <version>4.1.7.RELEASE</version>
          </dependency>

###1-2 秒杀业务分析
1、业务分析
![此处输入图片的描述][1]
![此处输入图片的描述][2]
![此处输入图片的描述][3]
2、难点分析
![此处输入图片的描述][4]
![此处输入图片的描述][5]
![此处输入图片的描述][6]
![此处输入图片的描述][7]
3、功能分析
![此处输入图片的描述][8]
![此处输入图片的描述][9]
###1-3 DAO层设计与开发
1、数据库设计与编码

- [ ] 手写DDL---记录每次上线的DDL修改
- [ ] 联合主键---通过主键约束每个用户不能重复购买（唯一性）

```
CREATE TABLE seckill(
  `seckill_id` BIGINT NOT NUll AUTO_INCREMENT COMMENT '商品库存ID',
  `name` VARCHAR(120) NOT NULL COMMENT '商品名称',
  `number` int NOT NULL COMMENT '库存数量',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` TIMESTAMP  NOT NULL COMMENT '秒杀开始时间',
  `end_time`   TIMESTAMP   NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)ENGINE=INNODB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- 秒杀成功明细表
-- 用户登录认证相关信息(简化为手机号)
CREATE TABLE success_killed(
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀商品ID',
  `user_phone` BIGINT NOT NULL COMMENT '用户手机号',
  `state` TINYINT NOT NULL DEFAULT -1 COMMENT '状态标识:-1:无效 0:成功 1:已付款 2:已发货',
  `create_time` TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY(seckill_id,user_phone),/*联合主键*/
  KEY idx_create_time(create_time)
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';
```
2、DAO实体的编写
```
public class Seckill {
    private Long seckillId;  //秒杀商品id

    private String name;  //秒杀商品名称

    private Integer number; //秒杀商品库存数目

    private Date createTime;  //秒杀单的创建时间

    private Date startTime;  //秒杀商品开始时间

    private Date endTime; //秒杀商品结束时间
```
```
public class SuccessKilled {

    private short state; //用户秒杀商品状态

    private Date createTime;   //用户秒杀商品时间

    private Long seckillId;   //秒杀商品id

    private Long userPhone;  //用户手机号

    // 多对一,因为一件商品在库存中有很多数量，对应的购买明细也有很多。
    private Seckill seckill;

```
3、DAO接口编写
SeckillDao

- [ ] 有多个参数时需用@param进行标识
```
// 减库存
int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

//根据id查询秒杀商品
Seckill queryById(long seckillId);

//根据偏移量查询秒杀商品列表
List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);
```
SuccessKilledDao
```
//插入购买明细，可过滤重复
int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

//根据id查询SuccessKilled并携带秒杀商品对象实体
SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
```
4、基于mybatis实现DAO理论
![此处输入图片的描述][10]
![此处输入图片的描述][11]

5、基于mybatis实现DAO编程

① 在resources文件下编写mybatis-config.xml(从官网拷贝)
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!--配置全局属性 -->
    <settings>
        <!--使用jdbc的getGeneratekeys获取自增主键值，默认是false
            当inert一条记录时我们是不插入id的，id是通过自增去赋值的
            当插入完后想得到该插入记录的id时可以调用jdbc的getGeneratekeys -->
        <setting name="useGeneratedKeys" value="true"/>

        <!--使用列别名替换列名 默认值为true（可以不用写出来，这里写出来只是为了讲解该配置的作用）
            select name as title(实体中的属性名是title) form table;
            开启后mybatis会自动帮我们把表中name的值赋到对应实体的title属性中 -->
        <setting name="useColumnLabel" value="true"/>

        <!--开启驼峰命名转换Table:create_time到 Entity(createTime) -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

</configuration>
```

② 在resources文件创建mappers文件夹，编写xml文件（mybatis官网拷贝）

- [ ]  xml中不允许有<=符号，因装换为<![CDATA[ <= ]]>
SeckillDao.xml
```
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- namespace:指定为哪个接口提供配置 -->
<mapper namespace="com.seckill.dao.SeckillDao">
    <!--目的:为dao接口方法提供sql语句配置， 即针对dao接口中的方法编写我们的sql语句 -->

    <!-- int reduceNumber(long seckillId, Date killTime);-->
    <!-- 这里id必须和对应的DAO接口的方法名一样 -->
    <update id="reduceNumber">
		UPDATE seckill
		SET number = number-1
		WHERE seckill_id=#{seckillId}
		AND start_time <![CDATA[ <= ]]> #{killTime}
		AND end_time >= #{killTime}
		AND number > 0;
	</update>

    <!-- parameterType:使用到的参数类型
       正常情况java表示一个类型的包名+类名，这直接写类名，因为后面有一个配置可以简化写包名的过程 -->
    <select id="queryById" resultType="Seckill" parameterType="long">
        <!-- 可以通过别名的方式列明到java名的转换，如果开启了驼峰命名法就可以不用这么写了
                select seckill_id as seckillId
        -->
        SELECT seckill_id,name,number,create_time,start_time,end_time
        FROM seckill
        WHERE seckill_id=#{seckillId}
    </select>

    <select id="queryAll" resultType="Seckill">
		SELECT seckill_id,name,number,create_time,start_time,end_time
		FROM seckill
		ORDER BY create_time DESC
		limit #{offset},#{limit}
	</select>
</mapper>
```
SuccessKilledDao.xml

- [ ] sql中ignore可使参数主键冲突不报错而返回0
```
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.seckill.dao.SuccessKilledDao">

    <insert id="insertSuccessKilled">
        <!--当出现主键冲突时(即重复秒杀时)，会报错;不想让程序报错，加入ignore-->
        INSERT ignore INTO success_killed(seckill_id,user_phone,state)
        VALUES (#{seckillId},#{userPhone},0)
    </insert>

    <select id="queryByIdWithSeckill" resultType="SuccessKilled">

        <!--根据seckillId查询SuccessKilled对象，并携带Seckill对象-->
        <!--如何告诉mybatis把结果映射到SuccessKill属性同时映射到Seckill属性-->
        <!--可以自由控制SQL语句-->

        SELECT
        sk.seckill_id,
        sk.user_phone,
        sk.create_time,
        sk.state,
        s.seckill_id "seckill.seckill_id",
        s.name "seckill.name",
        s.number "seckill.number",
        s.start_time "seckill.start_time",
        s.end_time "seckill.end_time",
        s.create_time "seckill.create_time"
        FROM success_killed sk
        INNER JOIN seckill s ON sk.seckill_id=s.seckill_id
        WHERE sk.seckill_id=#{seckillId} and sk.user_phone=#{userPhone}
    </select>

</mapper>
```
6、mybatis整合Spring理论
![此处输入图片的描述][12]
![此处输入图片的描述][13]
![此处输入图片的描述][14]
![此处输入图片的描述][15]
![此处输入图片的描述][16]
![此处输入图片的描述][17]

7、mybatis整合Spring编码
① 在resources文件中编写jdbc.properties
```
jdbc.driver=com.mysql.jdbc.Driver
jbdc.url=jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf-8
jdbc.username=root
jdbc.password=root
```
② 在resources文件添加spring文件夹用于存放spring配置文件，在其中编写spring-dao.xml

* 配置数据源
* 配置sqlSessionFactory
* 配置扫描dao接口包
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
		http://www.springframework.org/schema/context/spring-context.xsd">

    <!--配置整合mybatis过程
    1.配置数据库相关参数属性  ${url}-->
    <context:property-placeholder location="classpath:jdbc.properties"/>

    <!--2.数据库连接池-->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <!--配置连接池属性-->
        <property name="driverClass" value="${jdbc.driver}"/>

        <!-- 基本属性 url、user、password -->
        <property name="jdbcUrl" value="${jbdc.url}"/>
        <property name="user" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>

        <!--c3p0私有属性-->
        <property name="maxPoolSize" value="30"/>
        <property name="minPoolSize" value="10"/>
        <!--关闭连接后不自动commit-->
        <property name="autoCommitOnClose" value="false"/>

        <!--获取连接超时时间-->
        <property name="checkoutTimeout" value="1000"/>
        <!--当获取连接失败重试次数-->
        <property name="acquireRetryAttempts" value="2"/>
    </bean>

    <!--约定大于配置-->
    <!--３.配置SqlSessionFactory对象-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!--往下才是mybatis和spring真正整合的配置-->
        <!--注入数据库连接池-->
        <property name="dataSource" ref="dataSource"/>
        <!--配置mybatis全局配置文件:mybatis-config.xml-->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <!--扫描entity包,使用别名,多个用;隔开-->
        <property name="typeAliasesPackage" value="com.seckill.entity"/>
        <!--扫描sql配置文件:mapper需要的xml文件-->
        <property name="mapperLocations" value="classpath:mappers/*.xml"/>
    </bean>

    <!--４:配置扫描Dao接口包,动态实现DAO接口,注入到spring容器-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--注入SqlSessionFactory-->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <!-- 给出需要扫描的Dao接口-->
        <property name="basePackage" value="com.seckill.dao"/>
    </bean>
</beans>
```
###1-4 DAO层单元测试
进入dao接口，在接口名上使用快捷键alt+enter创建该dao接口的测试类，选择JUnit4（pom.xml对应版本为4.11），对所选方法进行测试

- [ ] @RunWith(SpringJUnit4ClassRunner.class)
- [ ] @ContextConfiguration({"classpath:spring/spring-dao.xml"})
```
/**
 * 配置Spring和Junit整合,junit启动时加载springIOC容器 
 * spring-test,junit
 */

@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    // 注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;


    @Test
    public void testReduceNumber() {
        long seckillId = 1000;
        Date date = new Date();
        int updateCount = seckillDao.reduceNumber(seckillId, date);
        System.out.println(updateCount);
    }

    @Test
    public void testQueryById() {

        long seckillId = 1000;
        Seckill seckill = seckillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void testQueryAll() {
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for (Seckill seckill : seckills) {
            System.out.println(seckill);
        }
    }
}
```
##2、Java高并发秒杀API之Service层
###2-1 秒杀接口设计
业务接口的设计原则：站在使用者（程序员）的角度设计接口
* 方法粒度，方法定义的要非常清楚
* 参数，要越简练越好
* 返回类型，return类型一定要友好或者return我们允许的异常
```
public interface SeckillService {

    //查询全部的秒杀记录
    List<Seckill> getSeckillList();

    //查询单个秒杀记录
    Seckill getById(long seckillId);

    //在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
    Exposer exportSeckillUrl(long seckillId);

    //执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;

```
###2-2 DTO、Enum、自定义Exception
1、暴露秒杀地址DTO,在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
```
public class Exposer {

    // 是否开启秒杀
    private boolean exposed;

    // 加密措施
    private String md5;

    //id为seckillId的商品的秒杀地址
    private long seckillId;

    // 系统当前时间(毫秒)
    private long now;

    // 秒杀的开启时间
    private long start;

    // 秒杀的结束时间
    private long end;

    public Exposer(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public Exposer(boolean exposed, long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }
```
2、封装执行秒杀后的结果:是否秒杀成功
```
public class SeckillExecution {
    private long seckillId;

    //秒杀执行结果的状态
    private int state;

    //状态的明文标识
    private String stateInfo;

    //当秒杀成功时，需要传递秒杀成功的对象回去
    private SuccessKilled successKilled;

    //秒杀成功返回所有信息
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getInfo();
        this.successKilled = successKilled;
    }

    //秒杀失败
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getInfo();
    }

```
3、使用枚举表述常量数据字段
```
public enum SeckillStatEnum {

    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATE_REWRITE(-3, "数据篡改");

    private int state;
    private String info;

    SeckillStatEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }

    public String getInfo() {
        return info;
    }

    public static SeckillStatEnum stateOf(int index) {
        for (SeckillStatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
```
4、定义统一的返回类型，将所有的ajax请求返回类型，全部封装成json数据
```
public class SeckillResult<T> {

    //请求是否成功
    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
```
5、秒杀相关业务异常
秒杀相关业务异常，继承RuntimeException，Spring声明式事务只有对RuntimeException可以回滚
```
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
重复秒杀异常
```
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
秒杀关闭异常
```
public class SeckillCloseException extends SeckillException {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
###2-3 秒杀接口的实现
- [ ] 使用Spring工具实现md5加密
- [ ] 只有运行期异常可以回滚，最后把所有异常都进行catch再抛出SeckillException是为了失败时回滚事务
```
public class SeckillServiceImpl implements SeckillService {

     // 日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 加入一个混淆字符串(秒杀接口)的salt，为了避免用户猜出我们的md5值，值任意给，越复杂越好
    private final String salt = "akseh295sdssd52536";

    // 注入Service依赖
    @Autowired  // @Resource ,@Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;


    @Autowired
    private RedisDao redisDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }


    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        //spring的工具类
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        
        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            return new Exposer(false, seckillId);
        }
        
        // 若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        // 系统当前时间
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 秒杀开启，返回秒杀商品的id、用给接口加密的md5
        //转化特定字符的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }


    @Override
    @Transactional
    // 秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");// 秒杀数据被重写了
        }
        // 执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        
        try {
            // 减库存，热点商品竞争
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                // 没有更新库存记录，说明秒杀结束 rollback
                throw new SeckillCloseException("seckill is closed");
             } else {
                 // 否则更新了库存，秒杀成功,增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                // 看是否该明细被重复插入，即用户是否重复秒杀
                if (insertCount <= 0) {
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                } else {
                    // 秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 将编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }
}
```
###2-4 基于Spring托管Service实现类
理论
![此处输入图片的描述][18]
![此处输入图片的描述][19]
![此处输入图片的描述][20]
![此处输入图片的描述][21]
![此处输入图片的描述][22]
配置
1、在spring文件夹添加spring-service.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!--扫描service包下所有使用注解的类型 -->
    <context:component-scan base-package="com.seckill.service"/>
```
2、在SeckillServiceImpl添加@Service、@Autowire等注解
###2-5 配置使用Spring声明式事务
理论
![此处输入图片的描述][23]
![此处输入图片的描述][24]
![此处输入图片的描述][25]
![此处输入图片的描述][26]
配置
1、在spring-service.xml中配置事务管理器
```
<!--配置事务管理器 -->
    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <!--注入数据库连接池 -->
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--配置基于注解的声明式事务 默认使用注解来管理事务行为 -->
    <tx:annotation-driven transaction-manager="transactionManager"/>
```
2、在Service层中需要加事务的方法添加@Transactional注解

3、使用注解控制事务方法的优点:

 * 开发团队达成一致约定，明确标注事务方法的编程风格
 * 保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
 * 不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
    
###2-6 Service集成测试
1、使用logback日志，在resources文件下添加配置logback.xml(官网拷贝) 

> java日志有:slf4j,log4j,logback,common-logging 
slf4j:是规范/接口
> 日志实现有:log4j,logback,common-logging 
项目使用的是:slf4j+logback

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```
2、测试代码
```
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 注入Service实现类依赖
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}", list);
    }

    @Test
    public void getById() {
        long seckillId = 1000;
        Seckill seckill = seckillService.getById(seckillId);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long seckillId = 1001;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        logger.info("exposer={}", exposer);
    }

    // 集成测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId = 1004;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long userPhone = 13476191576L;
            String md5 = exposer.getMd5();

            try {
                SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("result={}", execution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e1) {
                logger.error(e1.getMessage());
            }
        } else {
            // 秒杀未开启
            logger.warn("exposer={}", exposer);
        }
    }
```
## 3、Java高性能秒杀API之Web层
###3-1 前端交互流程设计
![此处输入图片的描述][27]
![此处输入图片的描述][28]
###3-2 Restful接口设计
![此处输入图片的描述][29]
![此处输入图片的描述][30]
![此处输入图片的描述][31]
![此处输入图片的描述][32]
###3-3 SpringMvc整合Spring
1、springmvc框架理论
![此处输入图片的描述][33]
![此处输入图片的描述][34]
![此处输入图片的描述][35]
![此处输入图片的描述][36]
![此处输入图片的描述][37]
![此处输入图片的描述][38]
![此处输入图片的描述][39]
![此处输入图片的描述][40]
2、配置
①配置web.xml
```
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                      http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0"
         metadata-complete="true">
    <!--用maven创建的web-app需要修改servlet的版本为3.0 -->
    <!--配置DispatcherServlet -->
    <servlet>
        <servlet-name>seckill-dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>

        <!-- 配置SpringMVC 需要配置的文件 spring-dao.xml，spring-service.xml,spring-web.xml
            MyBatis -> Spring -> SpringMVC -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring/spring-*.xml</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>seckill-dispatcher</servlet-name>
        <!--默认匹配所有请求 -->
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```
②配置spring-web.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/mvc
        http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!--配置spring mvc-->
    <!--1,开启springmvc注解模式
    a.自动注册DefaultAnnotationHandlerMapping,AnnotationMethodHandlerAdapter
    b.默认提供一系列的功能:数据绑定，数字和日期的format@NumberFormat,@DateTimeFormat
    c:xml,json的默认读写支持-->
    <mvc:annotation-driven/>

    <!--2.静态资源默认servlet配置-->
    <!--
        1).加入对静态资源处理：js,gif,png
        2).允许使用 "/" 做整体映射
    -->
    <mvc:default-servlet-handler/>

    <!--3：配置JSP 显示ViewResolver-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

    <!--4:扫描web相关的controller-->
    <context:component-scan base-package="com.seckill.web"/>
</beans>
```
###3-4 使用SpringMVC实现Restful接口
- [ ] url:模块/资源/{}/细分
- [ ] produces:浏览器查看方便（json自动格式化，带搜索），也可以防止中文乱码。
```
@Controller
@RequestMapping("/seckill")//url:模块/资源/{}/细分
public class SeckillController {

    // 日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    //显示秒杀商品信息列表
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //list.jsp+mode=ModelAndView
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        return "list"; //"/WEB-INF/jsp/list.sjp"
    }

    //显示秒杀商品详情
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";   //请求重定向
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";   //请求转发
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }


    //ajax ,json暴露秒杀接口的方法
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            e.printStackTrace();
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }

        return result;
    }


    //执行秒杀过程
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone", required = false) Long userPhone) {

        if (userPhone == null) {
            return new SeckillResult<SeckillExecution>(false, "未注册");

        }
        SeckillResult<SeckillExecution> result;
        try {
          
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (RepeatKillException e1) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e2) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }

    }


    //获取系统时间
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }

}
```
###3-5 基于bootstrap开发页面结构和交互逻辑编程
在bootstrap官网拷贝通用模板，在此基础上进行修改和封装

- [ ] jQery文件务必在bootstrap.min.js之前引入
- [ ] 使用CDN 获取公共js
- [ ] jQuery Cookie操作插件
- [ ] jQuery countDown倒计时插件
head.jsp
```
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta charset="utf-8">
<!-- 新 Bootstrap 核心 CSS 文件 -->
<link href="http://apps.bdimg.com/libs/bootstrap/3.3.0/css/bootstrap.min.css" rel="stylesheet">
<!-- 可选的Bootstrap主题文件（一般不使用） -->
<link href="http://apps.bdimg.com/libs/bootstrap/3.3.0/css/bootstrap-theme.min.css" rel="stylesheet">

```
tag.jsp
```
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

```
list.jsp
```
<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<%@include file="common/tag.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <title>秒杀商品列表</title>
    <%@include file="common/head.jsp" %>
</head>
<body background="<%=basePath%>images/ms30.png">
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading text-center">
            <h2>秒杀列表</h2>
        </div>
        <div class="panel-body">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>名称</th>
                    <th>库存</th>
                    <th>开始时间</th>
                    <th>结束时间</th>
                    <th>创建时间</th>
                    <th>详情页</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${list}" var="sk">
                    <tr>
                        <td>${sk.name}</td>

                        <td>${sk.number}</td>
                        <td>
                            <fmt:formatDate value="${sk.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <fmt:formatDate value="${sk.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td>
                            <fmt:formatDate value="${sk.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </td>
                        <td><a class="btn btn-info"
                               href="${pageContext.request.contextPath }/seckill/${sk.seckillId}/detail"
                               target="_blank">链接</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

        </div>
    </div>
</div>

<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>

<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>

</body>
</html>
```
detail.jsp
```
<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@include file="common/tag.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <title>秒杀详情页</title>
    <%@include file="common/head.jsp" %>
</head>
<body>
<div class="container">
    <div class="panel panel-default text-center">
        <div class="pannel-heading">
            <h1>${seckill.name}</h1>
        </div>

        <div class="panel-body">
            <h2 class="text-danger">
                <%--显示time图标--%>
                <span class="glyphicon glyphicon-time"></span>
                <%--展示倒计时--%>
                <span class="glyphicon" id="seckill-box"></span>
            </h2>
        </div>
    </div>
</div>
<%--登录弹出层 输入电话--%>
<div id="killPhoneModal" class="modal fade">

    <div class="modal-dialog">

        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title text-center">
                    <span class="glyphicon glyphicon-phone"> </span>秒杀电话:
                </h3>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-8 col-xs-offset-2">
                        <input type="text" name="killPhone" id="killPhoneKey"
                               placeholder="填写手机号^o^" class="form-control">
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <%--验证信息--%>
                <span id="killPhoneMessage" class="glyphicon"> </span>
                <button type="button" id="killPhoneBtn" class="btn btn-success">
                    <span class="glyphicon glyphicon-phone"></span>
                    Submit
                </button>
            </div>

        </div>
    </div>

</div>

</body>
<%--jQery文件,务必在bootstrap.min.js之前引入--%>
<script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>
<script src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>
<%--使用CDN 获取公共js http://www.bootcdn.cn/--%>
<%--jQuery Cookie操作插件--%>
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<%--jQuery countDown倒计时插件--%>
<script src="https://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>

<script src="${pageContext.request.contextPath}/resources/js/seckill.js"></script>

<script type="text/javascript">
    jQuery(function () {
        //使用EL表达式传入参数
        seckill.detail.init({
            seckillId:${seckill.seckillId},
            startTime:${seckill.startTime.time},//毫秒
            endTime:${seckill.endTime.time}
        });
    });
</script>
</html>
```
seckill.js
```
var seckill = {

    //封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },

    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;//直接判断对象会看对象是否为空,空就是undefine就是false; isNaN 非数字返回true
        } else {
            return false;
        }
    },

    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            //手机验证和登录,计时交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var userPhone = $.cookie('userPhone');
            //验证手机号
            if (!seckill.validatePhone(userPhone)) {
                //绑定手机 控制输出
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static',//禁止位置关闭
                    keyboard: false//关闭键盘事件
                });

                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
//                    console.log("inputPhone: " + inputPhone);
                    if (seckill.validatePhone(inputPhone)) {
                        //电话写入cookie(7天过期)
                        $.cookie('userPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //验证通过　　刷新页面
                        window.location.reload();
                    } else {
                        //todo 错误文案信息抽取到前端字典里
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }

            //已经登录
            //计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
//            console.log("开始秒杀时间=======" + startTime);
//            console.log("结束秒杀时间========" + endTime);
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间判断 计时交互
                    seckill.countDown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result: ' + result);
                    alert('result: ' + result);
                }
            });
        }
    },

    handlerSeckill: function (seckillId, node) {
        //获取秒杀地址,控制显示器,执行秒杀
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');

        $.get(seckill.URL.exposer(seckillId), {}, function (result) {
            //在回调函数种执行交互流程
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log("killUrl: " + killUrl);
                    //绑定一次点击事件
                    $('#killBtn').one('click', function () {
                        //执行秒杀请求
                        //1.先禁用按钮
                        $(this).addClass('disabled');//,<-$(this)===('#killBtn')->
                        //2.发送秒杀请求执行秒杀
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            }
                        });
                    });
                    node.show();
                } else {
                    //未开启秒杀(浏览器计时偏差)
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countDown(seckillId, now, start, end);
                }
            } else {
                console.log('result: ' + result);
            }
        });

    },

    countDown: function (seckillId, nowTime, startTime, endTime) {
        console.log(seckillId + '_' + nowTime + '_' + startTime + '_' + endTime);
        var seckillBox = $('#seckill-box');
        if (nowTime > endTime) {
            //秒杀结束
            seckillBox.html('秒杀结束!');
        } else if (nowTime < startTime) {
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);//todo 防止时间偏移
            seckillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒 ');
                seckillBox.html(format);
            }).on('finish.countdown', function () {
                //时间完成后回调事件
                //获取秒杀地址,控制现实逻辑,执行秒杀
                console.log('______fininsh.countdown');
                seckill.handlerSeckill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
            seckill.handlerSeckill(seckillId, seckillBox);
        }
    }
};
```
##4、Java高并发秒杀API之高并发优化
###4-1 高并发优化分析
1、从项目流程中分析高并发可能发生的地方（红色部分表示高并发可能出现的地方）
![此处输入图片的描述][41]

2、CDN部署
![此处输入图片的描述][42]
![此处输入图片的描述][43]

虽然静态资源可以部署在CDN上，但是系统时间必须从服务器取。获取系统时间不需要进行优化，可直接从服务器获取，只需要没有其他（java访问一次内存可达10ns）

3、秒杀地址接口优化
![此处输入图片的描述][44]
![此处输入图片的描述][45]

4、秒杀操作优化
![此处输入图片的描述][46]
5、其他方案分析
![此处输入图片的描述][47]
![此处输入图片的描述][48]

为什么不用mysql解决？？mysql真的低效吗？？事实上，同一个id执行update减库存每秒钟可执行4w次
![此处输入图片的描述][49]
使mysql低效的原因是，java控制事务行为中网络延迟和gc操作
![此处输入图片的描述][50]
![此处输入图片的描述][51]
减少行级锁持有时间
![此处输入图片的描述][52]
使用存储过程
![此处输入图片的描述][53]
![此处输入图片的描述][54]
![此处输入图片的描述][55]

6、优化总结
![此处输入图片的描述][56]

###4-2 redis后端缓存优化编码
1、在pom.xml中引入jedis依赖
```
    <dependency>
          <groupId>redis.clients</groupId>
          <artifactId>jedis</artifactId>
          <version>2.7.3</version>
    </dependency>
```
2、引入protostuff序列化依赖
redis并没有实现内部序列化操作，因为这种方式序列化性能（序列化所需时间、序列化后在网络中传输的字节数大小）较高，更适合高并发的场景
```
      <dependency>
          <groupId>com.dyuproject.protostuff</groupId>
          <artifactId>protostuff-core</artifactId>
          <version>1.0.8</version>
      </dependency>
      <dependency>
          <groupId>com.dyuproject.protostuff</groupId>
          <artifactId>protostuff-runtime</artifactId>
          <version>1.0.8</version>
      </dependency>
```
3、在dao/cache包中编写RedisDao类进行对Redis数据库的操作
```
public class RedisDao {

    // 日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId) {
        // redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                // 并没有实现内部序列化操作
                // 采用自定义序列化
                // protostuff: pojo.
                byte[] bytes = jedis.get(key.getBytes());
                // 缓存重获取到
                if (bytes != null) {
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    // seckill被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                // 超时缓存
                int timeout = 60;// 1 min
                String result = jedis.setex(key.getBytes(), timeout, bytes);

                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {

        }

        return null;
    }
}

```
4、在spring-dao.xml中注入RedisDao这个bean
```
    <bean id="redisDao" class="com.seckill.dao.cache.RedisDao">
        <constructor-arg index="0" value="localhost"/>
        <constructor-arg index="1" value="6379"/>
    </bean>
```
5、编写测试方法对RedisDao进行测试
```
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long id = 1004;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testSeckill() {

        Seckill seckill = redisDao.getSeckill(id);
        if (seckill == null) {
            seckill = seckillDao.queryById(id);
            if (seckill != null) {
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                //logger.info("result={}", result);
                seckill = redisDao.getSeckill(id);
                System.out.println(seckill);
                //logger.info("seckill={}", seckill);
            }
        }
    }

}
```
6、在SeckillServiceImpl中操作redis进行缓存优化
```
         //优化点：缓存点 超时的基础上维护一致性  减低了数据库访问量
        // 1.访问redi
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {// 说明查不到这个秒杀产品的记录
                return new Exposer(false, seckillId);
            } else {
                // 3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
```
###4-3 秒杀操作并发优化
1、秒杀操作事务执行过程
![此处输入图片的描述][57]
2、简单优化：先insert明细再update减库存，一可以先阻止重复秒杀操作，二可以减少事务中持有锁的时间。
![此处输入图片的描述][58]
编码
```
 public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");// 秒杀数据被重写了
        }
        // 执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        /**
         * 将 减库存 插入购买明细  提交
         * 改为 插入购买明细 减库存 提交
         * 降低了网络延迟和GC影响，同时减少了rowLock的时间
         */
        try {
            // 否则更新了库存，秒杀成功,增加明细
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            // 看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                // 减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    // 没有更新库存记录，说明秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    // 秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 将编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }
```
3、深度优化：利用存储过程使事务SQL在Mysql端执行，可以屏蔽网络延迟和GC影响
编写存储过程sql代码并在mysql端执行
```
-- 秒杀执行存储过程
DELIMITER $$ -- onsole ; 转换为
$$
-- 定义存储过程
-- 参数：in 输入参数; out 输出参数
-- row_count():返回上一条修改类型sql(delete,insert,upodate)的影响行数
-- row_count: 0:未修改数据; >0:表示修改的行数; <0:sql错误/未执行修改sql
CREATE PROCEDURE `seckill`.`execute_seckill`
  (IN v_seckill_id bigint, IN v_phone BIGINT,
   IN v_kill_time  TIMESTAMP, OUT r_result INT)
  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION;
    INSERT ignore INTO success_killed (seckill_id, user_phone, create_time)
    VALUES (v_seckill_id, v_phone, v_kill_time);
    SELECT ROW_COUNT() INTO insert_count;
    IF (insert_count = 0)
    THEN
      ROLLBACK;
      SET r_result = -1;
    ELSEIF (insert_count < 0)
      THEN
        ROLLBACK;
        SET r_result = -2;
    ELSE
      UPDATE seckill
      SET number = number - 1
      WHERE seckill_id = v_seckill_id
        AND end_time > v_kill_time
        AND start_time < v_kill_time
        AND number > 0;
      SELECT ROW_COUNT() INTO insert_count;
      IF (insert_count = 0)
      THEN
        ROLLBACK;
        SET r_result = 0;
      ELSEIF (insert_count < 0)
        THEN
          ROLLBACK;
          SET r_result = -2;
      ELSE
        COMMIT;
        SET r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 代表存储过程定义结束

DELIMITER ;

SET @r_result = -3;
-- 执行存储过程
call execute_seckill(1001, 17215638291, now(), @r_result);
-- 获取结果
SELECT @r_result;

-- 存储过程
-- 1.存储过程优化：事务行级锁持有的时间
-- 2.不要过度依赖存储过程
-- 3.简单的逻辑可以应用存储过程
-- 4.QPS:一个秒杀单6000/qps
```
java客户端调用存储过程
dao层
```
//使用储存过程执行秒杀
void killByProcedure(Map<String, Object> paramMap);
```
```
    <!--调用储存过程 -->
    <select id="killByProcedure" statementType="CALLABLE">
		CALL execute_seckill(
			#{seckillId,jdbcType=BIGINT,mode=IN},
			#{phone,jdbcType=BIGINT,mode=IN},
			#{killTime,jdbcType=TIMESTAMP,mode=IN},
			#{result,jdbcType=INTEGER,mode=OUT}
		)
	</select>
```
service层
事务操作在mysql端，service层不用使用声明式事务注解，也没有必要抛出异常
```
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行储存过程,result被复制
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);

        }
    }
```
测试
```
     public void executeSeckillProcedure() {
        long seckillId = 1007;
        long phone = 13680115101L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info(execution.getStateInfo());
        }
    }
```
controller层
```
    //执行秒杀过程
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone", required = false) Long userPhone) {

        if (userPhone == null) {
            return new SeckillResult<SeckillExecution>(false, "未注册");

        }
        SeckillResult<SeckillExecution> result;
        try {
            //存储过程调用
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId, userPhone, md5);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (RepeatKillException e1) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e2) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }

    }

```

###4-4 系统部署架构
系统部署可能用到的服务
![此处输入图片的描述][59]
大型系统部署架构
![此处输入图片的描述][60]
项目中可能参与的角色
![此处输入图片的描述][61]


  [1]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%A7%92%E6%9D%80%E7%B3%BB%E7%BB%9F%E4%B8%9A%E5%8A%A1%E6%B5%81%E7%A8%8B.png
  [2]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%A7%92%E6%9D%80%E4%B8%9A%E5%8A%A1%E6%A0%B8%E5%BF%83.png
  [3]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%94%A8%E6%88%B7%E9%92%88%E5%AF%B9%E5%BA%93%E5%AD%98%E4%B8%9A%E5%8A%A1%E5%88%86%E6%9E%90.png
  [4]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%A7%92%E6%9D%80%E9%9A%BE%E7%82%B9%EF%BC%88%E7%AB%9E%E4%BA%89%EF%BC%89.png
  [5]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BA%8B%E5%8A%A1%E5%92%8C%E8%A1%8C%E7%BA%A7%E9%94%81.png
  [6]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%AB%9E%E4%BA%89%E4%B9%8B%E4%BA%8B%E5%8A%A1.png
  [7]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%AB%9E%E4%BA%89%E4%B9%8B%E8%A1%8C%E7%BA%A7%E9%94%81.png
  [8]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%A7%92%E6%9D%80%E5%8A%9F%E8%83%BD.png
  [9]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BB%A3%E7%A0%81%E5%BC%80%E5%8F%91.png
  [10]: http://pr4gg6olg.bkt.clouddn.com/seckillsql%E7%BC%96%E5%86%99%E6%96%B9%E5%BC%8F.png
  [11]: http://pr4gg6olg.bkt.clouddn.com/seckilldao%E5%AE%9E%E7%8E%B0%E6%96%B9%E5%BC%8F.png
  [12]: http://pr4gg6olg.bkt.clouddn.com/seckillmybatis%E6%95%B4%E5%90%88%E7%9B%AE%E6%A0%87.png
  [13]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%8E%A5%E5%8F%A3%E4%BD%9C%E7%94%A8.png
  [14]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%9B%B4%E5%B0%91%E7%9A%84%E9%85%8D%E7%BD%AE%EF%BC%88%E5%88%AB%E5%90%8D%EF%BC%89.png
  [15]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%9B%B4%E5%B0%91%E7%9A%84%E9%85%8D%E7%BD%AE%EF%BC%88%E9%85%8D%E7%BD%AE%E6%89%AB%E6%8F%8F%EF%BC%89.png
  [16]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%9B%B4%E5%B0%91%E7%9A%84%E9%85%8D%E7%BD%AE%EF%BC%88dao%E5%AE%9E%E7%8E%B0%EF%BC%89.png
  [17]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%81%B5%E6%B4%BB%E6%80%A7.png
  [18]: http://pr4gg6olg.bkt.clouddn.com/seckillSpring%20IOC%E5%8A%9F%E8%83%BD%E7%90%86%E8%A7%A3.png
  [19]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%B8%9A%E5%8A%A1%E5%AF%B9%E8%B1%A1%E4%BE%9D%E8%B5%96%E5%9B%BE.png
  [20]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%B8%BA%E4%BB%80%E4%B9%88%E4%BD%BF%E7%94%A8IOC.png
  [21]: http://pr4gg6olg.bkt.clouddn.com/seckillSpring%20IOC%E6%B3%A8%E5%85%A5%E6%96%B9%E5%BC%8F%E5%92%8C%E5%9C%BA%E6%99%AF.png
  [22]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%9C%AC%E9%A1%B9%E7%9B%AEIOC%E4%BD%BF%E7%94%A8.png
  [23]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BB%80%E4%B9%88%E6%98%AF%E5%A3%B0%E6%98%8E%E5%BC%8F%E4%BA%8B%E5%8A%A1.png
  [24]: http://pr4gg6olg.bkt.clouddn.com/seckill%E5%A3%B0%E6%98%8E%E5%BC%8F%E4%BA%8B%E5%8A%A1%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F.png
  [25]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BA%8B%E5%8A%A1%E6%96%B9%E6%B3%95%E5%B5%8C%E5%A5%97.png
  [26]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BB%80%E4%B9%88%E6%97%B6%E5%80%99%E5%9B%9E%E6%BB%9A%E4%BA%8B%E5%8A%A1.png
  [27]: http://pr4gg6olg.bkt.clouddn.com/seckill%E5%89%8D%E7%AB%AF%E9%A1%B5%E9%9D%A2%E6%B5%81%E7%A8%8B.png
  [28]: http://pr4gg6olg.bkt.clouddn.com/seckill%E8%AF%A6%E6%83%85%E9%A1%B5%E6%B5%81%E7%A8%8B%E9%80%BB%E8%BE%91.png
  [29]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BB%80%E4%B9%88%E6%98%AFrestful.png
  [30]: http://pr4gg6olg.bkt.clouddn.com/seckillRestful%E8%A7%84%E8%8C%83.png
  [31]: http://pr4gg6olg.bkt.clouddn.com/seckillurl%E8%AE%BE%E8%AE%A1.png
  [32]: http://pr4gg6olg.bkt.clouddn.com/seckill%E7%A7%92%E6%9D%80api%E4%B8%AD%E7%9A%84url%E8%AE%BE%E8%AE%A1.png
  [33]: http://pr4gg6olg.bkt.clouddn.com/seckill%E5%9B%B4%E7%BB%95handler%E5%BC%80%E5%8F%91.png
  [34]: http://pr4gg6olg.bkt.clouddn.com/seckillSpringmvc%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B.png
  [35]: http://pr4gg6olg.bkt.clouddn.com/seckillhttp%E8%AF%B7%E6%B1%82%E5%9C%B0%E5%9D%80%E6%98%A0%E5%B0%84%E5%8E%9F%E7%90%86.png
  [36]: http://pr4gg6olg.bkt.clouddn.com/seckill%E6%B3%A8%E8%A7%A3%E6%98%A0%E5%B0%84%E6%8A%80%E5%B7%A7.png
  [37]: http://pr4gg6olg.bkt.clouddn.com/seckill%E8%AF%B7%E6%B1%82%E6%96%B9%E6%B3%95%E7%BB%86%E8%8A%82%E5%A4%84%E7%90%86.png
  [38]: http://pr4gg6olg.bkt.clouddn.com/seckill%E4%BE%8B%E5%AD%90.png
  [39]: http://pr4gg6olg.bkt.clouddn.com/seckill%E8%BF%94%E5%9B%9Ejson%E6%95%B0%E6%8D%AE.png
  [40]: http://pr4gg6olg.bkt.clouddn.com/seckillcookie%E8%AE%BF%E9%97%AE.png
  [41]: http://pr4gg6olg.bkt.clouddn.com/seckill4-1%E9%A1%B9%E7%9B%AE%E6%B5%81%E7%A8%8B.png
  [42]: http://pr4gg6olg.bkt.clouddn.com/seckill4-2%E8%AF%A6%E6%83%85%E9%A1%B5CDN.png
  [43]: http://pr4gg6olg.bkt.clouddn.com/seckill4-3CDN%E7%9A%84%E7%90%86%E8%A7%A3.png
  [44]: http://pr4gg6olg.bkt.clouddn.com/seckill4-4%E7%A7%92%E6%9D%80%E5%9C%B0%E5%9D%80%E6%8E%A5%E5%8F%A3%E5%88%86%E6%9E%90.png
  [45]: http://pr4gg6olg.bkt.clouddn.com/seckill4-5%E7%A7%92%E6%9D%80%E5%9C%B0%E5%9D%80%E6%8E%A5%E5%8F%A3%E4%BC%98%E5%8C%96.png
  [46]: http://pr4gg6olg.bkt.clouddn.com/seckill4-6%E7%A7%92%E6%9D%80%E6%93%8D%E4%BD%9C%E4%BC%98%E5%8C%96.png
  [47]: http://pr4gg6olg.bkt.clouddn.com/seckill4-7%E5%85%B6%E4%BB%96%E6%96%B9%E6%A1%88%E5%88%86%E6%9E%90.png
  [48]: http://pr4gg6olg.bkt.clouddn.com/seckill4-8%E6%88%90%E6%9C%AC%E5%88%86%E6%9E%90.png
  [49]: http://pr4gg6olg.bkt.clouddn.com/seckill4-9%E4%B8%80%E6%9D%A1update%E5%8E%8B%E5%8A%9B%E6%B5%8B%E8%AF%95.png
  [50]: http://pr4gg6olg.bkt.clouddn.com/seckill4-10java%E6%8E%A7%E5%88%B6%E4%BA%8B%E5%8A%A1%E8%A1%8C%E4%B8%BA%E5%88%86%E6%9E%90.png
  [51]: http://pr4gg6olg.bkt.clouddn.com/seckill4-11%E7%93%B6%E9%A2%88%E5%88%86%E6%9E%90.png
  [52]: http://pr4gg6olg.bkt.clouddn.com/seckill4-12%E4%BC%98%E5%8C%96%E5%88%86%E6%9E%90.png
  [53]: http://pr4gg6olg.bkt.clouddn.com/seckill4-13%E5%BB%B6%E8%BF%9F%E5%88%86%E6%9E%90.png
  [54]: http://pr4gg6olg.bkt.clouddn.com/seckill4-14%E5%88%A4%E6%96%ADupdate%E6%88%90%E5%8A%9F.png
  [55]: http://pr4gg6olg.bkt.clouddn.com/seckill4-15%E6%8A%8A%E5%AE%A2%E6%88%B7%E7%AB%AF%E9%80%BB%E8%BE%91%E6%94%BE%E5%88%B0mysql%E6%9C%8D%E5%8A%A1%E7%AB%AF.png
  [56]: http://pr4gg6olg.bkt.clouddn.com/seckill4-16%E4%BC%98%E5%8C%96%E6%80%BB%E7%BB%93.png
  [57]: http://pr4gg6olg.bkt.clouddn.com/seckill4-17%E5%9B%9E%E9%A1%BE%E4%BA%8B%E5%8A%A1%E6%89%A7%E8%A1%8C.png
  [58]: http://pr4gg6olg.bkt.clouddn.com/seckill4-18%E6%89%A7%E8%A1%8C%E6%93%8D%E4%BD%9C%E7%AE%80%E5%8D%95%E4%BC%98%E5%8C%96.png
  [59]: http://pr4gg6olg.bkt.clouddn.com/seckill4-19%E7%B3%BB%E7%BB%9F%E9%83%A8%E7%BD%B2%E5%8F%AF%E8%83%BD%E7%94%A8%E5%88%B0%E7%9A%84%E6%9C%8D%E5%8A%A1.png
  [60]: http://pr4gg6olg.bkt.clouddn.com/seckill4-20%E7%B3%BB%E7%BB%9F%E9%83%A8%E7%BD%B2%E6%9E%B6%E6%9E%84.png
  [61]: http://pr4gg6olg.bkt.clouddn.com/seckill4-21%E9%A1%B9%E7%9B%AE%E4%B8%AD%E5%8F%AF%E8%83%BD%E5%8F%82%E4%B8%8E%E7%9A%84%E8%A7%92%E8%89%B2.png