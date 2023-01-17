package com.devsuperior.bds03.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //aqui o spring security saberá na hora de se autenticar em como buscar por email e como analisar a senha criptografada
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder); //configuramos o userDetail e password através do método userDetailService e passwordEncoder
        super.configure(auth);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/actuator/**"); //liberando todo mundo mas agora passando pela lib que o spring oAuth usa para passar nas requisições(actuator)
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception { //para que seja um componente disponível no sistema
        return super.authenticationManager();
    }
}
