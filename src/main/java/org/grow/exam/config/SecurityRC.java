package org.grow.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.annotation.Resource;

/**
 * Author : xieweig
 * Date : 18-7-19
 * <p>
 * Description:
 */
@Configuration
public class SecurityRC extends WebSecurityConfigurerAdapter {

    @Resource
    private AccessDeny accessDeny;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**
         **
         * xieweig notes: 内存用户名密码表，新建两个简单的用户和角色，注意加密，也可以在此多建立几个
         */
        auth.inMemoryAuthentication()
                .withUser("xieweiguang").password(passwordEncoder().encode("123456")).roles("TRAINER")
                .and()
                .withUser("xwg").password(passwordEncoder().encode("123")).roles("ADMIN")
                .and()
                .withUser("super").password(passwordEncoder().encode("superman")).roles("ADMIN", "TRAINER","GUEST")
                .and()
                .withUser("guest").password(passwordEncoder().encode("guest")).roles("STUDENT");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /**
         **
         * xieweig notes: 废弃csrf防御，往往造成访问不通
         */
        http.csrf().disable();
        /**
         **
         * xieweig notes: 核心：formLogin提供了后端一个api post 用户名密码的 login请求。确定登录页面被所有人访问
         * 默认成功跳转页面，通常为总页面，失败跳转页面通常还是为登录页面。但是会有model值出现提示用户名密码错误
         */
        http.formLogin().loginPage("/login").permitAll();
        /**
         **
         * xieweig notes: 用的session所以有登出，页提供一个logout的api，登出后跳转到login界面
         */
        http.logout().permitAll();

        http.exceptionHandling().accessDeniedHandler(accessDeny);


        http.authorizeRequests()
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "**/favicon.ico").permitAll()
                .antMatchers("/", "/index","/uploadQuestions","/classInfo","/import","/confirm","/swagger-ui.html#/**").permitAll()
                .antMatchers("/admin/**").hasAnyRole("ADMIN")
                .antMatchers("/trainer/**").hasAnyRole("TRAINER")
                .antMatchers("/student/**").hasAnyRole("STUDENT")
                .anyRequest().authenticated();


    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }


}
