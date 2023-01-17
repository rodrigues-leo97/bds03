package com.devsuperior.bds03.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.devsuperior.bds03.dto.EmployeeDTO;
import com.devsuperior.bds03.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private String operatorUsername;
	private String operatorPassword;
	private String adminUsername;
	private String adminPassword;
	
	@BeforeEach
	void setUp() throws Exception {
		
		operatorUsername = "ana@gmail.com";
		operatorPassword = "123456";
		adminUsername = "bob@gmail.com";
		adminPassword = "123456";
	}
	
	@Test
	public void insertShouldReturn403WhenOperatorLogged() throws Exception { //retornar 403 quando operador estiver logado, ou seja, sem permissão... dará esse erro quando um operador tentar inserir um novo funcionário

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, operatorUsername, operatorPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isForbidden());
	}	

	@Test
	public void insertShouldReturn401WhenNoUserLogged() throws Exception { //retornar 401 quando não estiver nenhum usuário logado

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnauthorized());
	}	
	
	@Test
	public void insertShouldInsertResourceWhenAdminLoggedAndCorrectData() throws Exception { //inserir o recurso quando admin estiver logado e os dados estiverem corretos

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto); //transformo pra Json
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated()); //201
		result.andExpect(jsonPath("$.id").exists()); //verificar se id existe
		result.andExpect(jsonPath("$.name").value("Joaquim")); //verificando o nome
		result.andExpect(jsonPath("$.email").value("joaquim@gmail.com")); //verificando o email
		result.andExpect(jsonPath("$.departmentId").value(1L)); //testando id do departamento
	}	

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndBlankName() throws Exception { //logado como admin e retorna o 422 pq inseriu um nome em branco

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "   ", "joaquim@gmail.com", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity()); //422 - pois inseriu um nome em branco
		result.andExpect(jsonPath("$.errors[0].fieldName").value("name")); //vou informar que tem que dar um erro no campo name
		result.andExpect(jsonPath("$.errors[0].message").value("Campo requerido")); // e a mensagem de erro é Campo requerido
	}

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndInvalidEmail() throws Exception { //dar 422 quando logado como admin e email inválido, igual ao de cima, porém, agora para o email

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("email"));
		result.andExpect(jsonPath("$.errors[0].message").value("Email inválido"));
	}

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndNullDepartment() throws Exception { //retorna 422 quando admin logado e departamento nullo, iguais aos de cima, só que agora para o departamento

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		EmployeeDTO dto = new EmployeeDTO(null, "Joaquim", "joaquim@gmail.com", null);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
				mockMvc.perform(post("/employees")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("departmentId"));
		result.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}
}
