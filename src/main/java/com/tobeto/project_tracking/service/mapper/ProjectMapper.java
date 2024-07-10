package com.tobeto.project_tracking.service.mapper;

import com.tobeto.project_tracking.domain.Project;
import com.tobeto.project_tracking.service.dto.ProjectDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Project} and its DTO {@link ProjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {}
