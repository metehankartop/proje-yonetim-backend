package com.metehankartop.proje_yonetim.controller;

import com.metehankartop.proje_yonetim.model.Project;
import com.metehankartop.proje_yonetim.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    // SADECE MANAGER'LAR tüm projeleri görebilir
    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            System.out.println("=== GET ALL PROJECTS ===");
            System.out.println("Username: " + username + ", Role: " + role);

            // Eğer MANAGER ise tüm projeleri göster, değilse sadece kendi projelerini
            if (role.contains("MANAGER")) {
                List<Project> allProjects = service.getAll();
                System.out.println("Manager - Tüm projeler getiriliyor: " + allProjects.size());
                return ResponseEntity.ok(allProjects);
            } else {
                List<Project> userProjects = service.getProjectsByUsername(username);
                System.out.println("Employee - Sadece atanmış projeler getiriliyor: " + userProjects.size());
                return ResponseEntity.ok(userProjects);
            }
        } catch (Exception e) {
            System.err.println("Error getting projects: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Giriş yapan kullanıcının atandığı projeleri getir
    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            System.out.println("=== GET MY PROJECTS ===");
            System.out.println("Username: " + username);

            List<Project> userProjects = service.getProjectsByUsername(username);
            System.out.println("User projects found: " + userProjects.size());
            return ResponseEntity.ok(userProjects);
        } catch (Exception e) {
            System.err.println("Error getting user projects: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            System.out.println("=== GET PROJECT BY ID: " + id + " ===");

            Optional<Project> projectOpt = service.getById(id);
            if (projectOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Project project = projectOpt.get();

            // MANAGER ise tüm projeleri görebilir
            if (role.contains("MANAGER")) {
                return ResponseEntity.ok(project);
            }

            // EMPLOYEE ise sadece atandığı projeleri görebilir
            boolean isAssigned = project.getEmployees().stream()
                    .anyMatch(emp -> emp.getUsername().equals(username));

            if (isAssigned) {
                return ResponseEntity.ok(project);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

        } catch (Exception e) {
            System.err.println("Error getting project by id: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Proje oluştur (Sadece MANAGER)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Project project) {
        System.out.println("=== CREATE PROJECT DEBUG ===");
        System.out.println("Creating project: " + project.getName());
        System.out.println("Project description: " + project.getDescription());
        System.out.println("Project status: " + project.getStatus());

        try {
            if (project.getName() == null || project.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Proje adı boş olamaz"));
            }

            Project savedProject = service.save(project);
            System.out.println("Project saved successfully with ID: " + savedProject.getId());

            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            System.err.println("Project creation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Proje kaydedilirken hata oluştu: " + e.getMessage()));
        }
    }

    // Proje güncelle (Sadece MANAGER)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Project project) {
        System.out.println("=== UPDATE PROJECT DEBUG ===");
        System.out.println("Updating project with ID: " + id);

        try {
            Project updatedProject = service.update(id, project);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            System.err.println("Project update error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Proje güncellenirken hata oluştu: " + e.getMessage()));
        }
    }

    // Proje silme (Sadece MANAGER)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        System.out.println("=== DELETE PROJECT DEBUG ===");
        System.out.println("Deleting project with ID: " + id);

        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Project deletion error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Proje silinirken hata oluştu: " + e.getMessage()));
        }
    }

    // Çalışan atama (Sadece MANAGER)
    @PostMapping("/{projectId}/employees/{employeeId}")
    public ResponseEntity<?> assign(@PathVariable Long projectId, @PathVariable Long employeeId) {
        System.out.println("=== ASSIGN EMPLOYEE DEBUG ===");
        System.out.println("Assigning employee " + employeeId + " to project " + projectId);

        try {
            Project project = service.assignEmployee(projectId, employeeId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            System.err.println("Employee assignment error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Çalışan atanırken hata oluştu: " + e.getMessage()));
        }
    }

    // Çalışan çıkarma (Sadece MANAGER)
    @DeleteMapping("/{projectId}/employees/{employeeId}")
    public ResponseEntity<?> removeEmployee(@PathVariable Long projectId, @PathVariable Long employeeId) {
        System.out.println("=== REMOVE EMPLOYEE DEBUG ===");
        System.out.println("Removing employee " + employeeId + " from project " + projectId);

        try {
            Project project = service.removeEmployee(projectId, employeeId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            System.err.println("Employee removal error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Çalışan çıkarılırken hata oluştu: " + e.getMessage()));
        }
    }
}