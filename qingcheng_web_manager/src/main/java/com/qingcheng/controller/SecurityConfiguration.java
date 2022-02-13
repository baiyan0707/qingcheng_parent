package com.qingcheng.controller;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

//@EnableWebSecurity
public class SecurityConfiguration {


    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/*/find*.do").permitAll()
                .antMatchers("/goods/*.do").hasAuthority("goods")
                .and()
                .requiresChannel().antMatchers("/**").requiresInsecure();
    }
}
