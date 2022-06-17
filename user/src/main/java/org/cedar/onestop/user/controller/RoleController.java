package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.JsonApiData;
import org.cedar.onestop.data.api.JsonApiMeta;
import org.cedar.onestop.data.api.JsonApiResponse;
import org.cedar.onestop.data.api.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.repository.OnestopRoleRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
public class RoleController {
  private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

  private final OnestopRoleRepository roleRepository;

  @Autowired
  public RoleController(OnestopRoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_ROLES_BY_USER_ID)
  @ApiOperation(value = "Get roles (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user roles"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/role", produces = "application/json")
  public JsonApiResponse getRoles(Pageable pageable,
                                  @RequestParam(required = false) String userId,
                                  HttpServletResponse response) {
    logger.info("Retrieving roles w/ page params: " + pageable);
    var page = userId != null && !userId.isBlank() ?
        roleRepository.findByUsersId(userId, pageable) :
        roleRepository.findAll(pageable);
    var total = page.getTotalElements();
    logger.info("Retrieved " + page.getSize() + " of " + total + " total roles");
    List<JsonApiData> dataList = page.get()
        .map(p -> new JsonApiData.Builder().setId(p.getId()).setType("role").setAttributes(p.toMap()).build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(dataList).build();
  }

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_ROLES_BY_USER_ID)
  @ApiOperation(value = "Get role by id (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user role"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/role/{id}", produces = "application/json")
  public JsonApiResponse getRoleById(@PathVariable(value = "id") String id, HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Retrieving user role w/ id: " + id);
    List<JsonApiData> result = roleRepository.findById(id)
        .map(r -> new JsonApiData.Builder().setId(r.getId()).setAttributes(r.toMap()).setType("role").build())
        .map(Arrays::asList)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found for id: " + id));
    logger.info("Retrieved role w/ id " + id);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.CREATE_ROLE})
  @ApiOperation(value = "Create a role")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully created role"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping(value = "/role", produces = "application/json")
  public JsonApiResponse createRole(@RequestBody OnestopRole role, HttpServletResponse response)
      throws RuntimeException {
    logger.info("Creating new role : " + role.getName());
    OnestopRole savedRole = roleRepository.save(role);
    logger.info("Created role : " + savedRole.getId() + ":" + savedRole.getName());
    List<JsonApiData> dataList = new ArrayList<>();
    JsonApiData dataItem = new JsonApiData.Builder()
        .setId(savedRole.getId())
        .setAttributes(savedRole.toMap())
        .setType("role").build();
    dataList.add(dataItem);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.CREATED.value(), response)
        .setData(dataList).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.DELETE_ROLE})
  @ApiOperation(value = "Delete role")
  @DeleteMapping(value = "/role/{id}", produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id, HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received role DELETE request");
    logger.info(id);
    OnestopRole role = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found for requested id: " + id));
    logger.info("Deleting role with id: " + role.getId());
    roleRepository.delete(role);
    logger.info("Delete complete for role id: " + id);
    Map<String, Boolean> result = new HashMap<>();
    result.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
  }

  @ApiOperation(value = "Get authenticated user roles")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user roles"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/self/role", produces = "application/json")
  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_OWN_PROFILE})
  public JsonApiResponse getAuthenticatedUserRoles(
      Principal authentication,
      Pageable pageable,
      HttpServletResponse response)
      throws RuntimeException {
    String userId = authentication.getName();
    logger.info("Retrieving roles for authenticated user with id: " + userId);
    var result = roleRepository.findByUsersId(userId, pageable).get()
        .map(u -> new JsonApiData.Builder().setId(u.getId()).setAttributes(u.toMap()).setType("role").build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

}
