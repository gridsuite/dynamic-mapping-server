/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.dto.workspace.MappingWorkspaceItem;
import org.gridsuite.mapping.server.dto.workspace.Workspace;
import org.gridsuite.mapping.server.repository.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceControllerTest {

    private static final String BASE_URL = "/workspaces";
    private static final String USER_ID = "testUser";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @AfterEach
    void cleanUp() {
        workspaceRepository.deleteAll();
    }

    // --- GET /workspaces/{userId} --- //

    @Test
    void testGetOrCreateWorkspace_createsNewWhenNotExist() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        Workspace workspace = objectMapper.readValue(result.getResponse().getContentAsString(), Workspace.class);
        assertThat(workspace.userId()).isEqualTo(USER_ID);
        assertThat(workspace.id()).isNotNull();
        assertThat(workspace.mappingWorkspaceItems()).isEmpty();
    }

    @Test
    void testGetOrCreateWorkspace_returnsExistingWorkspace() throws Exception {
        // First call creates it
        mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk());

        // Second call returns the same workspace
        MvcResult result = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();

        Workspace workspace = objectMapper.readValue(result.getResponse().getContentAsString(), Workspace.class);
        assertThat(workspace.userId()).isEqualTo(USER_ID);
        // Only one workspace entity should exist in the database
        assertThat(workspaceRepository.findAll()).hasSize(1);
    }

    // --- PUT /workspaces/{workspaceId} --- //

    @Test
    void testUpdateWorkspace_withMappingItems() throws Exception {
        // Create the workspace first
        MvcResult createResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace createdWorkspace = objectMapper.readValue(createResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceId = createdWorkspace.id();

        UUID mappingId = UUID.randomUUID();
        Workspace updatedWorkspace = new Workspace(
            workspaceId,
            USER_ID,
            List.of(new MappingWorkspaceItem(null, mappingId, true))
        );

        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedWorkspace)))
            .andExpect(status().isOk());

        // Verify the update persisted
        MvcResult getResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace fetchedWorkspace = objectMapper.readValue(getResult.getResponse().getContentAsString(), Workspace.class);
        assertThat(fetchedWorkspace.mappingWorkspaceItems()).hasSize(1);
        assertThat(fetchedWorkspace.mappingWorkspaceItems().get(0).mappingId()).isEqualTo(mappingId);
        assertThat(fetchedWorkspace.mappingWorkspaceItems().get(0).pinned()).isTrue();
    }

    @Test
    void testUpdateWorkspace_clearsMappingItems() throws Exception {
        // Create workspace and populate items
        MvcResult createResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace createdWorkspace = objectMapper.readValue(createResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceId = createdWorkspace.id();

        Workspace workspaceWithItem = new Workspace(workspaceId, USER_ID,
            List.of(new MappingWorkspaceItem(null, UUID.randomUUID(), false)));
        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceWithItem)))
            .andExpect(status().isOk());

        // Now clear items
        Workspace clearedWorkspace = new Workspace(workspaceId, USER_ID, List.of());
        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clearedWorkspace)))
            .andExpect(status().isOk());

        MvcResult getResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace fetchedWorkspace = objectMapper.readValue(getResult.getResponse().getContentAsString(), Workspace.class);
        assertThat(fetchedWorkspace.mappingWorkspaceItems()).isEmpty();
    }

    @Test
    void testUpdateWorkspace_updatesExistingMappingItem() throws Exception {
        // Create workspace
        MvcResult createResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace createdWrokspace = objectMapper.readValue(createResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceId = createdWrokspace.id();

        UUID mappingId = UUID.randomUUID();
        Workspace workspaceWithItem = new Workspace(workspaceId, USER_ID,
            List.of(new MappingWorkspaceItem(null, mappingId, false)));
        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceWithItem)))
            .andExpect(status().isOk());

        // Fetch to get the assigned item ID
        MvcResult getResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace fetchedWorkspace = objectMapper.readValue(getResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceItemId = fetchedWorkspace.mappingWorkspaceItems().get(0).id();

        // Update that item (set pinned = true)
        Workspace workspaceWithUpdatedItem = new Workspace(workspaceId, USER_ID,
            List.of(new MappingWorkspaceItem(workspaceItemId, mappingId, true)));
        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceWithUpdatedItem)))
            .andExpect(status().isOk());

        MvcResult finalResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace finalWorkspace = objectMapper.readValue(finalResult.getResponse().getContentAsString(), Workspace.class);
        assertThat(finalWorkspace.mappingWorkspaceItems()).hasSize(1);
        assertThat(finalWorkspace.mappingWorkspaceItems().get(0).id()).isEqualTo(workspaceItemId);
        assertThat(finalWorkspace.mappingWorkspaceItems().get(0).pinned()).isTrue();
    }

    // --- DELETE /workspaces/{workspaceId} ---

    @Test
    void testDeleteWorkspace() throws Exception {
        // Create workspace
        MvcResult createResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace createdWorkspace = objectMapper.readValue(createResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceId = createdWorkspace.id();

        mockMvc.perform(delete(BASE_URL + "/{workspaceId}", workspaceId))
            .andExpect(status().isOk());

        assertThat(workspaceRepository.findById(workspaceId)).isEmpty();
    }

    @Test
    void testDeleteWorkspace_alsoDeletesMappingItems() throws Exception {
        // Create workspace with items
        MvcResult createResult = mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID))
            .andExpect(status().isOk())
            .andReturn();
        Workspace createdWorkspace = objectMapper.readValue(createResult.getResponse().getContentAsString(), Workspace.class);
        UUID workspaceId = createdWorkspace.id();

        Workspace workspaceWithItem = new Workspace(workspaceId, USER_ID,
            List.of(new MappingWorkspaceItem(null, UUID.randomUUID(), false)));
        mockMvc.perform(put(BASE_URL + "/{workspaceId}", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workspaceWithItem)))
            .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/{workspaceId}", workspaceId))
            .andExpect(status().isOk());

        assertThat(workspaceRepository.findById(workspaceId)).isEmpty();
    }
}
