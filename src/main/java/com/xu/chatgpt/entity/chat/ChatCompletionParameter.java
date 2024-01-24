package com.xu.chatgpt.entity.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author lzhpo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionParameter {

    /**
     * The parameter type.
     */
    private String type;

    /**
     * The parameter properties.
     */
    private Object properties;

    /**
     * The required properties entry from {@link ChatCompletionParameter#properties}.
     */
    private List<String> required;
}
