package com.metehankartop.proje_yonetim.controller;

import com.metehankartop.proje_yonetim.dto.LoginRequest;
import com.metehankartop.proje_yonetim.model.Employee;
import com.metehankartop.proje_yonetim.repository.EmployeeRepository;
import com.metehankartop.proje_yonetim.config.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    //kullanıcı girişini kontrol eder
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        System.out.println("Login attempt for username: " + request.getUsername());

        Optional<Employee> optionalEmployee = employeeRepository.findByUsername(request.getUsername());

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            System.out.println("Employee found: " + employee.getUsername());

            if (passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
                String token = jwtUtil.generateToken(employee.getUsername(), employee.getRole());
                System.out.println("Login successful, token generated");
                return Map.of("token", token);
            } else {
                System.out.println("Password mismatch");
            }
        } else {
            System.out.println("Employee not found");
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Hatalı giriş");
    }
    //yeni kullanıcı kaydı yapar
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Employee employee) {
        System.out.println("=== REGISTER DEBUG ===");
        System.out.println("Registering employee: " + employee.getUsername());
        System.out.println("Employee email: " + employee.getEmail());
        System.out.println("Employee role: " + employee.getRole());

        try {

            if (employee.getUsername() == null || employee.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kullanıcı adı boş olamaz"));
            }

            if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email boş olamaz"));
            }

            if (employee.getPassword() == null || employee.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Şifre boş olamaz"));
            }

            if (employee.getName() == null || employee.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "İsim boş olamaz"));
            }

            if (employee.getRole() == null || employee.getRole().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rol boş olamaz"));
            }

            // Kullanıcı adı kontrolü
            if (employeeRepository.findByUsername(employee.getUsername()).isPresent()) {
                System.out.println("Username already exists: " + employee.getUsername());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bu kullanıcı adı zaten kullanımda"));
            }

            // Email kontrolü
            if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
                System.out.println("Email already exists: " + employee.getEmail());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bu email zaten kullanımda"));
            }

            // Şifreyi encode et
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            Employee savedEmployee = employeeRepository.save(employee);
            System.out.println("Employee saved successfully with ID: " + savedEmployee.getId());

            // Şifreyi response'da göndermemek için null yapıyoruz
            savedEmployee.setPassword(null);

            return ResponseEntity.ok(savedEmployee);
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kullanıcı kaydedilirken hata oluştu: " + e.getMessage()));
        }
    }
}