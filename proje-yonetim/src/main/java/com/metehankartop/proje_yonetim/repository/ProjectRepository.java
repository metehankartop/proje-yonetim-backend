package com.metehankartop.proje_yonetim.repository;

import com.metehankartop.proje_yonetim.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Belirli bir kullanıcı adına sahip çalışanın atandığı projeleri getir
    @Query("SELECT p FROM Project p JOIN p.employees e WHERE e.username = :username")
    List<Project> findByEmployeesUsername(@Param("username") String username);
}