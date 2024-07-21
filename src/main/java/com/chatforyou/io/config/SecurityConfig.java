package com.chatforyou.io.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	public static final String ADMIN_COOKIE_NAME = "ovCallAdminToken";
	@Value("${CALL_USER}")
	private String CALL_USER;

	@Value("${CALL_SECRET}")
	private String CALL_SECRET;

	@Value("${CALL_PRIVATE_ACCESS}")
	private String CALL_PRIVATE_ACCESS;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// 기본 CORS 설정 및 CSRF 비활성화
		http.cors().and().csrf().disable();

		// 요청에 대한 권한 설정
		http.authorizeHttpRequests(auth -> {
			auth.requestMatchers("/call/**").permitAll();
			auth.requestMatchers("/auth/**").permitAll();
			// 유저 관련
			auth.requestMatchers("/user/**").permitAll();
			// 채팅방 관련
			auth.requestMatchers("/chatroom/**").permitAll();
			auth.requestMatchers("/sessions/**").permitAll();


			// CALL_PRIVATE_ACCESS 변수에 따라 추가 권한 설정
			if ("ENABLED".equals(CALL_PRIVATE_ACCESS)) {
				auth.requestMatchers("/recordings/**").authenticated();
				auth.requestMatchers("/sessions/**").authenticated();
			} else {
				System.out.println("PUBLIC ACCESS");
				auth.requestMatchers("/recordings/**").permitAll();
				auth.requestMatchers("/sessions/**").permitAll();
			}

			// 기타 요청은 인증이 필요함
			auth.anyRequest().authenticated();
		});

		// HTTP Basic 인증 사용
		http.httpBasic();

		return http.build();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList("*"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowedMethods(Arrays.asList("*"));
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
				.withUser(CALL_USER)
				.password("{noop}" + CALL_SECRET)
				.roles("ADMIN");
	}

	@Bean
	public UserDetailsService userDetailsService() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername(CALL_USER)
				.password("{noop}" + CALL_SECRET)
				.roles("ADMIN")
				.build());
		return manager;
	}
}
