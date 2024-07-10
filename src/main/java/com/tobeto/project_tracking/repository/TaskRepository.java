package com.tobeto.project_tracking.repository;

import com.tobeto.project_tracking.domain.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    default Optional<Task> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Task> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Task> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select task from Task task left join fetch task.project", countQuery = "select count(task) from Task task")
    Page<Task> findAllWithToOneRelationships(Pageable pageable);

    @Query("select task from Task task left join fetch task.project")
    List<Task> findAllWithToOneRelationships();

    @Query("select task from Task task left join fetch task.project where task.id =:id")
    Optional<Task> findOneWithToOneRelationships(@Param("id") Long id);
}
