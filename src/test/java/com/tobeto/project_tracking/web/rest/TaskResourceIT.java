package com.tobeto.project_tracking.web.rest;

import static com.tobeto.project_tracking.domain.TaskAsserts.*;
import static com.tobeto.project_tracking.web.rest.TestUtil.createUpdateProxyForBean;
import static com.tobeto.project_tracking.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tobeto.project_tracking.IntegrationTest;
import com.tobeto.project_tracking.domain.Task;
import com.tobeto.project_tracking.domain.enumeration.TaskStatus;
import com.tobeto.project_tracking.repository.TaskRepository;
import com.tobeto.project_tracking.service.TaskService;
import com.tobeto.project_tracking.service.dto.TaskDTO;
import com.tobeto.project_tracking.service.mapper.TaskMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaskResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TaskResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATION_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATION_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final TaskStatus DEFAULT_STATUS = TaskStatus.NEW;
    private static final TaskStatus UPDATED_STATUS = TaskStatus.IN_PROGRESS;

    private static final String ENTITY_API_URL = "/api/tasks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

    @Mock
    private TaskRepository taskRepositoryMock;

    @Autowired
    private TaskMapper taskMapper;

    @Mock
    private TaskService taskServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskMockMvc;

    private Task task;

    private Task insertedTask;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createEntity(EntityManager em) {
        Task task = new Task()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .creationDate(DEFAULT_CREATION_DATE)
            .status(DEFAULT_STATUS);
        return task;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createUpdatedEntity(EntityManager em) {
        Task task = new Task()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .creationDate(UPDATED_CREATION_DATE)
            .status(UPDATED_STATUS);
        return task;
    }

    @BeforeEach
    public void initTest() {
        task = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedTask != null) {
            taskRepository.delete(insertedTask);
            insertedTask = null;
        }
    }

    @Test
    @Transactional
    void createTask() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);
        var returnedTaskDTO = om.readValue(
            restTaskMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TaskDTO.class
        );

        // Validate the Task in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTask = taskMapper.toEntity(returnedTaskDTO);
        assertTaskUpdatableFieldsEquals(returnedTask, getPersistedTask(returnedTask));

        insertedTask = returnedTask;
    }

    @Test
    @Transactional
    void createTaskWithExistingId() throws Exception {
        // Create the Task with an existing ID
        task.setId(1L);
        TaskDTO taskDTO = taskMapper.toDto(task);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        task.setTitle(null);

        // Create the Task, which fails.
        TaskDTO taskDTO = taskMapper.toDto(task);

        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreationDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        task.setCreationDate(null);

        // Create the Task, which fails.
        TaskDTO taskDTO = taskMapper.toDto(task);

        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        task.setStatus(null);

        // Create the Task, which fails.
        TaskDTO taskDTO = taskMapper.toDto(task);

        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTasks() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        // Get all the taskList
        restTaskMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].creationDate").value(hasItem(sameInstant(DEFAULT_CREATION_DATE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getAllTasksWithEagerRelationshipsIsEnabled() throws Exception {
        when(taskServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTaskMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(taskServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void getAllTasksWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(taskServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTaskMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(taskRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTask() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        // Get the task
        restTaskMockMvc
            .perform(get(ENTITY_API_URL_ID, task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(task.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.creationDate").value(sameInstant(DEFAULT_CREATION_DATE)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingTask() throws Exception {
        // Get the task
        restTaskMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTask() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the task
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTask are not directly saved in db
        em.detach(updatedTask);
        updatedTask.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).creationDate(UPDATED_CREATION_DATE).status(UPDATED_STATUS);
        TaskDTO taskDTO = taskMapper.toDto(updatedTask);

        restTaskMockMvc
            .perform(put(ENTITY_API_URL_ID, taskDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isOk());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTaskToMatchAllProperties(updatedTask);
    }

    @Test
    @Transactional
    void putNonExistingTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(put(ENTITY_API_URL_ID, taskDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTaskWithPatch() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the task using partial update
        Task partialUpdatedTask = new Task();
        partialUpdatedTask.setId(task.getId());

        partialUpdatedTask.title(UPDATED_TITLE).status(UPDATED_STATUS);

        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTask.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTask, task), getPersistedTask(task));
    }

    @Test
    @Transactional
    void fullUpdateTaskWithPatch() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the task using partial update
        Task partialUpdatedTask = new Task();
        partialUpdatedTask.setId(task.getId());

        partialUpdatedTask.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).creationDate(UPDATED_CREATION_DATE).status(UPDATED_STATUS);

        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTask.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskUpdatableFieldsEquals(partialUpdatedTask, getPersistedTask(partialUpdatedTask));
    }

    @Test
    @Transactional
    void patchNonExistingTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, taskDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(taskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTask() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        task.setId(longCount.incrementAndGet());

        // Create the Task
        TaskDTO taskDTO = taskMapper.toDto(task);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(taskDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Task in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTask() throws Exception {
        // Initialize the database
        insertedTask = taskRepository.saveAndFlush(task);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the task
        restTaskMockMvc
            .perform(delete(ENTITY_API_URL_ID, task.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return taskRepository.count();
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

    protected Task getPersistedTask(Task task) {
        return taskRepository.findById(task.getId()).orElseThrow();
    }

    protected void assertPersistedTaskToMatchAllProperties(Task expectedTask) {
        assertTaskAllPropertiesEquals(expectedTask, getPersistedTask(expectedTask));
    }

    protected void assertPersistedTaskToMatchUpdatableProperties(Task expectedTask) {
        assertTaskAllUpdatablePropertiesEquals(expectedTask, getPersistedTask(expectedTask));
    }
}
