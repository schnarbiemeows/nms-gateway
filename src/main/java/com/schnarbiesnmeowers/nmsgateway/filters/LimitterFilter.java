package com.schnarbiesnmeowers.nmsgateway.filters;



public class LimitterFilter /*implements GlobalFilter, Ordered*/ {

    /*@Value("${limitter.url}")
    private String limitterUrl;

    private final WebClient webClient;

    private final LimitterClientUtility limitterClientUtility;

    public LimitterFilter(WebClient.Builder webClient,
                          LimitterClientUtility limitterClientUtility) {
        this.limitterClientUtility = limitterClientUtility;
        this.webClient = webClient.baseUrl(limitterUrl).build();
    }*/

    /**
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().toString();
        if(url.contains("//")) {
            // just in case its a full url
            url = url.substring(url.indexOf("/",url.indexOf("//")+2));
        }
        System.out.println("URI = " + url);
        String user = exchange.getRequest().getHeaders().get("user").get(0);
        List<String> permissions = Arrays.asList(user.substring(user.indexOf("["),user.indexOf("]"))
                .split(","));
        System.out.println("permissions = " + permissions);
        String ipAddress = exchange.getRequest().getRemoteAddress().toString();
        System.out.println("IP = " + ipAddress);
        RequestCheck check = new RequestCheck(url,ipAddress,permissions);
        try {
            Boolean proceed = limitterClientUtility.createPost(check).block();
            if(!proceed) {
                return Mono.empty();
            }
            return chain.filter(exchange);
        } catch (InsufficientPermissionsException e) {
            return Mono.empty();
        } catch (UrlNotFoundException e) {
            return Mono.empty();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    } **/
}
