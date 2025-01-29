package com.emt.dms1.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {


    public static String getCurrentUserLogin()
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null)
        {
            if (authentication.getPrincipal() instanceof UserDetails)
            {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            }
            else if (authentication.getPrincipal() instanceof String){
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

//    /**
//     * Check if a user is authenticated.
//     *
//     * @return true if the user is authenticated, false otherwise
//     */
//    public static boolean isAuthenticated()
//    {
//        SecurityContext securityContext = SecurityContextHolder.getContext();
//        Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
//        if (authorities != null)
//        {
//            for (GrantedAuthority authority : authorities)
//            {
//                if (authority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS))
//                    return false;
//            }
//        }
//        return true;
//    }
    public static Object isCurrentUserInRole(String authority)
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null)
        {
            if (authentication.getPrincipal() instanceof UserDetails)
            {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                return null/*springSecurityUser.getAuthorities().contains(new SimpleGrantedAuthority(authority))*/;
            }
        }
        return false;
    }
}