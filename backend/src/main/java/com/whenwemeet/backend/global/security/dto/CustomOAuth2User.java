package com.whenwemeet.backend.global.security.dto;

import com.whenwemeet.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> "ROLE_" + user.getRole());
        return collection;
    }

    @Override
    public String getName() {
        return user.getNickname();
    }

    public Long getId(){
        return user.getId() == null ? null : user.getId();
    }
}
