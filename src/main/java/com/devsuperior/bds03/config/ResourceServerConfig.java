package com.devsuperior.bds03.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Arrays;

@Configuration
@EnableResourceServer //processa para que a classe implemente a funcionalidade do ResourceServe
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	private Environment env; //a partir dele dá pra acessar diversas variáveis

	@Autowired
	private JwtTokenStore tokenStore;

	private static final String[] PUBLIC = {"/oauth/token", "/h2-console/**"}; //endpoints publicos

	private static final String[] OPERATOR_OR_ADMIN = {"/products/**", "/categories/**"}; //rotas que estarão liberadas para quem for operador e admin

	private static final String[] ADMIN = {"/users/**"}; //rotas liberadas somente para ADMIN

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception { //irá decodificar o token e ver se está tudo certo ou não, valida o token
		//config do TOKEN STRORE
		resources.tokenStore(tokenStore); //irá receber o bean tokenStore
	}

	@Override
	public void configure(HttpSecurity http) throws Exception { //configuração das rotas e definir as configurações de acesso

		//para testar os profiles de execução que estou rodando (H2)
		//vou converter isso para uma lista
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) { //se nos PROFILES ativos eu tenho um perfil de test
			http.headers().frameOptions().disable(); //interface do h2 requer que desabilite essas questões de proteger os frames para que ela pegue
		}

		http.authorizeRequests()
				.antMatchers(PUBLIC).permitAll() //antMatchers -> não exige login nessas rotas e permite tudo relacionado a elas
				.antMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll() //estou dizendo que é pra liberar para todo mundo SOMENTE para o método GET
				.antMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN") //hasAnyRole(possui algum dos papéis, no caso OPERATOR ou ADMIN), ou seja, as rotas desse vetor pode acessar quem estiver o OPERATOR ou ADMIN
				.antMatchers(ADMIN).hasRole("ADMIN") //só pode acessar essas rotas quem tiver a role ADMIN
				.anyRequest().authenticated(); //qualquer outra rota não específicada ela irá cobrar autenticação pq aqui está configurado para isso, ou seja, tem que estar logado indepedente do perfil de usuário

	}
}
