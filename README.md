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

### 排查

* 2020-2-26 

在抓包时发现在运行一段时间后发起ssl/tls握手时，在客户端发送了clientHello到真正的服务器后，在代理客户端解密由代理服务端发回的加密的server hello时，解密虽然成功，但却和原报文并不完全一致，具体的来讲，这次发现头16字节与原报文不符

最终导致Ignored unknown record，表现为浏览器会显示 *** cannot provide a secure connection

同时在抓包过程中还发现了代理服务端向客户端发送TCP windows full 这倒验证了我的猜想，以及说明了有时会出现*** take too long to response的问题

![](./Screenshot_2020-02-26_23-08-18.png)

接下来的问题就是接收缓冲（receive buffer）为什么会如此之快的被填满，目前想到的可能性是应用由于某种原因没有即使的处理已收到的消息或者是对端消息发送过快，但结合andromeda的经验，我认为后者的关系不大。~~但如果是前者的话还有一个问题是目前64bit linux系统下TCP默认接受缓冲区我记得默认有4Mb 即使应用程序没有即使处理也不应该连页面都还没显示就用没了~~ 但是这可能就是问题的关键所在，我们知道通常浏览器为了加速访问会对一个host建立多条tcp链接 但是我的处理逻辑是这样的

```java
super.channel = ctx.channel();
        boolean httpConnect = checkHttpHeader(msg);
        if (httpConnect && p2SConnection != null) {
            channel.writeAndFlush(generateHttpConnectionEstablishedResponse());
        }
        if (!tunnelBuilt) {
            tunnelBuilt = buildTunnel(msg, httpConnect);
            return;
        }
        if (p2SConnection != null) {
            msg.readerIndex(0);
            var encrypted = crypto.encrypt(msg);
            var buf = PooledByteBufAllocator.DEFAULT.buffer();
            buf.writeByte(RequestCode.DATA_TRANS_REQ);
            buf.writeInt(id);
            buf.writeInt(encrypted.readableBytes());
            buf.writeBytes(encrypted);
            boolean r = p2SConnection.writeData(buf).syncUninterruptibly().isSuccess();
        }
```

可以看到，当浏览器试图通过发送CONNECT发送建立链接的请求的时候，我们虽然会发送一个200,但实际上只会建立一条到服务端的链接，如果多条浏览器和代理客户端的tcp连接上的数据，代理客户端都通过tcp一条链接转发岛代理服务端上的话，那么就可能出现这种情况。当然目前都是推测，具体如何得等我改了之后再看。

但这么解释的话还是有一个问题，那就是C_CHandler是非共享的，也就是每条连接都会对应一个，也就是说每次浏览器同代理客户端建立tcp链接的时候，都会有一个新的实例来处理，那么上面的代码的处理逻辑似乎就没有什么问题。

### 问题的解决

过去3天并没有想到问题之所在

但在试图解决的时候 发现http tunnel下 浏览器对同一host建立多条tcp连接时 会通过在多条tcp连接上发送多次connect请求的方式来实现？

### 问题描述二

像andromeda一样尚未对ByteBuf进行合理的回收 但对比andromeda的情况 认为这个问题并不是导致问题一的原因 这个问题比较容易解决 先记着

### 问题解决

等上个问题解决了再说
