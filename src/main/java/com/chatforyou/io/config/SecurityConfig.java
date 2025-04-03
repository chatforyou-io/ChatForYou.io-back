package com.chatforyou.io.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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
		// 기본 CSRF 비활성화
		http.csrf().disable();
		http.addFilter(this.corsFilter());

		// 모든 요청은 이 필터를 타게된다(cross-origin 요청이 와도 모두 허용이됨).
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션 사용하지 않고 stateless 서버로 만들겠다!
				.and()
				.formLogin().disable()  // jwt서버 사용. 폼로그인 사용X
				.httpBasic().disable()  // http 로그인 방식X
				.authorizeRequests()
				.anyRequest()
				.permitAll();  // 다른 요청 권한 없이 접근 가능

		return http.build();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);  // 내서버가 응답할때 json을 js에서 처리할 수 있게 설정
		config.addAllowedOrigin("*");  // 모든 ip의 응답을 허용
		config.addAllowedHeader("*");  // 모든 header의 응답을 허용
		config.addAllowedMethod("*");  // 모든 post, get, delete, patch요청을 허용하겠다
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
