package com.nextround.nextroundapi.service;
import com.nextround.nextroundapi.dtos.AuthResponse;
import com.nextround.nextroundapi.dtos.LoginRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.EmailAlreadyExistsException;
import com.nextround.nextroundapi.exception.UsernameNotFoundException;
import com.nextround.nextroundapi.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String encryptPassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        String encryptedPassword = encryptPassword(registerRequest.password());
        User user = new User(registerRequest.email(), encryptedPassword, registerRequest.firstName(), registerRequest.lastName(), Role.USER);

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);

        return new AuthResponse(jwtToken, "Bearer", jwtService.getExpirationTime(), UserMapper.toDto(savedUser));
    }


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.email()));

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationTime(),
                UserMapper.toDto(user)
        );

    }
}
