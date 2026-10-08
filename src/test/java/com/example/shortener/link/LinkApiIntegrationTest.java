package com.example.shortener.link;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LinkApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String uniqueAlias() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private void createLink(String url, String alias) throws Exception {
        String json = "{\"url\":\"" + url + "\",\"alias\":\"" + alias + "\"}";
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void createLink_returns201WithShortUrl() throws Exception {
        String alias = uniqueAlias();
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://www.wikipedia.org\",\"alias\":\"" + alias + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(alias))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/" + alias))
                .andExpect(jsonPath("$.originalUrl").value("https://www.wikipedia.org"));
    }

    @Test
    void createLink_withoutAlias_generatesSevenCharCode() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://www.wikipedia.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.hasLength(7)));
    }

    @Test
    void createLink_invalidUrl_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"ftp://x.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.url").exists());
    }

    @Test
    void createLink_duplicateAlias_returns409() throws Exception {
        String alias = uniqueAlias();
        createLink("https://www.wikipedia.org", alias);

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.org\",\"alias\":\"" + alias + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Alias already taken"));
    }

    @Test
    void redirect_returns302WithLocationHeader() throws Exception {
        String alias = uniqueAlias();
        createLink("https://www.wikipedia.org", alias);

        mockMvc.perform(get("/" + alias))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.wikipedia.org"));
    }

    @Test
    void redirect_unknownCode_returns404InErrorFormat() throws Exception {
        mockMvc.perform(get("/doesnotexist1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Link not found"))
                .andExpect(jsonPath("$.path").value("/doesnotexist1"));
    }

    @Test
    void redirect_expiredLink_returns410() throws Exception {
        String alias = uniqueAlias();
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.org\",\"alias\":\"" + alias
                                + "\",\"expiresAt\":\"2020-01-01T00:00:00Z\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/" + alias))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Link has expired"));
    }

    @Test
    void stats_countsClicks() throws Exception {
        String alias = uniqueAlias();
        createLink("https://www.wikipedia.org", alias);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/" + alias)).andExpect(status().isFound());
        }

        // Clicks are saved asynchronously, so poll for up to 5 seconds
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            String body = mockMvc.perform(get("/api/links/" + alias + "/stats"))
                    .andReturn().getResponse().getContentAsString();
            if (body.contains("\"totalClicks\":3")) {
                break;
            }
            Thread.sleep(100);
        }

        mockMvc.perform(get("/api/links/" + alias + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(3))
                .andExpect(jsonPath("$.topReferrers[0].referrer").value("direct"));
    }

    @Test
    void stats_unknownCode_returns404() throws Exception {
        mockMvc.perform(get("/api/links/nope12345/stats"))
                .andExpect(status().isNotFound());
    }
}