package com.unicolour.joyspace.config;

import javax.sql.DataSource;

import com.unicolour.joyspace.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	DataSource dataSource;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
    ManagerService managerService;
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	//http.authorizeRequests().anyRequest().permitAll();
		String[] permitAllPatterns = {
				"/thirdparty/**",
				"/img/**",
				"/js/**",
				"/css/**",
				"/fonts/**",
				"/api/**",
				"/app/**",
				"/graphql",
				"/wxpay/notify",
				"/printStation/**",
		};
        http
        	.csrf().disable()
            .authorizeRequests()
                .antMatchers(permitAllPatterns).permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/manager/list")
                .permitAll()
                .and()
            .logout()
                .permitAll()
				.and()
			.headers()
				.frameOptions()
				.sameOrigin();
    }
    

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	auth.userDetailsService(managerService).passwordEncoder(passwordEncoder);
    }
}