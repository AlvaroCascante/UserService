package com.quetoquenana.userservice.controller;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwksController {

    private final JWKSource<SecurityContext> jwkSource;

    public JwksController(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() throws Exception {

        List<JWK> jwks = jwkSource.get(
                new JWKSelector(new JWKMatcher.Builder().build()),
                null
        );

        return Map.of(
                "keys",
                jwks.stream()
                        .map(JWK::toPublicJWK)
                        .map(JWK::toJSONObject)
                        .toList()
        );
    }
}