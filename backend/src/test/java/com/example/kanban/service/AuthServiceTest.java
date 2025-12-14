package com.example.kanban.service;

import com.example.kanban.dto.AuthenticationResponse;
import com.example.kanban.dto.LoginRequest;
import com.example.kanban.dto.RegisterRequest;
import com.example.kanban.entity.User;
import com.example.kanban.exception.EmailAlreadyExistsException;
import com.example.kanban.repository.UserRepository;
import com.example.kanban.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("테스트사용자")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .name("테스트사용자")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        AuthenticationResponse response = authService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("테스트사용자");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_Fail_EmailExists() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void authenticate_Success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        AuthenticationResponse response = authService.authenticate(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("테스트사용자");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void authenticate_Fail_UserNotFound() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(RuntimeException.class);
    }
}
