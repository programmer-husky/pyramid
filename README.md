# Pyramid缓存组件
Pyramid是基于AOP实现的缓存组件，可使用本地缓存和Redis缓存。
本文档对应的版本号为 ***0.0.5***

 ## -  导入
 增加maven依赖，reimport
```java
<dependency>
            <groupId>com.meeruu</groupId>
            <artifactId>pyramid</artifactId>
            <version>0.0.5</version>
</dependency>
```

 ## - @Pyramid
 以方法入参参数数组列表中指定的一项为Key，以返回值为Value，存入缓存。若Key为List，Value为List<*Model*>,则需要在*Model*中用@PyramidKey来指定对象中Key元素。目前仅支持一对一，多对多，还不支持一对多的情况（比如String--->List<*String*>）。
 ##### 参数列表
 
|参数名  |释义  |默认值  |
|--|--|--|
| cacheName |缓存空间名称  | “” |
| key |缓存标识  | “” |
| collection| 是否为集合  | false |
| collectionArgsIndex |方法入参参数数组下标  | 0 |
| onlyLocal |只使用本地缓存  | false |
| onlyDistributed |  只使用分布式缓存| false |
| redisExpiration |  redis缓存失效时间|-1023L（单位秒，-1为永久有效，[0,-]）|
| nativeExpiration |  本地缓存失效时间| 600(单位秒，[0,-])|
| refreshTime | 缓存刷新时间 | -1(单位秒，-1为不用刷新，[0,-]) |
<span id="jump"></span>
 ##### Model
 
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    String key;
    String name;

    @PyramidKey
    public String getKey(){
        return this.key;
    }
}
```

 
 ##### Single
```java
	@Pyramid(key = "#key",cacheName = "cachetest",collectionArgsIndex = 1,redisExpiration = 30,nativeExpiration = 30)
    public User fresh(Integer id,String key){
        return new User(key,key+"name");
    }

    @Pyramid(key = "#user.key",cacheName = "cachetest",redisExpiration = 30,nativeExpiration = 30)
    public User fresh(User user){
        return user;
    }
```
 ##### List
 [User](#jump)
```java
	/**
     * 这里是多对多的情况，且返回值为User对象的List
     * 这里需要在User中用@PyramidKey注明Key字段的get方法 {@link User}
     * @param users
     * @return
     */
	@Pyramid(key = "#users?.![key]",cacheName = "cachetest",collection = true,redisExpiration = 30,nativeExpiration = 30)
    public List<User> fresh(List<User> users){
        return users;
    }
```

 ## - 删除缓存@Del
 
 |参数名  |释义  |默认值  |
|--|--|--|
| cacheName |缓存空间名称  | “” |
| key |缓存标识  | “” |
| collection| 是否为集合  | false |

 ##### Single
```java
	@Del(key = "#user.key",cacheName = "cachetest")
    public User delete(User user){
        return user;
    }
```
 ##### List
```java
	@Del(key = "#users?.![key]",cacheName = "cachetest",collection = true)
    public List<User> delete(List<User> users){
        return users;
    }
```

