package com.xu.chatgpt.config;

import cn.hutool.core.util.StrUtil;
import com.xu.chatgpt.apikey.OpenAiKeyProvider;
import com.xu.chatgpt.apikey.OpenAiKeyWrapper;
import com.xu.chatgpt.client.DefaultChatClient;
import com.xu.chatgpt.exception.OpenAiErrorInterceptor;
import com.xu.chatgpt.properties.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({OpenAiProperties.class})
public class ChatConfiguration {

    private final OpenAiProperties openAiProperties;

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        mapper.from(interceptors).to(x -> builder.interceptors().addAll(x));
        mapper.from(openAiProperties::getReadTimeout).to(builder::readTimeout);
        mapper.from(openAiProperties::getWriteTimeout).to(builder::writeTimeout);
        mapper.from(openAiProperties::getConnectTimeout).to(builder::connectTimeout);
        return Optional.ofNullable(openAiProperties.getProxy())
                .map(proxy -> {
                    String username = proxy.getUsername();
                    String password = proxy.getPassword();
                    mapper.from(() -> StrUtil.isAllNotBlank(username, password))
                            .whenTrue()
                            .toCall(() -> builder.proxyAuthenticator((route, response) -> response.request()
                                    .newBuilder()
                                    .header(proxy.getHeaderName(), Credentials.basic(username, password))
                                    .build()));
                    builder.proxy(new Proxy(proxy.getType(), new InetSocketAddress(proxy.getHost(), proxy.getPort())));
                    return builder.build();
                })
                .orElseGet(builder::build);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultChatClient chatClient(
            OkHttpClient okHttpClient,
            OpenAiKeyWrapper openAiKeyWrapper,
            ObjectProvider<UriTemplateHandler> uriTemplateHandlerObjectProvider) {

        UriTemplateHandler uriTplHandler = uriTemplateHandlerObjectProvider.getIfAvailable(() -> {
            DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
            uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
            return uriBuilderFactory;
        });
        return new DefaultChatClient(okHttpClient, openAiProperties, openAiKeyWrapper, uriTplHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiKeyWrapper openAiKeyWrapper(OpenAiKeyProvider openAiKeyProvider) {
        return new OpenAiKeyWrapper(openAiKeyProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiKeyProvider openAiKeyProvider() {
        return openAiProperties::getKeys;
    }

    @Bean
    public OpenAiErrorInterceptor openAiErrorInterceptor(OpenAiKeyWrapper openAiKeyWrapper) {
        return new OpenAiErrorInterceptor(openAiKeyWrapper);
    }
}
