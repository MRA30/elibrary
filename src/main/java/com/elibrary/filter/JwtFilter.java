package com.elibrary.filter;

import com.elibrary.services.UserService;
import com.elibrary.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

//    @Override
//    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
//            throws ServletException, IOException {
//
//    Cookie[] cookies = httpServletRequest.getCookies();
//    String email = null;
//    String token = null;
//
//    if(cookies != null) {
//        for(Cookie cookie : cookies) {
//            if(cookie.getName().equals(Constans.ACCESS_TOKEN)) {
//                token = cookie.getValue();
//                email = jwtUtil.extractEmail(cookie.getValue());
//            }
//        }
//    }
//    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//        UserDetails userDetails = (UserDetails) userService.findByEmail(email);
//
//        if (jwtUtil.validateToken(token, userDetails)) {
//
//            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
//                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            usernamePasswordAuthenticationToken
//                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
//            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//        }
//    }
//    filterChain.doFilter(httpServletRequest, httpServletResponse);
//}
}

