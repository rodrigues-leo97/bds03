package com.devsuperior.bds03.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.bds03.tests.TokenUtil;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DepartmentControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private String operatorUsername;
	private String operatorPassword;
	private String adminUsername;
	private String adminPassword;
	
	@BeforeEach
	void setUp() throws Exception {

		//ele já faz o login nos testes, mas se olhar no seed do banco ainda não tem os operadores de cada um, tem que implementar
		operatorUsername = "ana@gmail.com";
		operatorPassword = "123456";
		adminUsername = "bob@gmail.com";
		adminPassword = "123456";
	}
	
	@Test
	public void findAllShouldReturnAllResourcesSortedByNameWhenAdminLogged() throws Exception { //retornar todos os recursos ordenados por nome quando estiver logado com ADMIN
		
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword); //pegar um token de admin
		
		ResultActions result =
				mockMvc.perform(get("/departments")
					.header("Authorization", "Bearer " + accessToken) //passando no header o token
					.contentType(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$[0].name").value("Management"));
		result.andExpect(jsonPath("$[1].name").value("Sales"));
		result.andExpect(jsonPath("$[2].name").value("Training"));
	}
	
	@Test
	public void findAllShouldReturnAllResourcesSortedByNameWhenEmployeeLogged() throws Exception { //retornar todos os recursos ordenados por nome quando o operador estiver logado, operador também tem essa permissão
		
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, operatorUsername, operatorPassword);
		
		ResultActions result =
				mockMvc.perform(get("/departments")
					.header("Authorization", "Bearer " + accessToken)
					.contentType(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$[0].name").value("Management"));
		result.andExpect(jsonPath("$[1].name").value("Sales"));
		result.andExpect(jsonPath("$[2].name").value("Training"));
	}
	
	@Test
	public void findAllShouldReturn401WhenNoUserLogged() throws Exception { //retornar 401 quando o usuário não estiver logado, ou seja, sem nenhum usuário logado(UNAUTORIZED)
		
		ResultActions result =
				mockMvc.perform(get("/departments")
					.contentType(MediaType.APPLICATION_JSON));

		result.andExpect(status().isUnauthorized());
	}
}
