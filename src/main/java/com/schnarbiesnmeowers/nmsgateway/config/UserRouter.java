package com.schnarbiesnmeowers.nmsgateway.config;

import com.schnarbiesnmeowers.nmsgateway.controllers.UserController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


import static org.springframework.web.reactive.function.server.RequestPredicates.*;

//@Configuration(proxyBeanMethods = false)
public class UserRouter {

    /*@Bean
    public RouterFunction<ServerResponse> route(UserController userController) {

        return RouterFunctions
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register)
                .route(POST("/user/register").and(accept(MediaType.APPLICATION_JSON)),
                        userController::register);
    }*/
}
