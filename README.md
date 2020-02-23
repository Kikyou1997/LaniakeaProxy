# LaniakeaProxy 阶段性总结

由于andromeda项目存在诸多问题 包括非常不安全 极度丑陋的代码实现 连接无法及时释放以致占用用过多空间等等 故决定重做之

**目标功能**:

1. 提供HTTP(s)的代理服务
2. 多用户认证
3. 用户使用流量的统计
4. 数据加密

## 关于认证流程

客户端保存: 用户名 密钥

* 流程

1. 客户端发起请求同步时钟 请求格式: ` |1 byte_code| `
2. 客户端发起请求 包括用户名 以及SHA256加盐(salt为同步到的时钟值)hash过的密钥
3. 服务端根据用户名找到该用户的密钥 并进行hash 对比密钥是否合法
4. 合法则返回一个随机数和id 否则关闭连接

所以请求认证的报文格式是这样 ` | 1 byte code | 32 byte hash | username | `

响应报文格式 `| 1 byte code | 4 byte id | 16 byte iv |`

## 各类报文的格式汇总

| 报文类型  | 格式  |
|---|---|
| 认证请求  | [ 1 byte code ][ 32 bytes hash ][ username ]  |
|认证请求的响应报文 |[ 1 byte code ][ 4 bytes id ][ 16 bytes iv ]|
| 数据传输报文 |[ 1 byte req code ][ 4 bytes id ][ 4 bytes content length][ encrypted data ]|
|数据传输的响应报文 | [1 byte code ][ 4 bytes content length ][ encrypted data ]|
|时钟同步请求 |[1 byte code ]|
| 时钟同步的响应 |[ 1 byte code ][ 8 bytes value of time]|
| 隧道建立请求 |[ 1 byte req CONNECT code ][ 4 bytes id ][ 4 bytes content length ][ 若干字节的host ][ 2 bytes port ]|

## 一些问题 2020-2-23

### 问题描述一

当启动代理后 浏览器在打开多个tab的情况下只有个别网页可以正常跑 而其他网页则一直处于转圈而无任何实际的响应和页面并最终会返回一个take too long to response 且如果代理服务端试图和某个host建立连接时出现connection reset的情况则整个代理都可能处于不可用的情况

而且整个代理 速度奇慢无比

更为需要注意的一个现象是 proxyServer可能由于某种原因拒绝任何proxyClient的连接 初次发现这个问题是在proxyClient重启 而proxyServer不动的情况下 proxyClient无法从proxyServer获得任何的认证响应 proxyClient始终卡在waiting client context initialized这里

所以推断问题处在proxyServer上面

### 问题的解决

过去3天并没有想到问题之所在

但在试图解决的时候 发现http tunel下 浏览器对同一host建立多条tcp连接时 会通过在多条tcp连接上发送多次connect请求的方式来实现

### 问题描述二

像andromeda一样尚未对ByteBuf进行合理的回收 但对比andromeda的情况 认为这个问题并不是导致问题一的原因 这个问题比较容易解决 先记者

### 问题解决

等上个问题解决了再说
