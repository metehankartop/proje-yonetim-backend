package com.metehankartop.proje_yonetim.service;

import com.metehankartop.proje_yonetim.model.Employee;
import com.metehankartop.proje_yonetim.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository repo;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> getAll() {
        return repo.findAll();
    }

    public Optional<Employee> getById(Long id) {
        return repo.findById(id);
    }

    public Employee save(Employee e) {
        System.out.println("=== EMPLOYEE SAVE DEBUG ===");
        System.out.println("Saving employee: " + e.getUsername());
        System.out.println("Employee email: " + e.getEmail());
        System.out.println("Employee role: " + e.getRole());

        try {
            e.setPassword(passwordEncoder.encode(e.getPassword()));
            Employee saved = repo.save(e);
            System.out.println("Employee saved successfully with ID: " + saved.getId());
            return saved;
        } catch (Exception ex) {
            System.err.println("Error saving employee: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    public Employee update(Long id, Employee e) {
        System.out.println("=== EMPLOYEE UPDATE DEBUG ===");
        System.out.println("Updating employee with ID: " + id);

        try {
            e.setId(id);
            e.setPassword(passwordEncoder.encode(e.getPassword()));
            Employee updated = repo.save(e);
            System.out.println("Employee updated successfully");
            return updated;
        } catch (Exception ex) {
            System.err.println("Error updating employee: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    public void delete(Long id) {
        System.out.println("=== EMPLOYEE DELETE DEBUG ===");
        System.out.println("Deleting employee with ID: " + id);

        try {
            repo.deleteById(id);
            System.out.println("Employee deleted successfully");
        } catch (Exception ex) {
            System.err.println("Error deleting employee: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}