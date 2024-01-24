## 简要

作为chat-bot的后端服务，通过对接大模型API，对外支持客户端调用

基于开源项目进行二次开发，调整部分代码

## 概览

1. 支持设置多个 API Key，并且支持对其设置权重以及是否启用，支持自动禁用失效的 API Key 以及自动轮转
2. 支持设置请求代理
3. 支持自定义请求 API（如果对 OpenAi 的 API 做了中转/代理）
4. 支持 OpenAi 所有可以使用 API Key 访问的 API
5. 支持流式响应，即所谓的"打字机"模式
6. 请求参数自动校验
7. 支持 token 计算
8. 支持 function calling

## 支持的功能

✅ 模型查询（Model）<br>
✅ 流式、非流式对话聊天（Stream Chat/completion）<br>
✅ 根据提示生成文本（Edit）<br>
✅ 自然语言转换为向量表示<br>
✅ 音频、视频语音转文本（Create transcription）<br>
✅ 文本翻译（Create translation）<br>
✅ 文件的查询、上传、删除（File - List/Upload/Delete/Retrieve）<br>
✅ 预训练模型的微调、查询、放弃、过程(事件)（Fine-tunes - Create/List/Retrieve/Cancel/Events）<br>
✅ 内容审核（Moderation）<br>
✅ 用户余额、使用量查询（Billing/Usage）<br>
✅ 用户信息查询（User）<br>
✅ 根据提示创建、编辑图像、根据图像生成多版本图像（Image - Create/Create edit/Create variation）

## 参考项目地址

- Github: https://github.com/lzhpo/chatgpt-spring-boot-starter

## 客户端项目地址

- Chatbox: https://github.com/Bin-Huang/chatbox

## 配置示例

### 1. 支持配置多个 API Key（权重、是否启用）

> 可以对当前 api key 设置权重，以及是否需要启用此 api key

#### 1.1 yaml配置方式

```yaml
gpt:
    keys:
        -   key: "sk-xxx1"
            weight: 1.0
            enabled: true
        -   key: "sk-xxx2"
            weight: 2.0
            enabled: false
        -   key: "sk-xxx3"
            weight: 3.0
            enabled: false
```

_支持自动禁用失效的 API Key 以及自动轮转，参考：`InvalidedKeyEvent`、`NoAvailableKeyEvent`、`OpenAiEventListener`_

**注意：每次请求都会调用此方法，有需要的话可以在此加一个缓存。**

### 2. 支持配置超时时间

```yaml
gpt:
    connect-timeout: 1m
    read-timeout: 1m
    write-timeout: 1m
```

### 3. 配置API地址

```yaml
gpt:
    domain: "https://api.baichuan-ai.com"
```

### 4. 支持token计算

示例1：

```java
Long tokens = TokenUtils.tokens(model, content);
```

示例2：`CompletionRequest`

```java
CompletionRequest request = new CompletionRequest();
// request.setXXX 略...
Long tokens = TokenUtils.tokens(request.getModel(), request.getPrompt());
```

OpenAi返回的token计算结果可在response返回体中获取：

- `prompt_tokens`：OpenAi计算的输入消耗的token
- `completion_tokens`：OpenAi计算的输出消耗的token
- `total_tokens`：`prompt_tokens` + `completion_tokens`

具体可参考测试用例`OpenAiCountTokensTest`以及`TokenUtils`

### 5. 支持 function calling

关于 function calling 的介绍：https://platform.openai.com/docs/guides/gpt/function-calling

### 6. 关于异常处理

1. 常规、SSE以及WebSocket请求失败均会抛出`OpenAiException`异常，可自定义全局异常，取出OpenAi的响应结果转换为`OpenAiError`(
   如果转换结果`OpenAiError`不为空)，继而自行处理。
2. 自定义流式处理的`EventSourceListener`，推荐继承`AbstractEventSourceListener`，如果没有特殊需求，直接重写`onEvent`
   方法即可，如果重写了`onFailure`方法，抛出何种异常取决于重写的`onFailure`方法。

## 参考

### 1. 流式输出

#### 1.1 SSE方式示例

** SSE（Server-Sent Events）**

SSE和WebSocket都是用于实现服务器和浏览器之间实时通信的技术。
WebSocket是全双工通信协议，适用于双向通信的实时场景，而SSE是单向通信协议，适用于服务器向客户端推送消息的实时场景。

### 2. 自定义请求拦截器

实现`okhttp3.Interceptor`接口，并将其声明为bean即可。