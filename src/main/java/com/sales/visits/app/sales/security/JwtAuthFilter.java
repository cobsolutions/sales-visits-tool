package com.sales.visits.app.sales.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        log.info("RAW HEADER: [" + header + "]");
        log.info("TOKEN: [" + token + "]");
        log.info("VALID? " + jwtService.isValid(token));

//        if (jwtService.isValid(token)) {
//            String email = jwtService.extractEmail(token);
//
//            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//
//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(
//                            userDetails, null, userDetails.getAuthorities());
//            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//        }
        if (jwtService.isValid(token)) {
            try {
                String email = jwtService.extractEmail(token);
                System.out.println("EMAIL FROM TOKEN: [" + email + "]");

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("USER FOUND: " + userDetails.getUsername());
                System.out.println("AUTHORITIES: " + userDetails.getAuthorities());
                System.out.println("ENABLED: " + userDetails.isEnabled());
                System.out.println("ACCOUNT NON LOCKED: " + userDetails.isAccountNonLocked());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("AUTHENTICATION SET SUCCESSFULLY");
            } catch (Exception e) {
                System.out.println("EXCEPTION IN FILTER: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);

    }
}
