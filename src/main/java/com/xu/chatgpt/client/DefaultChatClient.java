
package com.xu.chatgpt.client;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.WeightRandom;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.Header;
import com.xu.chatgpt.apikey.OpenAiKeyWrapper;
import com.xu.chatgpt.constant.ChatConstant;
import com.xu.chatgpt.entity.audio.CreateAudioRequest;
import com.xu.chatgpt.entity.audio.CreateAudioResponse;
import com.xu.chatgpt.entity.billing.CreditGrantsResponse;
import com.xu.chatgpt.entity.billing.SubscriptionResponse;
import com.xu.chatgpt.entity.billing.UsageResponse;
import com.xu.chatgpt.entity.chat.ChatCompletionRequest;
import com.xu.chatgpt.entity.chat.ChatCompletionResponse;
import com.xu.chatgpt.entity.completions.CompletionRequest;
import com.xu.chatgpt.entity.completions.CompletionResponse;
import com.xu.chatgpt.entity.edit.EditRequest;
import com.xu.chatgpt.entity.edit.EditResponse;
import com.xu.chatgpt.entity.embeddings.EmbeddingRequest;
import com.xu.chatgpt.entity.embeddings.EmbeddingResponse;
import com.xu.chatgpt.entity.files.DeleteFileResponse;
import com.xu.chatgpt.entity.files.ListFileResponse;
import com.xu.chatgpt.entity.files.RetrieveFileResponse;
import com.xu.chatgpt.entity.files.UploadFileResponse;
import com.xu.chatgpt.entity.finetunes.*;
import com.xu.chatgpt.entity.image.CreateImageRequest;
import com.xu.chatgpt.entity.image.CreateImageResponse;
import com.xu.chatgpt.entity.image.CreateImageVariationRequest;
import com.xu.chatgpt.entity.model.ListModelsResponse;
import com.xu.chatgpt.entity.model.RetrieveModelResponse;
import com.xu.chatgpt.entity.moderations.ModerationRequest;
import com.xu.chatgpt.entity.moderations.ModerationResponse;
import com.xu.chatgpt.entity.users.UserResponse;
import com.xu.chatgpt.exception.NoAvailableKeyEvent;
import com.xu.chatgpt.exception.OpenAiException;
import com.xu.chatgpt.properties.OpenAiProperties;
import com.xu.chatgpt.properties.OpenAiUrl;
import com.xu.chatgpt.utils.JsonUtils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Validated
@RequiredArgsConstructor
@SuppressWarnings({"squid:S6539"})
public class DefaultChatClient implements ChatClient {

    private final OkHttpClient okHttpClient;
    private final OpenAiProperties openAiProperties;
    private final OpenAiKeyWrapper openAiKeyWrapper;
    private final UriTemplateHandler uriTemplateHandler;

    @Override
    public ModerationResponse moderations(ModerationRequest request) {
        return execute(OpenAiUrl.MODERATIONS, createRequestBody(request), ModerationResponse.class);
    }

    @Override
    public CompletionResponse completions(CompletionRequest request) {
        return execute(OpenAiUrl.COMPLETIONS, createRequestBody(request), CompletionResponse.class);
    }

    @Override
    public void streamCompletions(CompletionRequest request, EventSourceListener listener) {
        request.setStream(true);
        Request clientRequest = createRequest(OpenAiUrl.COMPLETIONS, createRequestBody(request));
        RealEventSource realEventSource = new RealEventSource(clientRequest, listener);
        realEventSource.connect(okHttpClient);
    }

    @Override
    public EditResponse edits(EditRequest request) {
        return execute(OpenAiUrl.EDITS, createRequestBody(request), EditResponse.class);
    }

    @Override
    public ChatCompletionResponse chatCompletions(ChatCompletionRequest request) {
        return execute(OpenAiUrl.CHAT_COMPLETIONS, createRequestBody(request), ChatCompletionResponse.class);
    }

    @Override
    public void streamChatCompletions(ChatCompletionRequest request, EventSourceListener listener) {
        request.setStream(true);
        Request clientRequest = createRequest(OpenAiUrl.CHAT_COMPLETIONS, createRequestBody(request));
        RealEventSource realEventSource = new RealEventSource(clientRequest, listener);
        realEventSource.connect(okHttpClient);
    }

    @Override
    public ListModelsResponse models() {
        return execute(OpenAiUrl.LIST_MODELS, null, ListModelsResponse.class);
    }

    @Override
    public RetrieveModelResponse retrieveModel(String modelId) {
        return execute(OpenAiUrl.RETRIEVE_MODEL, null, RetrieveModelResponse.class, modelId);
    }

    @Override
    public EmbeddingResponse embeddings(EmbeddingRequest request) {
        return execute(OpenAiUrl.EMBEDDINGS, createRequestBody(request), EmbeddingResponse.class);
    }

    @Override
    public ListFileResponse listFiles() {
        return execute(OpenAiUrl.LIST_FILES, null, ListFileResponse.class);
    }

    @Override
    @SneakyThrows
    public UploadFileResponse uploadFile(Resource fileResource, String purpose) {
        byte[] bytes = IoUtil.readBytes(fileResource.getInputStream());
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("purpose", purpose)
                .addFormDataPart("file", fileResource.getFilename(), RequestBody.create(bytes, ChatConstant.IMAGE_PNG))
                .build();
        return execute(OpenAiUrl.UPLOAD_FILE, multipartBody, UploadFileResponse.class);
    }

    @Override
    public DeleteFileResponse deleteFile(String fileId) {
        return execute(OpenAiUrl.DELETE_FILE, null, DeleteFileResponse.class, fileId);
    }

    @Override
    public RetrieveFileResponse retrieveFile(String fileId) {
        return execute(OpenAiUrl.RETRIEVE_FILE, null, RetrieveFileResponse.class, fileId);
    }

    @Override
    public CreateFineTuneResponse createFineTune(CreateFineTuneRequest request) {
        return execute(OpenAiUrl.CREATE_FINE_TUNE, createRequestBody(request), CreateFineTuneResponse.class);
    }

    @Override
    public ListFineTuneResponse listFineTunes() {
        return execute(OpenAiUrl.LIST_FINE_TUNE, null, ListFineTuneResponse.class);
    }

    @Override
    public RetrieveFineTuneResponse retrieveFineTunes(String fineTuneId) {
        return execute(OpenAiUrl.RETRIEVE_FINE_TUNE, null, RetrieveFineTuneResponse.class, fineTuneId);
    }

    @Override
    public CancelFineTuneResponse cancelFineTune(String fineTuneId) {
        return execute(OpenAiUrl.CANCEL_FINE_TUNE, RequestBody.create("", null), CancelFineTuneResponse.class, fineTuneId);
    }

    @Override
    public ListFineTuneEventResponse listFineTuneEvents(String fineTuneId) {
        return execute(OpenAiUrl.LIST_FINE_TUNE_EVENTS, null, ListFineTuneEventResponse.class, fineTuneId);
    }

    @Override
    public DeleteFineTuneModelResponse deleteFineTuneModel(String model) {
        return execute(OpenAiUrl.DELETE_FINE_TUNE_EVENTS, null, DeleteFineTuneModelResponse.class, model);
    }

    @Override
    public CreateAudioResponse createTranscription(Resource fileResource, CreateAudioRequest request) {
        MultipartBody multipartBody = createAudioBody(fileResource, request);
        return execute(OpenAiUrl.CREATE_TRANSCRIPTION, multipartBody, CreateAudioResponse.class);
    }

    @Override
    public CreateAudioResponse createTranslation(Resource fileResource, CreateAudioRequest request) {
        MultipartBody multipartBody = createAudioBody(fileResource, request);
        return execute(OpenAiUrl.CREATE_TRANSLATION, multipartBody, CreateAudioResponse.class);
    }

    @Override
    public CreateImageResponse createImage(CreateImageRequest request) {
        return execute(OpenAiUrl.CREATE_IMAGE, createRequestBody(request), CreateImageResponse.class);
    }

    @Override
    @SneakyThrows
    public CreateImageResponse createImageEdit(Resource image, Resource mask, CreateImageRequest request) {
        boolean imageIsPng = FileNameUtil.isType(image.getFilename(), ChatConstant.EXPECTED_IMAGE_TYPE);
        boolean maskIsPng = FileNameUtil.isType(mask.getFilename(), ChatConstant.EXPECTED_IMAGE_TYPE);
        Assert.isTrue(imageIsPng, "The image must png type.");
        Assert.isTrue(maskIsPng, "The mask must png type.");

        Assert.isTrue(image.contentLength() < ChatConstant.MAX_IMAGE_SIZE, "The image must less than 4MB.");
        Assert.isTrue(mask.contentLength() < ChatConstant.MAX_IMAGE_SIZE, "The mask must less than 4MB.");

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("image", image.getFilename(), createResourceBody(image));
        builder.addFormDataPart("mask", mask.getFilename(), createResourceBody(mask));

        PropertyMapper mapper = buildImageForm(request, builder);
        mapper.from(request.getPrompt()).to(prompt -> builder.addFormDataPart("prompt", prompt));
        return execute(OpenAiUrl.CREATE_IMAGE_EDIT, builder.build(), CreateImageResponse.class);
    }

    @Override
    @SneakyThrows
    public CreateImageResponse createImageVariation(Resource image, CreateImageVariationRequest request) {
        boolean imageIsPng = FileNameUtil.isType(image.getFilename(), ChatConstant.EXPECTED_IMAGE_TYPE);
        Assert.isTrue(imageIsPng, "The image must png type.");
        Assert.isTrue(image.contentLength() < ChatConstant.MAX_IMAGE_SIZE, "The image must less than 4MB.");

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("image", image.getFilename(), createResourceBody(image));

        buildImageForm(request, builder);
        return execute(OpenAiUrl.CREATE_IMAGE_VARIATION, builder.build(), CreateImageResponse.class);
    }

    @Override
    public CreditGrantsResponse billingCreditGrants() {
        return execute(OpenAiUrl.BILLING_CREDIT_GRANTS, null, CreditGrantsResponse.class);
    }

    @Override
    public UserResponse users(String organizationId) {
        return execute(OpenAiUrl.USERS, null, UserResponse.class, organizationId);
    }

    @Override
    public SubscriptionResponse billingSubscription() {
        return execute(OpenAiUrl.BILLING_SUBSCRIPTION, null, SubscriptionResponse.class);
    }

    @Override
    public UsageResponse billingUsage(String startDate, String endDate) {
        return execute(OpenAiUrl.BILLING_USAGE, null, UsageResponse.class, startDate, endDate);
    }

    @SneakyThrows
    private <S> S execute(OpenAiUrl openAiUrl, RequestBody requestBody, Class<S> responseType, Object... uriVariables) {
        Request clientRequest = createRequest(openAiUrl, requestBody, uriVariables);
        @Cleanup Response response = okHttpClient.newCall(clientRequest).execute();

        ResponseBody responseBody = response.body();
        Assert.notNull(responseBody, "Resolve response responseBody failed.");
        String responseBodyStr = responseBody.string();

        int code = response.code();
        Assert.isTrue(code >= 200 && code < 300, () -> {
            log.error("Response code: {}", code);
            log.error("Request message: {}", clientRequest);
            throw new OpenAiException(responseBodyStr);
        });

        return JsonUtils.parse(responseBodyStr, responseType);
    }

    private Request createRequest(OpenAiUrl openAiUrl, RequestBody requestBody, Object... uriVariables) {
        WeightRandom<String> weightRandom = openAiKeyWrapper.wrap();
        String apiKey = weightRandom.next();
        if (!StringUtils.hasText(apiKey)) {
            List<String> invalidedKeys = openAiKeyWrapper.getInvalidKeys();
            SpringUtil.publishEvent(new NoAvailableKeyEvent(this, invalidedKeys));
            throw new OpenAiException("No available api key.");
        }

        Map<OpenAiUrl, String> configUrls = openAiProperties.getUrls();
        String requestUrl = configUrls.get(openAiUrl);
        if (!StringUtils.hasText(requestUrl)) {
            requestUrl = openAiProperties.getDomain() + openAiUrl.getSuffix();
        }
        URI requestURI = uriTemplateHandler.expand(requestUrl, uriVariables);
        return new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.get(requestURI)))
                .headers(Headers.of(Header.AUTHORIZATION.name(), ChatConstant.BEARER.concat(apiKey), Header.CONTENT_TYPE.getValue(), ChatConstant.APPLICATION_JSON.toString()))
                .method(openAiUrl.getMethod(), requestBody)
                .build();
    }

    private RequestBody createRequestBody(Object request) {
        return RequestBody.create(JsonUtils.toJsonString(request), ChatConstant.APPLICATION_JSON);
    }

    @SneakyThrows
    private RequestBody createResourceBody(Resource resource) {
        return RequestBody.create(IoUtil.readBytes(resource.getInputStream()), ChatConstant.IMAGE_PNG);
    }

    private MultipartBody createAudioBody(Resource fileResource, CreateAudioRequest request) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("file", fileResource.getFilename(), createResourceBody(fileResource));

        PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        mapper.from(request.getModel()).to(model -> builder.addFormDataPart("model", model));
        mapper.from(request.getPrompt()).to(prompt -> builder.addFormDataPart("prompt", prompt));
        mapper.from(request.getResponseFormat()).to(format -> builder.addFormDataPart("response_format", format));
        mapper.from(request.getTemperature()).to(obj -> builder.addFormDataPart("temperature", obj.toString()));
        mapper.from(request.getLanguage()).to(language -> builder.addFormDataPart("language", language));
        return builder.build();
    }

    private PropertyMapper buildImageForm(CreateImageVariationRequest request, MultipartBody.Builder builder) {
        PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        mapper.from(request.getN()).to(n -> builder.addFormDataPart("n", n.toString()));
        mapper.from(request.getSize()).to(size -> builder.addFormDataPart("size", size.getValue()));
        mapper.from(request.getResponseFormat()).to(obj -> builder.addFormDataPart("response_format", obj.getValue()));
        mapper.from(request.getUser()).to(user -> builder.addFormDataPart("user", user));
        return mapper;
    }
}
