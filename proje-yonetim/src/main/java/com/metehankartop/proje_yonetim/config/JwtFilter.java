package com.metehankartop.proje_yonetim.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        String method = req.getMethod();

        System.out.println("JwtFilter - İstek: " + method + " " + path);

        // CORS preflight requests için OPTIONS metodunu handle et
        if ("OPTIONS".equals(method)) {
            res.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }

        // Auth istekleri filtreyi atlasın
        if (path.startsWith("/api/auth/")) {
            System.out.println("Auth endpoint, filter atlanıyor");
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token alındı: " + token.substring(0, Math.min(20, token.length())) + "...");

            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractAllClaims(token).get("role", String.class);

                    System.out.println("Token geçerli - Username: " + username + ", Role: " + role);

                    String springSecurityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    System.out.println("Spring Security rolü: " + springSecurityRole);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority(springSecurityRole))
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("Authentication başarılı!");
                } else {
                    System.err.println("Token geçersiz!");
                }
            } catch (Exception e) {
                System.err.println("Token işleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Authorization header yok veya Bearer ile başlamıyor");
        }

        chain.doFilter(request, response);
    }
}