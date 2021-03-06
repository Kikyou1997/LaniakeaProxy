# LaniakeaProxy 阶段性总结

由于andromeda项目存在诸多问题 包括非常不安全 极度丑陋的代码实现 连接无法及时释放以致占用用过多空间等等 故决定重做之

**已完成功能**:

1. 提供HTTP(s)的代理服务
2. 多用户认证
3. 用户使用流量的统计
4. 数据加密

**待做**:

1. 客户端自动选择延迟较低的代理服务器
2. 根据域名决定是否由代理服务器进行转发
3  支持更多的加密工具
4. 支持socks协议
5. 提供ui接界面（预计采用electron或者javafx实现）

## 配置

Linux环境下，配置文件应置于/etc/andromeda目录下，server的配置文件以server_config.json命名， client的配置文件以config.json命名

server sample:

```json
{
    "server_address":"127.0.0.1",
    "server_port":18081,
    "users":[
        {
            "username":"kikyou",
            "secret_key":"caIWbdpnMyjdbc8iU8x3eGNRLZ8tHxpH"
        }
    ]
}
```

client sample:

```json
{
    "server_address":"127.0.0.1",
    "server_port":18081,
    "local_port":1084,
    "username":"kikyou",
    "secret_key":"caIWbdpnMyjdbc8iU8x3eGNRLZ8tHxpH"
}
```

## 参数

| 参数  | 值  |
|---|---|
|-c| 指定配置文件所在的目录路径|
|-g| 生成密钥的数量|

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
|数据传输的响应报文 | [1 byte code ][4 bytes id ][ 4 bytes content length ][ encrypted data ]|
|时钟同步请求 |[1 byte code ]|
| 时钟同步的响应 |[ 1 byte code ][ 8 bytes value of time]|
| 隧道建立请求 |[ 1 byte req CONNECT code ][ 4 bytes id ][ 4 bytes content length ][ 若干字节的host ][ 2 bytes port ]|

## 本代理并不安全