package ru.yandex.practicum.shop.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shop.dto.ProductDto;

import java.io.IOException;
import java.util.List;

public class PageImplDeserializer extends JsonDeserializer<PageImpl<ProductDto>> {

    @Override
    public PageImpl<ProductDto> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        JsonNode contentNode = node.get("content");
        if (contentNode == null || contentNode.isNull()) {
            throw new JsonParseException(p, "Missing content in PageImpl JSON");
        }

        List<ProductDto> content = new ObjectMapper().readValue(contentNode.toString(), new TypeReference<>() {});

        JsonNode totalElementsNode = node.get("totalElements");
        if (totalElementsNode == null || totalElementsNode.isNull()) {
            throw new JsonParseException(p, "Missing totalElements in PageImpl JSON");
        }
        long totalElements = totalElementsNode.asLong();

        JsonNode pageNumberNode = node.get("pageNumber");
        JsonNode pageSizeNode = node.get("pageSize");
        if (pageNumberNode == null || pageSizeNode == null || pageNumberNode.isNull() || pageSizeNode.isNull()) {
            throw new JsonParseException(p, "Missing pagination information in PageImpl JSON");
        }
        int pageNumber = pageNumberNode.asInt();
        int pageSize = pageSizeNode.asInt();

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return new PageImpl<>(content, pageable, totalElements);
    }
}
