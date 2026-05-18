package com.portly.service;

public interface OAuthProvider {
    String getProviderName();
    String getAuthorizationUrl();
    String exchangeCodeForToken(String code);
    OAuthUserInfo fetchUserInfo(String accessToken);
}
