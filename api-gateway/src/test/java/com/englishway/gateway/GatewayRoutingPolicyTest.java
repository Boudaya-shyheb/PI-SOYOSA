package com.englishway.gateway;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

class GatewayRoutingPolicyTest {

    @Test
    void allGatewayRoutesMustUseServiceDiscoveryUris() throws IOException {
        ClassPathResource resource = new ClassPathResource("application.yml");
        String yaml = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        List<String> routeUriLines = yaml.lines()
            .map(String::trim)
            .filter(line -> line.startsWith("uri:"))
            .collect(Collectors.toList());

        assertFalse(routeUriLines.isEmpty(), "No gateway route URIs found in application.yml");

        List<String> invalidUris = routeUriLines.stream()
            .map(line -> line.substring("uri:".length()).trim())
            .filter(uri -> !uri.startsWith("lb://"))
            .collect(Collectors.toList());

        assertTrue(
            invalidUris.isEmpty(),
            "All gateway routes must use lb:// service discovery URIs. Invalid URIs: " + invalidUris
        );

        assertFalse(
            yaml.contains("uri: http://") || yaml.contains("uri: https://"),
            "Direct HTTP(S) route targets are forbidden. Route through service discovery using lb:// only."
        );
    }
}
