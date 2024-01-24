/*
 * Copyright 2023 lzhpo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xu.chatgpt.client;

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
import okhttp3.sse.EventSourceListener;
import org.springframework.core.io.Resource;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface ChatClient {

    /**
     * Create moderation.
     *
     * @param request {@link ModerationRequest}
     * @return {@link ModerationResponse}
     */
    ModerationResponse moderations(@Valid ModerationRequest request);

    /**
     * Create completion.
     */
    @Deprecated
    CompletionResponse completions(@Valid CompletionRequest request);

    /**
     * Create stream completion.
     */
    @Deprecated
    void streamCompletions(@Valid CompletionRequest request, @NotNull EventSourceListener listener);

    /**
     * Create edit.
     */
    EditResponse edits(@Valid EditRequest request);

    /**
     * Create chat completion.
     */
    ChatCompletionResponse chatCompletions(@Valid ChatCompletionRequest request);

    /**
     * Create stream chat completion.
     */
    void streamChatCompletions(@Valid ChatCompletionRequest request, @NotNull EventSourceListener listener);

    /**
     * List models.
     *
     * @return {@link ListModelsResponse}
     */
    ListModelsResponse models();

    /**
     * Retrieve model by {@code modelId}.
     *
     * @param modelId modelId
     * @return {@link RetrieveModelResponse}
     */
    RetrieveModelResponse retrieveModel(@NotBlank String modelId);

    /**
     * Create embeddings.
     *
     * @param request {@link EmbeddingRequest}
     * @return {@link EmbeddingResponse}
     */
    EmbeddingResponse embeddings(@Valid EmbeddingRequest request);

    /**
     * List files.
     *
     * @return {@link ListFileResponse}
     */
    ListFileResponse listFiles();

    /**
     * Upload file.
     *
     * @param fileResource {@link Resource}
     * @param purpose      purpose
     * @return {@link UploadFileResponse}
     */
    UploadFileResponse uploadFile(@NotNull Resource fileResource, @NotBlank String purpose);

    /**
     * Delete file by {@code fileId}.
     *
     * @param fileId fileId
     * @return {@link DeleteFileResponse}
     */
    DeleteFileResponse deleteFile(@NotBlank String fileId);

    /**
     * Retrieve file by {@code fileId}
     *
     * @param fileId fileId
     * @return {@link RetrieveFileResponse}
     */
    RetrieveFileResponse retrieveFile(@NotBlank String fileId);

    /**
     * Create fine-tune.
     *
     * @param request {@link CreateFineTuneRequest}
     * @return {@link CreateFineTuneResponse}
     */
    CreateFineTuneResponse createFineTune(@Valid CreateFineTuneRequest request);

    /**
     * List fine-tunes.
     *
     * @return {@link ListFineTuneResponse}
     */
    ListFineTuneResponse listFineTunes();

    /**
     * Retrieve fine-tune.
     *
     * @param fineTuneId fineTuneId
     * @return {@link RetrieveFineTuneResponse}
     */
    RetrieveFineTuneResponse retrieveFineTunes(@NotBlank String fineTuneId);

    /**
     * Cancel fine-tune by {@code fineTuneId}.
     *
     * @param fineTuneId fineTuneId
     * @return {@link CancelFineTuneResponse}
     */
    CancelFineTuneResponse cancelFineTune(@NotBlank String fineTuneId);

    /**
     * List fine-tune events.
     *
     * @param fineTuneId fineTuneId
     * @return {@link ListFineTuneEventResponse}
     */
    ListFineTuneEventResponse listFineTuneEvents(@NotBlank String fineTuneId);

    /**
     * Delete fine-tune model.
     *
     * @param model model
     * @return {@link DeleteFineTuneModelResponse}
     */
    DeleteFineTuneModelResponse deleteFineTuneModel(@NotBlank String model);

    /**
     * Create transcription.
     *
     * @param fileResource The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
     * @param request      {@link CreateAudioRequest}
     * @return {@link CreateAudioResponse}
     */
    CreateAudioResponse createTranscription(@NotNull Resource fileResource, @Valid CreateAudioRequest request);

    /**
     * Create translation.
     *
     * @param fileResource The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
     * @param request      {@link CreateAudioRequest}
     * @return {@link CreateAudioResponse}
     */
    CreateAudioResponse createTranslation(@NotNull Resource fileResource, @Valid CreateAudioRequest request);

    /**
     * Create image.
     *
     * @param request {@link CreateImageRequest}
     * @return {@link CreateImageResponse}
     */
    CreateImageResponse createImage(@Valid CreateImageRequest request);

    /**
     * Create image edit.
     *
     * @param image   {@link Resource}
     * @param mask    {@link Resource}
     * @param request {@link CreateImageRequest}
     * @return {@link CreateImageResponse}
     */
    CreateImageResponse createImageEdit(
            @NotNull Resource image, @NotNull Resource mask, @Valid CreateImageRequest request);

    /**
     * Create image variation.
     *
     * @param image   {@link Resource}
     * @param request {@link CreateImageRequest}
     * @return {@link CreateImageResponse}
     */
    CreateImageResponse createImageVariation(@NotNull Resource image, @Valid CreateImageVariationRequest request);

    /**
     * Query billing credit grants.
     *
     * @return {@link CreditGrantsResponse}
     */
    CreditGrantsResponse billingCreditGrants();

    /**
     * Get users.
     *
     * @param organizationId organizationId
     * @return {@link UserResponse}
     */
    UserResponse users(@NotBlank String organizationId);

    /**
     * Query billing subscription.
     *
     * @return {@link SubscriptionResponse}
     */
    SubscriptionResponse billingSubscription();

    /**
     * Query billing usage.
     *
     * @param startDate startDate
     * @param endDate   endDate
     * @return {@link UsageResponse}
     */
    UsageResponse billingUsage(@NotBlank String startDate, @NotBlank String endDate);
}
