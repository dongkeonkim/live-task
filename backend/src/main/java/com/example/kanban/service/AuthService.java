package com.example.kanban.service;

import com.example.kanban.dto.AuthenticationResponse;
import com.example.kanban.dto.LoginRequest;
import com.example.kanban.dto.RegisterRequest;
import com.example.kanban.entity.User;
import com.example.kanban.exception.EmailAlreadyExistsException;
import com.example.kanban.repository.UserRepository;
import com.example.kanban.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        var jwtToken = jwtTokenProvider.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getName())
                .build();
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtTokenProvider.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getName())
                .build();
    }
}
