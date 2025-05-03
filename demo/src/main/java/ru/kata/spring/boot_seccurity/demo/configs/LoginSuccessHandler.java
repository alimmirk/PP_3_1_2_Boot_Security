package ru.kata.spring.boot_seccurity.demo.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request
            , HttpServletResponse response
            , Authentication authentication) throws IOException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin");
                return;
            }
        }

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_USER")) {
                response.sendRedirect("/user");
                return;
            }
        }

        response.sendRedirect("/");
    }
}
