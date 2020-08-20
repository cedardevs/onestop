package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.user.common.*;
import org.cedar.onestop.user.repository.SavedSearchRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.cedar.onestop.user.service.SavedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @Autowired
  public SavedSearchController(SavedSearchRepository savedSearchRepository) {
    this.savedSearchRepository = savedSearchRepository;
  }

  @ApiOperation(value = "View all available save searches (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search/all", method = RequestMethod.GET, produces = "application/json")
  public JsonApiResponse getAll() {
    List<SavedSearch> searchResults = savedSearchRepository.findAll();
    return getJsonApiResponse(searchResults);
  }

  //TODO do we need it?
//  @ApiOperation(value = "Search with an ID (ADMIN)", response = SavedSearch.class)
//  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.GET, produces = "application/json")
//  public ResponseEntity<SavedSearch> getById(@PathVariable(value = "id") String id)
//      throws ResourceNotFoundException {
//    SavedSearch savedSearch = savedSearchRepository.findById(id)
//        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
//    return ResponseEntity.ok().body(savedSearch);
//  }

  @ApiOperation(value = "View all available save searches by UserId (ADMIN)", response = Iterable.class)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully retrieved list"),
      @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search/user/{userId}", method = RequestMethod.GET, produces = "application/json")
  public  JsonApiResponse getByUserId(final @AuthenticationPrincipal Authentication authentication, @PathVariable(value = "userId") String userId)
      throws RuntimeException {
    if(authentication != null && authentication.getName() == userId) {
      List <SavedSearch> searchResults = savedSearchRepository.findAllByUserId(userId);
      return getJsonApiResponse(searchResults);
    }
    else{
      return new JsonApiErrorResponse.Builder().setCode("Unauthorized").setStatus(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @ApiOperation(value = "View all user searches", response = Iterable.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successfully retrieved list"),
          @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
  })
  @RequestMapping(value = "/saved-search", method = RequestMethod.GET, produces = "application/json")
  public JsonApiResponse getByUserId(final @AuthenticationPrincipal Authentication authentication)
          throws RuntimeException {
    if(authentication != null) {
      String userId = authentication.getName();
      List <SavedSearch> searchResults = savedSearchRepository.findAllByUserId(userId);
      return getJsonApiResponse(searchResults);
    }
    else{
      return new JsonApiErrorResponse.Builder().setCode("Unauthorized").setStatus(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @ApiOperation(value = "Add user search")
  @RequestMapping(value = "/saved-search", method = RequestMethod.POST, produces = "application/json")
  public JsonApiResponse create(@RequestBody SavedSearch savedSearch,
                                final @AuthenticationPrincipal Authentication authentication) {
    if(authentication != null) {
      String userId = authentication.getName();
      savedSearch.setUserId(userId);
      SavedSearch item = savedSearchRepository.save(savedSearch);
      List<SavedSearch> result = new ArrayList<>();
      result.add(item);
      return getJsonApiResponse(result);
    }
    else{
      return new JsonApiErrorResponse.Builder().setCode("Unauthorized").setStatus(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @ApiOperation(value = "Update user saved search")
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.PUT, produces = "application/json")
  public JsonApiResponse update(@PathVariable(value = "id") String id,
                                @Valid @RequestBody SavedSearch savedSearchDetails)
    throws ResourceNotFoundException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id : " + id));

    savedSearch.setUserId(savedSearchDetails.getUserId());
    savedSearch.setName(savedSearchDetails.getName());
    savedSearch.setValue(savedSearchDetails.getValue());
    savedSearch.setCreatedOn(savedSearchDetails.getCreatedOn());
    savedSearch.setLastUpdatedOn(savedSearchDetails.getLastUpdatedOn());
    final SavedSearch updatedSavedSearch = savedSearchRepository.save(savedSearch);
    List<SavedSearch> result = new ArrayList<>();
    result.add(updatedSavedSearch);
    return getJsonApiResponse(result);
  }

//todo more to do here so users cannot delete each others request
  @ApiOperation(value = "Delete saved search")
  @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.DELETE, produces = "application/json")
  public JsonApiResponse delete(@PathVariable(value = "id") String id,
                                final @AuthenticationPrincipal Authentication authentication)
    throws ResourceNotFoundException {
    if(authentication != null) {
      SavedSearch savedSearch = savedSearchRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
      savedSearchRepository.delete(savedSearch);
      Map<String, Boolean> response = new HashMap<>();
      response.put("deleted", Boolean.TRUE);
      return new JsonApiSuccessResponse.Builder()
        .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(response).build()).build();
    }else{
      return new JsonApiErrorResponse.Builder().setCode("Unauthorized").setStatus(HttpStatus.UNAUTHORIZED).build();
    }
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

    return new JsonApiSuccessResponse.Builder()
      .setData(dataList).build();
  }
}
