## 提供一些Spring的功能模块
以下都是一些在项目遇到需要的功能，感觉应该以后也能用上，所以就列出来，供以后参考使用。
因为是从项目里抽出来的，所以直接从git上取下来，必须配置自己的环境来运行，这里只提供了功能组件。

### 方法执行时间收集
可以对所有方法的执行时间进行采集，好处就不用多说了，相信这个应该是非常有用的功能。
主要功能参见com.test.events目录，所有功能都在这里了。
目前是使用H2数据库来收集信息的，数据有2种保存模式，一种是内存模式，一种是文件模式。
内存模式也就是应用在运行期间，可以查看对应的数据，但应用停止，数据也就没有。文件模式就是为了避免这种情况而存在。
在application.properties里有2项配置与此功能相关：
```
method.event.enable = true     #控制是否开启此功能
method.event.db.path = /var/log/eventdb     #H2数据库的位置，如果没有配置此路径，则默认所有操作记录保存在内存里，重启系统后数据就没有了
``` 

### p6spy的集成
Hibernate自带的show-sql功能基本上是没有什么用处的，特别是现在的项目基本上全部是prepareStatment模式的情况下，打印出来的sql具体参数是什么都看不到，就是一堆问号。
使用p6spy就可以查看到具体每个SQL的执行情况，包含真正包含参数的SQL，以及SQl执行对应的时间，这个才是真正有用处的功能。
集成方式很简单，主要在于application.properties里的2项配置：
```
spring.datasource.url = jdbc:p6spy:mysql://139.224.18.74:3306/cloudhotel?useSSL=false
spring.datasource.driverClassName = com.p6spy.engine.spy.P6SpyDriver
```
然后将spy.properties拷贝到resources目录下，具体如何配置的，请参见p6spy官网，这里不多说了。

### 通用的Native SQL查询模块
不得不说，Spring Data提供的Native SQL查询功能太不好用了，必须一个一个的定义什么ResultMapping，非常不灵活，不好用。
这里供用Spring提供的@Query注解，对功能进行增加，可以随意查询任意的SQL，然后返回结果可以是Map，也可以指定特定的DTO对象。
主要功能都放在com.test.repository下面。然后具体的Sample，请参见test/java下的UserQuery类。
在使用的时候，可以像正常Spring Data接口那样的用法，不需要写实现类。
