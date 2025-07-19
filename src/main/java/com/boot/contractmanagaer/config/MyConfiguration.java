package com.boot.contractmanagaer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfiguration {

	@Autowired
	private UserDetailsService userdetailservice;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(userdetailservice);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            .requestMatchers("/normal/**", "/user/**").hasRole("USER")
	            .requestMatchers("/", "/signup", "/do_register", "/login","/send-otp","/dashboard","/forgot","/about", "/verify-otp","/change-password","/css/**", "/js/**", "/img/**")
	            .permitAll()
	            .anyRequest().authenticated())
	        .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/dologin").defaultSuccessUrl("/user/index", true).failureUrl("/faillogin").permitAll())
	        .logout(logout -> logout.permitAll());
//	        .debug(true); // Enable debug mode for Spring Security

	    return http.build();
	}



}
