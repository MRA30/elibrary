package com.elibrary.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.elibrary.Constans;
import com.elibrary.services.UserService;
import com.elibrary.utils.JwtUtil;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
    
    Cookie[] cookies = httpServletRequest.getCookies();
    String userName = null;
    String token = null;

    if(cookies != null) {
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(Constans.ACCESS_TOKEN)) {
                token = cookie.getValue();
                userName = jwtUtil.extractEmail(cookie.getValue());
            }
        }
    }
    if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        UserDetails userDetails = userService.loadUserByUsername(userName);

        if (jwtUtil.validateToken(token, userDetails)) {

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
}
}

