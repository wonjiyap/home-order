package com.wonjiyap.homeorder.config

import com.wonjiyap.homeorder.interceptor.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authInterceptor: AuthInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",        // 로그인, 회원가입
                "/api/parties/join",   // 게스트 파티 참여
                "/api/docs/**",        // Swagger UI
                "/api-docs/**",        // OpenAPI JSON
                "/swagger-ui/**",      // Swagger 리소스
                "/swagger-resources/**",
            )
    }
}
