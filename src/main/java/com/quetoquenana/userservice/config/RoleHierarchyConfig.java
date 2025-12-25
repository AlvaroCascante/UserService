package com.quetoquenana.userservice.config;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static com.quetoquenana.userservice.util.Constants.Roles.*;

public class RoleHierarchyConfig implements RoleHierarchy {

    private final Map<String, Set<String>> implies = new HashMap<>();

    public RoleHierarchyConfig() {
        implies.put(ROLE_SYSTEM, Set.of(ROLE_ADMIN));
        implies.put(ROLE_ADMIN, Set.of(ROLE_USER));
    }

    @Override
    public Collection<GrantedAuthority> getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> reachable = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();

        for (GrantedAuthority grantedAuthority : authorities) {
            String role = grantedAuthority.getAuthority();
            if (role != null) {
                reachable.add(role);
                queue.add(role);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> children = implies.getOrDefault(current, Collections.emptySet());
            for (String child : children) {
                if (reachable.add(child)) {
                    queue.add(child);
                }
            }
        }

        List<GrantedAuthority> result = new ArrayList<>();
        for (String r : reachable) {
            result.add(new SimpleGrantedAuthority(r));
        }
        return result;
    }
}