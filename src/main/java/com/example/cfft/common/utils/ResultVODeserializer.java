package com.example.cfft.common.utils;

import com.example.cfft.common.vo.ResultVO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ResultVODeserializer extends JsonDeserializer<ResultVO> {

    private final ObjectMapper objectMapper;

    public ResultVODeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ResultVO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        int code = node.get("code").asInt();
        String msg = node.get("msg").asText();
        JsonNode dataNode = node.get("data");

        Object data = null;
        if (dataNode != null && !dataNode.isNull()) {
            data = objectMapper.treeToValue(dataNode, Object.class);
        }

        return ResultVO.success(msg, data);
    }
}
