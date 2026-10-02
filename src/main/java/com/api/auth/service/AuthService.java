package com.api.auth.service;

import com.api.auth.dto.CreateUserRequest;
import com.api.auth.dto.LoginRequest;
import com.api.auth.dto.LoginResponse;
import com.api.auth.dto.RegisterRequest;
import com.api.auth.dto.UserResponse;
import com.api.exception.ConflictException;
import com.api.exception.UnauthorizedException;
import com.api.security.JwtService;
import com.api.user.entity.UserAccount;
import com.api.user.enums.Role;
import com.api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse register(RegisterRequest request) {
        UserAccount saved = saveUser(request.name(), request.email(), request.password(), Role.CUSTOMER);
        return tokenFor(saved);
    }

    public UserResponse createUser(CreateUserRequest request) {
        UserAccount saved = saveUser(request.name(), request.email(), request.password(), request.role());
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    private UserAccount saveUser(String name, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe um usuário com o e-mail informado");
        }

        UserAccount user = new UserAccount();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
        if (!passwordMatches) {
            throw new UnauthorizedException("E-mail ou senha inválidos");
        }

        return tokenFor(user);
    }

    private LoginResponse tokenFor(UserAccount user) {
        String token = jwtService.generate(user);
        return new LoginResponse(token, "Bearer");
    }
}
