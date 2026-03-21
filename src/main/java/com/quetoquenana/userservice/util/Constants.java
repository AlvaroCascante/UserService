package com.quetoquenana.userservice.util;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static class JWTClaims {
        public static final String KEY_FACTORY_ALGORITHM = "RSA";
        public static final String KEY_NAME = "name";
        public static final String KEY_ROLES = "roles";
        public static final String KEY_TYPE = "type";
        public static final String KEY_USER_ID = "userId";

        public static final String KEY_SUB = "sub";

        public static final String ROLE_SUFFIX = "ROLE_";

        public static final String TYPE_AUTH = "auth";
        public static final String TYPE_REFRESH = "refresh";
    }

    public static class Dates {
        public static final String YEAR_MONTH_FORMAT = "yyyy-MM";
        public static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT);
    }

    public static class Emails {
        public static final String TEMPLATES_PATH = "templates/email/";
        public static final String SUFFIX = ".html";
        public static final String TEMPLATE_MODE = "HTML";
    }

    public static class Headers {
        public static final String APP_NAME = "X-Application-Name";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class Logging {
        public static final String ENV_PROPERTY = "app.environment";
        public static final String ENV_PROPERTY_DEFAULT = "env unknown";
        public static final String MDC_ENV_KEY = "env";
    }

    public static class MessageSource {
        public static final String BASENAME = "classpath:messages";
        public static final String DEFAULT = "UTF-8";
    }

    public static class Pagination {
        public static final String PAGE = "0";
        public static final String PAGE_SIZE = "10";
    }

    public static class Roles {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_NAME_ADMIN = "ADMIN";
        public static final String ROLE_NAME_USER = "USER";
        public static final String ROLE_USER = "ROLE_USER";
    }
}
