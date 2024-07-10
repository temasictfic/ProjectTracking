package com.tobeto.project_tracking.service.impl;

import com.tobeto.project_tracking.domain.Project;
import com.tobeto.project_tracking.repository.ProjectRepository;
import com.tobeto.project_tracking.service.ProjectService;
import com.tobeto.project_tracking.service.dto.ProjectDTO;
import com.tobeto.project_tracking.service.mapper.ProjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.tobeto.project_tracking.domain.Project}.
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public ProjectDTO save(ProjectDTO projectDTO) {
        log.debug("Request to save Project : {}", projectDTO);
        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    @Override
    public ProjectDTO update(ProjectDTO projectDTO) {
        log.debug("Request to update Project : {}", projectDTO);
        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    @Override
    public Optional<ProjectDTO> partialUpdate(ProjectDTO projectDTO) {
        log.debug("Request to partially update Project : {}", projectDTO);

        return projectRepository
            .findById(projectDTO.getId())
            .map(existingProject -> {
                projectMapper.partialUpdate(existingProject, projectDTO);

                return existingProject;
            })
            .map(projectRepository::save)
            .map(projectMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        log.debug("Request to get all Projects");
        return projectRepository.findAll().stream().map(projectMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(Long id) {
        log.debug("Request to get Project : {}", id);
        return projectRepository.findById(id).map(projectMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Project : {}", id);
        projectRepository.deleteById(id);
    }
}
