package by.bk.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Sergey Koval
 */
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class BookkeeperSecurity {
    private static final DispatcherType[] SUPPORTED_DISPATCHER_TYPES = {DispatcherType.ASYNC, DispatcherType.ERROR};

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter authenticationFilter) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(registry -> registry
                .dispatcherTypeMatchers(SUPPORTED_DISPATCHER_TYPES).permitAll()
                .requestMatchers(HttpMethod.GET, "/token/server/version", "/mobile-app/android").permitAll()
                .requestMatchers(HttpMethod.POST, "/token/generate-token", "/token/generate-token-mobile", "/token/send-registration-code", "/token/review-registration-code", "/logs").permitAll()
                .requestMatchers(HttpMethod.GET).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(configurer -> configurer
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(unauthorizedHandler))
            .build();
    }


//    @Value("${bookkeeper.require.http}")
//    private Boolean requireHttp;
//
//    @Autowired
//    private JwtAuthenticationFilter authenticationFilter;
//    @Autowired
//    private JwtAuthenticationEntryPoint unauthorizedHandler;
//    @Autowired
//    @Qualifier("userService")
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(passwordEncoderBean());
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        if (!BooleanUtils.toBoolean(requireHttp)) {
//            http.requiresChannel().anyRequest().requiresSecure();
//        }
//
//        http
//                .csrf().disable()
//                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .authorizeRequests()
//                .antMatchers(HttpMethod.GET, "/token/server/version").permitAll()
//                .antMatchers(HttpMethod.GET, "/mobile-app/android").permitAll()
//                .antMatchers(HttpMethod.POST, "/token/generate-token").permitAll()
//                .antMatchers(HttpMethod.POST, "/token/generate-token-mobile").permitAll()
//                .antMatchers(HttpMethod.POST, "/token/send-registration-code").permitAll()
//                .antMatchers(HttpMethod.POST, "/token/review-registration-code").permitAll()
//                .antMatchers(HttpMethod.POST, "/logs").permitAll()
//                .antMatchers(HttpMethod.GET).permitAll()
//                .anyRequest().authenticated();
//        http
//                .headers()
//                .frameOptions().sameOrigin()
//                .cacheControl();
//
//        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
//    }
//
//    @Override
//    public void configure(WebSecurity web) {
//        web.ignoring().antMatchers(HttpMethod.GET, "/*.html", "/favicon.png", "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.gif", "/**/*.woff", "/**/*.woff2", "/**/*.ttf", "/**/*.txt");
//    }

//    @Bean
//    public AuthenticationManagerBuilder authenticationManagerBean(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService) throws Exception {
//        return auth
//            .userDetailsService(userDetailsService)
//            .passwordEncoder(passwordEncoderBean()).and();
//        return super.authenticationManagerBean();
//    }

    @Bean
    public PasswordEncoder passwordEncoderBean() {
        return new BCryptPasswordEncoder();
    }
}
