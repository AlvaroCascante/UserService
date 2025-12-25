package com.quetoquenana.userservice.util;

public class Constants {
    public static class Headers {
        public static final String APP_NAME = "X-Application-Name";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String AUTHORIZATION = "Authorization";
    }
    public static class Methods {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";
    }

    public static class Roles {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_USER = "ROLE_USER";
    }

    public static class OAuth2 {
        public static final String KEY_FACTORY_ALGORITHM = "RSA";

        public static final String TOKEN_CLAIM_SUB = "sub";
        public static final String TOKEN_CLAIM_ROLES = "roles";
    }
}
