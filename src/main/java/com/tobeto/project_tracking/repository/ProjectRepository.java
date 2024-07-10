package com.tobeto.project_tracking.repository;

import com.tobeto.project_tracking.domain.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {}
