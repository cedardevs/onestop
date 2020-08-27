package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.common.*;
import org.cedar.onestop.user.repository.SavedSearchRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.cedar.onestop.user.service.SavedSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/v1")
public class SavedSearchController {
  static String type = "search";
  private SavedSearchRepository savedSearchRepository;
  Logger logger = LoggerFactory.getLogger(SavedSearchController.class);

  @Autowired
  public SavedSearchController(SavedSearchRepository savedSearchRepository) {
    this.savedSearchRepository = savedSearchRepository;
  }

  @Secured("ROLE_ADMIN")
  @ApiOperation(value = "View all available save searches (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/saved-search/all", produces = "application/json")
  public JsonApiResponse getAll() {
    logger.info("Retrieving all saved searches... ");
    List<SavedSearch> searchResults = savedSearchRepository.findAll();
    logger.info("Retrieved " + searchResults.size() + " saved searches.");
    logger.debug(searchResults.toString());
    return getJsonApiResponse(searchResults);
  }

  @Secured("ROLE_ADMIN")
  @ApiOperation(value = "Search with an ID (ADMIN)", response = SavedSearch.class)
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.GET, produces = "application/json")
  public JsonApiResponse getById(@PathVariable(value = "id") String id)
      throws ResourceNotFoundException {
    logger.info("Retrieving saved search for id: " + id);
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
    List<SavedSearch> result = new ArrayList<>();
    result.add(savedSearch);
    return getJsonApiResponse(result);
  }

  @Secured("ROLE_ADMIN")
  @ApiOperation(value = "View all available save searches by UserId (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/saved-search/user/{userId}", produces = "application/json")
  public  JsonApiResponse getByUserId(@PathVariable(value = "userId") String userId)
      throws RuntimeException {
    logger.info("Retrieving user searches for user id: " + userId);
    List <SavedSearch> searchResults = savedSearchRepository.findAllByUserId(userId);
    logger.info("Retrieved " + searchResults.size() + " saved searches for user id " + userId);
    return getJsonApiResponse(searchResults);
  }

  @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
  @ApiOperation(value = "View all user searches", response = Iterable.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successfully retrieved list"),
          @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @GetMapping(value = "/saved-search", produces = "application/json")
  public JsonApiResponse getAuthenticatedUserById(final @AuthenticationPrincipal Authentication authentication)
          throws RuntimeException {
    String userId = authentication.getName();
    logger.info("Retrieving user searches authenticated user with id: " + userId);
    List <SavedSearch> searchResults = savedSearchRepository.findAllByUserId(userId);
    logger.info("Retrieved " + searchResults.size() + " saved searches for user id " + userId);
    return getJsonApiResponse(searchResults);
  }

  @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
  @ApiOperation(value = "Add user search")
  @PostMapping(value = "/saved-search", produces = "application/json")
  public JsonApiResponse create(@RequestBody SavedSearch savedSearch,
                                final @AuthenticationPrincipal Authentication authentication) {
    logger.info("Received saved-search POST request");
    String userId = authentication.getName();
    savedSearch.setUserId(userId);
    logger.info("Creating search for user with ID: " + userId);
    SavedSearch item = savedSearchRepository.save(savedSearch);
    logger.info("Created search with ID: " + userId);
    List<SavedSearch> result = new ArrayList<>();
    result.add(item);
    return getJsonApiResponse(result);
  }

  //todo use postAuth to prevent changing others
  @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
  @ApiOperation(value = "Update user saved search")
  @PutMapping(value = "/saved-search/{id}", produces = "application/json")
  public JsonApiResponse update(@PathVariable(value = "id") String id,
                                @Valid @RequestBody SavedSearch savedSearchDetails)
    throws ResourceNotFoundException {
    logger.info("Received saved-search PUT request");
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id : " + id));
    logger.info("Updating saved-search with id: " + id);
    savedSearch.setUserId(savedSearchDetails.getUserId());
    savedSearch.setName(savedSearchDetails.getName());
    savedSearch.setValue(savedSearchDetails.getValue());
    savedSearch.setCreatedOn(savedSearchDetails.getCreatedOn());
    savedSearch.setLastUpdatedOn(savedSearchDetails.getLastUpdatedOn());
    final SavedSearch updatedSavedSearch = savedSearchRepository.save(savedSearch);
    logger.info("Update complete for search with id: " + id);
    List<SavedSearch> result = new ArrayList<>();
    result.add(updatedSavedSearch);
    return getJsonApiResponse(result);
  }

//todo more to do here so users cannot delete each others request
  @Secured({"ROLE_PUBLIC", "ROLE_ADMIN"})
  @ApiOperation(value = "Delete saved search")
  @DeleteMapping(value = "/saved-search/{id}", produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id,
                                final @AuthenticationPrincipal Authentication authentication)
    throws ResourceNotFoundException {
    logger.info("Received saved-search DELETE request");
    SavedSearch savedSearch = savedSearchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
    logger.info("Deleting saved-search with id: " + id);
    savedSearchRepository.delete(savedSearch);
    logger.info("Delete complete for search with id: " + id);
    Map<String, Boolean> response = new HashMap<>();
    response.put("deleted", Boolean.TRUE);
    return new JsonApiSuccessResponse.Builder()
      .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(response).build()).build();
  }

  private JsonApiResponse getJsonApiResponse(List<SavedSearch> searchResults) {
    List<JsonApiData> dataList = new ArrayList<>();
    if (searchResults != null) {
      Iterator it = searchResults.iterator();
      while (it.hasNext()) {
        SavedSearch searchItem = (SavedSearch) it.next();
        JsonApiData dataItem = new JsonApiData.Builder()
          .setId(searchItem.id)
          .setType(type)
          .setAttributes(searchItem.toMap()).build();
        dataList.add(dataItem);
      }
    }
    JsonApiSuccessResponse response = new JsonApiSuccessResponse.Builder()
            .setData(dataList).build();
    return response;
  }
}
