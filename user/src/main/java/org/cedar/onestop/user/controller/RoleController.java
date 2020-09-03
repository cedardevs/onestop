package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.common.JsonApiData;
import org.cedar.onestop.user.common.JsonApiMeta;
import org.cedar.onestop.user.common.JsonApiResponse;
import org.cedar.onestop.user.common.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.SecurityConfig;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.domain.SavedSearch;
import org.cedar.onestop.user.repository.OnestopRoleRepository;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class RoleController {
  Logger logger = LoggerFactory.getLogger(RoleController.class);

  @Autowired
  public OnestopUserRepository onestopUserRepository;

  @Autowired
  public OnestopRoleRepository onestopRoleRepository;

  @Secured("ROLE_" + SecurityConfig.ADMIN_PRIVILEGE)
  @ApiOperation(value = "Get user roles (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully retrieved user roles"),
    @ApiResponse(code = 401, message = "Access denied"),
    @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/role/{id}", produces = "application/json")
  public JsonApiResponse getUserRoles(@PathVariable(value = "id") String id, final @AuthenticationPrincipal Authentication authentication, HttpServletResponse response)
    throws ResourceNotFoundException {
    logger.info("Retrieving user roles for user id: " + id);
    OnestopUser user = onestopUserRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id :: " + id));
    logger.info("Retrieved " + user.getRoles().size() + " roles for user id: " + id);
    List<JsonApiData> dataList = new ArrayList<>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("roles", user.getRoles());

    JsonApiData dataItem = new JsonApiData.Builder()
      .setId(user.getId())
      .setAttributes(data)
      .setType("roles").build();
    dataList.add(dataItem);
    return new JsonApiSuccessResponse.Builder()
      .setStatus(HttpStatus.OK, response)
      .setData(dataList).build();
  }

  @Secured({"ROLE_" + SecurityConfig.ADMIN_PRIVILEGE})
  @ApiOperation(value = "Create a role", response = Iterable.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully created role"),
    @ApiResponse(code = 401, message = "Access denied"),
    @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping(value = "/role", produces = "application/json")
  public  JsonApiResponse createRole(@RequestBody OnestopRole role, HttpServletResponse response)
    throws RuntimeException {
    logger.info("Creating new role : " + role.getName());
    OnestopRole savedRole = onestopRoleRepository.save(role);
    logger.info("Created role : "+ savedRole.getId()+ ":" + savedRole.getName());
    List<JsonApiData> dataList = new ArrayList<>();
    JsonApiData dataItem = new JsonApiData.Builder()
      .setId(savedRole.getId())
      .setAttributes(savedRole.toMap())
      .setType("role").build();
    dataList.add(dataItem);
    return new JsonApiSuccessResponse.Builder()
      .setStatus(HttpStatus.CREATED, response)
      .setData(dataList).build();
  }

  @Secured({"ROLE_" + SecurityConfig.ADMIN_PRIVILEGE})
  @ApiOperation(value = "Delete role")
  @DeleteMapping(value = "/role/{id}", produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id, HttpServletResponse response)
    throws ResourceNotFoundException {
    logger.info("Received role DELETE request");
    logger.info(id);
    OnestopRole role = onestopRoleRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Role not found for requested id :: " + id));
    logger.info("Deleting role with id: " + role.getId());
    onestopRoleRepository.delete(role);
    logger.info("Delete complete for role id: " + id);
    Map<String, Boolean> result = new HashMap<>();
    result.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
      .setStatus(HttpStatus.OK, response)
      .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
  }

}
