package eu.opertusmundi.common.feign.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@ConditionalOnProperty(name = "opertusmundi.feign.wordpress.name")
@FeignClient(
    name = "${opertusmundi.feign.wordpress.name}",
    url = "${opertusmundi.feign.wordpress.url}"
)
public interface WordPressFeignClient {

    /**
     * Retrieve a Post
     *
     * @param slug An alphanumeric identifier for the object unique to its type
     * @return A JSON object with post data
     *
     * @see <a href="https://developer.wordpress.org/rest-api/reference/posts/#retrieve-a-post">Retrieve a Post</a>
     */
    @GetMapping(value = "/wp/v2/blog?_embed", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> getPost(@RequestParam("slug") String slug);

}
