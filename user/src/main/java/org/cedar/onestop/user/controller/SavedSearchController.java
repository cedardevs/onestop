package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.JsonApiData;
import org.cedar.onestop.data.api.JsonApiMeta;
import org.cedar.onestop.data.api.JsonApiResponse;
import org.cedar.onestop.data.api.JsonApiSuccessResponse;
import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.domain.SavedSearch;
import org.cedar.onestop.user.repository.SavedSearchRepository;
import org.cedar.onestop.user.service.OnestopUserService;
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
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/v1")
public class SavedSearchController {
  private static final Logger logger = LoggerFactory.getLogger(SavedSearchController.class);
  static String type = "search";

  private final SavedSearchRepository searchRepository;
  private final OnestopUserService userService;

  @Autowired
  public SavedSearchController(OnestopUserService userService, SavedSearchRepository searchRepository) {
    this.userService = userService;
    this.searchRepository = searchRepository;
  }

  //--- Admin REST Endpoints --

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.LIST_ALL_SAVED_SEARCHES)
  @ApiOperation(value = "View all available save searches (ADMIN)")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/saved-search", produces = "application/json")
  public JsonApiResponse getAll(Pageable pageable,
                                @RequestParam(required = false) String userId,
                                HttpServletResponse response) {
    logger.info("Retrieving all saved searches... ");
    var searchResults = userId != null && !userId.isBlank() ?
        searchRepository.findByUserId(userId, pageable) :
        searchRepository.findAll(pageable);
    logger.info("Retrieved " + searchResults.getNumber() + " out of " + searchResults.getTotalElements() + "total searches");
    logger.debug(searchResults.toString());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(generateListJsonApiData(searchResults.stream())).build();
  }

  @Secured(AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_SAVED_SEARCH_BY_ID)
  @ApiOperation(value = "Search with an ID (ADMIN)")
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.GET, produces = "application/json")
  public JsonApiResponse getById(@PathVariable(value = "id") String id,
                                 HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Retrieving saved search for id: " + id);
    SavedSearch savedSearch = searchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Saved search not found for requested id: " + id));
    List<SavedSearch> result = new ArrayList<>();
    result.add(savedSearch);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(generateListJsonApiData(result)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.UPDATE_SAVED_SEARCH})
  @ApiOperation(value = "Update user saved search")
  @PutMapping(value = "/saved-search/{id}", produces = "application/json")
  public JsonApiResponse update(@PathVariable(value = "id") String id,
                                @Valid @RequestBody SavedSearch savedSearchDetails,
                                HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search PUT request");
    SavedSearch savedSearch = searchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Saved search not found for this id: " + id));
    logger.info("Updating saved-search with id: " + id);
    savedSearch.setName(savedSearchDetails.getName());
    savedSearch.setValue(savedSearchDetails.getValue());
    savedSearch.setCreatedOn(savedSearchDetails.getCreatedOn());
    savedSearch.setLastUpdatedOn(savedSearchDetails.getLastUpdatedOn());
    final SavedSearch updatedSavedSearch = searchRepository.save(savedSearch);
    logger.info("Update complete for search with id: " + id);
    List<SavedSearch> result = new ArrayList<>();
    result.add(updatedSavedSearch);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(generateListJsonApiData(result)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.DELETE_SAVED_SEARCH})
  @ApiOperation(value = "Delete saved search")
  @DeleteMapping(value = "/saved-search/{id}", produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id,
                                Principal authentication,
                                HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search DELETE request");
    SavedSearch savedSearch = searchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Saved search not found for requested id: " + id));
    logger.info("Deleting saved-search with id: " + id);
    searchRepository.delete(savedSearch);
    logger.info("Delete complete for search with id: " + id);
    Map<String, Boolean> result = new HashMap<>();
    result.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
  }

  //--- Public Self-Management Endpoints

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_SAVED_SEARCH})
  @ApiOperation(value = "Get authenticated user searches")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved user searches"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping("/self/saved-search")
  public JsonApiResponse getAuthenticatedUserSearches(
      Principal authentication,
      Pageable pageable,
      HttpServletResponse response)
      throws RuntimeException {
    String userId = authentication.getName();
    logger.info("Retrieving searches for authenticated user with id: " + userId);
    var result = searchRepository.findByUserId(userId, pageable).get()
        .map(u -> new JsonApiData.Builder().setId(u.getId()).setAttributes(u.toMap()).setType("search").build())
        .collect(Collectors.toList());
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(result).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.CREATE_SAVED_SEARCH})
  @ApiOperation(value = "Create saved search w/ authenticated user")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successfully saved user search"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PostMapping("/self/saved-search")
  public JsonApiResponse createAuthenticatedUserSearch(
      @RequestBody SavedSearch savedSearch,
      Principal authentication,
      HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search POST request");
    var result = saveSearch(authentication.getName(), savedSearch);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.CREATED.value(), response)
        .setData(Collections.singletonList(result)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.READ_SAVED_SEARCH})
  @ApiOperation(value = "Retrieve saved search w/ authenticated user and id")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved saved search"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping("/self/saved-search/{id}")
  public JsonApiResponse getAuthenticatedUserSearch(
      @PathVariable String id,
      Principal authentication,
      HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search GET request");
    var userId = authentication.getName();
    var search = searchRepository.findByIdAndUserId(id, userId);
    var result = search
        .map(item -> new JsonApiData.Builder().setId(item.getId()).setType("search").setAttributes(item.toMap()).build())
        .orElseThrow(() -> new ResourceNotFoundException("Saved search not found for user " + userId + " with id: " + id));
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(Collections.singletonList(result)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.UPDATE_SAVED_SEARCH})
  @ApiOperation(value = "Update saved search w/ authenticated user and id")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully updated saved search"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @PutMapping("/self/saved-search/{id}")
  public JsonApiResponse updateAuthenticatedUserSearch(
      @RequestBody SavedSearch savedSearch,
      @PathVariable String id,
      Principal authentication,
      HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search PUT request");
    savedSearch.setId(id); // <-- force ID to URL param
    var result = saveSearch(authentication.getName(), savedSearch);
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setData(Collections.singletonList(result)).build();
  }

  @Secured({AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.DELETE_SAVED_SEARCH})
  @ApiOperation(value = "Delete saved search w/ authenticated user and id")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully deleted saved search"),
      @ApiResponse(code = 401, message = "Authentication required"),
      @ApiResponse(code = 403, message = "Authenticated user is not authorized to perform this action"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @DeleteMapping("/self/saved-search/{id}")
  public JsonApiResponse deleteAuthenticatedUserSearch(
      @PathVariable String id,
      Principal authentication,
      HttpServletResponse response)
      throws ResourceNotFoundException {
    logger.info("Received saved-search DELETE request");
    var userId = authentication.getName();
    var search = searchRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Saved search not found for user " + userId + " with id: " + id));
    searchRepository.delete(search);
    var metadata = new JsonApiMeta.Builder().setNonStandardMetadata(Map.of("deleted", true)).build();
    return new JsonApiSuccessResponse.Builder()
        .setStatus(HttpStatus.OK.value(), response)
        .setMeta(metadata).build();
  }

  private JsonApiData saveSearch(String userId, SavedSearch searchData) throws ResourceNotFoundException {
    var user = userService.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found for requested id: " + userId));
    logger.info("Creating search for user with ID: " + userId + " with details " + searchData);
    searchData.setUser(user);
    var item = searchRepository.save(searchData);
    logger.info("Saved search with ID: " + userId);
    return new JsonApiData.Builder()
        .setId(item.getId())
        .setType("search")
        .setAttributes(item.toMap()).build();
  }

  /**
   * Generate a List of JsonApiData to be used in a JsonApiResponse.
   *
   * @param searchResults List<SavedSearch> search results
   * @return List<JsonApiData> representing the search results
   */
  private List<JsonApiData> generateListJsonApiData(List<SavedSearch> searchResults) {
    return generateListJsonApiData(searchResults.stream());
  }

  /**
   * Generate a List of JsonApiData to be used in a JsonApiResponse.
   *
   * @param searchResults Stream<SavedSearch> search results
   * @return List<JsonApiData> representing the search results
   */
  private List<JsonApiData> generateListJsonApiData(Stream<SavedSearch> searchResults) {
    return searchResults
        .map(item -> new JsonApiData.Builder()
            .setId(item.getId())
            .setType(type)
            .setAttributes(item.toMap()).build())
        .collect(Collectors.toList());
  }
}
