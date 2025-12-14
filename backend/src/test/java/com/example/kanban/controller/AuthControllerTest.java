package com.example.kanban.controller;

import com.example.kanban.dto.AuthenticationResponse;
import com.example.kanban.dto.LoginRequest;
import com.example.kanban.dto.RegisterRequest;
import com.example.kanban.exception.EmailAlreadyExistsException;
import com.example.kanban.security.JwtAuthenticationFilter;
import com.example.kanban.security.JwtTokenProvider;
import com.example.kanban.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @Test
        @DisplayName("회원가입 API 성공")
        @WithMockUser
        void register_Success() throws Exception {
                // given
                RegisterRequest request = RegisterRequest.builder()
                                .name("테스트사용자")
                                .email("test@example.com")
                                .password("password123")
                                .build();

                AuthenticationResponse response = AuthenticationResponse.builder()
                                .token("jwt-token")
                                .username("테스트사용자")
                                .build();

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                // when & then
                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("jwt-token"))
                                .andExpect(jsonPath("$.username").value("테스트사용자"));
        }

        @Test
        @DisplayName("회원가입 API 실패 - 이메일 중복")
        @WithMockUser
        void register_Fail_EmailExists() throws Exception {
                // given
                RegisterRequest request = RegisterRequest.builder()
                                .name("테스트사용자")
                                .email("test@example.com")
                                .password("password123")
                                .build();

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new EmailAlreadyExistsException("이미 존재하는 이메일입니다."));

                // when & then
                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("로그인 API 성공")
        @WithMockUser
        void login_Success() throws Exception {
                // given
                LoginRequest request = LoginRequest.builder()
                                .email("test@example.com")
                                .password("password123")
                                .build();

                AuthenticationResponse response = AuthenticationResponse.builder()
                                .token("jwt-token")
                                .username("테스트사용자")
                                .build();

                when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

                // when & then
                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("jwt-token"))
                                .andExpect(jsonPath("$.username").value("테스트사용자"));
        }

        @Test
        @DisplayName("로그인 API 실패 - 인증 실패")
        @WithMockUser
        void login_Fail_BadCredentials() throws Exception {
                // given
                LoginRequest request = LoginRequest.builder()
                                .email("test@example.com")
                                .password("wrongpassword")
                                .build();

                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new BadCredentialsException("잘못된 인증 정보입니다."));

                // when & then
                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is4xxClientError());
        }
}
