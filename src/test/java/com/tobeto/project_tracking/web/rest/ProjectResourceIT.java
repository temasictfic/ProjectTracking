package com.tobeto.project_tracking.web.rest;

import static com.tobeto.project_tracking.domain.ProjectAsserts.*;
import static com.tobeto.project_tracking.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobeto.project_tracking.IntegrationTest;
import com.tobeto.project_tracking.domain.Project;
import com.tobeto.project_tracking.repository.ProjectRepository;
import com.tobeto.project_tracking.service.dto.ProjectDTO;
import com.tobeto.project_tracking.service.mapper.ProjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProjectResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProjectResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/projects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProjectMockMvc;

    private Project project;

    private Project insertedProject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createEntity(EntityManager em) {
        Project project = new Project().name(DEFAULT_NAME).startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE);
        return project;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createUpdatedEntity(EntityManager em) {
        Project project = new Project().name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);
        return project;
    }

    @BeforeEach
    public void initTest() {
        project = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedProject != null) {
            projectRepository.delete(insertedProject);
            insertedProject = null;
        }
    }

    @Test
    @Transactional
    void createProject() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);
        var returnedProjectDTO = om.readValue(
            restProjectMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProjectDTO.class
        );

        // Validate the Project in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProject = projectMapper.toEntity(returnedProjectDTO);
        assertProjectUpdatableFieldsEquals(returnedProject, getPersistedProject(returnedProject));

        insertedProject = returnedProject;
    }

    @Test
    @Transactional
    void createProjectWithExistingId() throws Exception {
        // Create the Project with an existing ID
        project.setId(1L);
        ProjectDTO projectDTO = projectMapper.toDto(project);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setName(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setStartDate(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setEndDate(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProjects() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())));
    }

    @Test
    @Transactional
    void getProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get the project
        restProjectMockMvc
            .perform(get(ENTITY_API_URL_ID, project.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(project.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProject() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProject are not directly saved in db
        em.detach(updatedProject);
        updatedProject.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);
        ProjectDTO projectDTO = projectMapper.toDto(updatedProject);

        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProjectToMatchAllProperties(updatedProject);
    }

    @Test
    @Transactional
    void putNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        partialUpdatedProject.startDate(UPDATED_START_DATE);

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProject, project), getPersistedProject(project));
    }

    @Test
    @Transactional
    void fullUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        partialUpdatedProject.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(partialUpdatedProject, getPersistedProject(partialUpdatedProject));
    }

    @Test
    @Transactional
    void patchNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, projectDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the project
        restProjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, project.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return projectRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Project getPersistedProject(Project project) {
        return projectRepository.findById(project.getId()).orElseThrow();
    }

    protected void assertPersistedProjectToMatchAllProperties(Project expectedProject) {
        assertProjectAllPropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }

    protected void assertPersistedProjectToMatchUpdatableProperties(Project expectedProject) {
        assertProjectAllUpdatablePropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }
}
