package com.metehankartop.proje_yonetim.service;

import com.metehankartop.proje_yonetim.model.Employee;
import com.metehankartop.proje_yonetim.model.Project;
import com.metehankartop.proje_yonetim.repository.EmployeeRepository;
import com.metehankartop.proje_yonetim.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectService(ProjectRepository projectRepository, EmployeeRepository employeeRepository) {
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> getById(Long id) {
        return projectRepository.findById(id);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    public Project update(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı: " + id));

        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setStatus(projectDetails.getStatus());

        return projectRepository.save(project);
    }

    public Project assignEmployee(Long projectId, Long employeeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı: " + projectId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Çalışan bulunamadı: " + employeeId));

        project.getEmployees().add(employee);
        return projectRepository.save(project);
    }

    public Project removeEmployee(Long projectId, Long employeeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı: " + projectId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Çalışan bulunamadı: " + employeeId));

        project.getEmployees().remove(employee);
        return projectRepository.save(project);
    }

    // Belirli bir kullanıcı adına sahip çalışanın atandığı projeleri getir
    public List<Project> getProjectsByUsername(String username) {
        return projectRepository.findByEmployeesUsername(username);
    }
}