package com.schnarbiesnmeowers.nmsgateway.utilities;

import org.springframework.stereotype.Component;

@Component
public class LimitterClientUtility {

    /*private final WebClient webClient;

    @Value("${limitter.url}")
    private String url;

    public LimitterClientUtility(WebClient.Builder webClient) {
        this.webClient = webClient.baseUrl(url).build();
    }

    public Mono<Boolean> createPost(RequestCheck postRequest) throws InsufficientPermissionsException,
            UrlNotFoundException {
            return webClient.post()
                    .uri("/limiter/validate")
                    .body(Mono.just(postRequest), RequestCheck.class)  // Set the body to the PostRequest object
                    .retrieve()
                    .bodyToMono(Boolean.class);
    }*/

}
