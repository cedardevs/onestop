package org.cedar.onestop.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cedar.onestop.data.api.*;
import org.cedar.onestop.user.config.SecurityConfig;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.repository.SavedSearchRepository;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.cedar.onestop.user.domain.SavedSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/v1")
public class SavedSearchController {
    static String type = "search";
    private SavedSearchRepository savedSearchRepository;
    private OnestopUserRepository onestopUserRepo;
    Logger logger = LoggerFactory.getLogger(SavedSearchController.class);

    @Autowired
    public SavedSearchController(OnestopUserRepository onestopUserRepo, SavedSearchRepository savedSearchRepository) {
        this.onestopUserRepo = onestopUserRepo;
        this.savedSearchRepository = savedSearchRepository;
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "View all available save searches (ADMIN)", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/saved-search/all", produces = "application/json")
    public JsonApiResponse getAll(HttpServletResponse response) {
        logger.info("Retrieving all saved searches... ");
        List<SavedSearch> searchResults = savedSearchRepository.findAll();
        logger.info("Retrieved " + searchResults.size() + " saved searches.");
        logger.debug(searchResults.toString());
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK.value(), response)
                .setData(generateListJsonApiData(searchResults)).build();
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "Search with an ID (ADMIN)", response = SavedSearch.class)
    @RequestMapping(value = "/saved-search/{id}", method = RequestMethod.GET, produces = "application/json")
    public JsonApiResponse getById(@PathVariable(value = "id") String id,
                                   HttpServletResponse response)
            throws ResourceNotFoundException {
        logger.info("Retrieving saved search for id: " + id);
        SavedSearch savedSearch = savedSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
        List<SavedSearch> result = new ArrayList<>();
        result.add(savedSearch);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK.value(), response)
                .setData(generateListJsonApiData(result)).build();
    }

    @Secured("ROLE_ADMIN")
    @ApiOperation(value = "View all available save searches by UserId (ADMIN)", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/saved-search/user/{userId}", produces = "application/json")
    public  JsonApiResponse getByUserId(@PathVariable(value = "userId") String userId,
                                        HttpServletResponse response)
            throws ResourceNotFoundException {
        logger.info("Retrieving user searches for user id: " + userId);
        OnestopUser user = onestopUserRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for requested id :: " + userId));
        Collection<SavedSearch> searchResults = user.getSearches();
        logger.info("Retrieved " + searchResults.size() + " saved searches for user id " + userId);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK.value(), response)
                .setData(generateListJsonApiData(new ArrayList<>(searchResults))).build();
    }

    @Secured({"ROLE_" + SecurityConfig.PUBLIC_ROLE, "ROLE_" + SecurityConfig.ADMIN_ROLE})
    @ApiOperation(value = "View all user searches", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/saved-search", produces = "application/json")
    public JsonApiResponse getAuthenticatedUserById(final @AuthenticationPrincipal Authentication authentication,
                                                    HttpServletResponse response)
            throws ResourceNotFoundException {
        String userId = authentication.getName();
        logger.info("Retrieving searches for user: " + userId);
        OnestopUser user = onestopUserRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for requested id :: " + userId));
        logger.info("Retrieving user searches authenticated user with id: " + userId);
        Set<SavedSearch> searchResults = user.getSearches();
        logger.info("Retrieved " + searchResults.size() + " saved searches for user id " + userId);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK.value(), response)
                .setData(generateListJsonApiData(new ArrayList<>(searchResults))).build();    }

    @Secured({"ROLE_" + SecurityConfig.PUBLIC_ROLE, "ROLE_" + SecurityConfig.ADMIN_ROLE})
    @ApiOperation(value = "Add user search")
    @PostMapping(value = "/saved-search", produces = "application/json")
    public JsonApiResponse create(@RequestBody SavedSearch savedSearch,
                                  final @AuthenticationPrincipal Authentication authentication,
                                  HttpServletResponse response)
            throws ResourceNotFoundException{
        logger.info("Received saved-search POST request");
        String userId = authentication.getName();
        OnestopUser user = onestopUserRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for requested id :: " + userId));
        logger.info("Creating search for user with ID: " + userId);
        savedSearch.setUser(user);
        SavedSearch item = savedSearchRepository.save(savedSearch);
        user.addSearch(item);
        onestopUserRepo.save(user);
        logger.info("Created search with ID: " + userId);
        List<SavedSearch> result = new ArrayList<>();
        result.add(item);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.CREATED.value(), response)
                .setData(generateListJsonApiData(result)).build();
    }

    //todo use postAuth to prevent changing others
    @Secured({"ROLE_" + SecurityConfig.PUBLIC_ROLE, "ROLE_" + SecurityConfig.ADMIN_ROLE})
    @ApiOperation(value = "Update user saved search")
    @PutMapping(value = "/saved-search/{id}", produces = "application/json")
    public JsonApiResponse update(@PathVariable(value = "id") String id,
                                  @Valid @RequestBody SavedSearch savedSearchDetails,
                                  HttpServletResponse response)
            throws ResourceNotFoundException {
        logger.info("Received saved-search PUT request");
        SavedSearch savedSearch = savedSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Save search not found for this id : " + id));
        logger.info("Updating saved-search with id: " + id);
        savedSearch.setName(savedSearchDetails.getName());
        savedSearch.setValue(savedSearchDetails.getValue());
        savedSearch.setCreatedOn(savedSearchDetails.getCreatedOn());
        savedSearch.setLastUpdatedOn(savedSearchDetails.getLastUpdatedOn());
        final SavedSearch updatedSavedSearch = savedSearchRepository.save(savedSearch);
        logger.info("Update complete for search with id: " + id);
        List<SavedSearch> result = new ArrayList<>();
        result.add(updatedSavedSearch);
        return new JsonApiSuccessResponse.Builder()
                .setStatus(HttpStatus.OK.value(), response)
                .setData(generateListJsonApiData(result)).build();
    }

    //todo more to do here so users cannot delete each others request
    @Secured({"ROLE_" + SecurityConfig.PUBLIC_ROLE, "ROLE_" + SecurityConfig.ADMIN_ROLE})
    @ApiOperation(value = "Delete saved search")
    @DeleteMapping(value = "/saved-search/{id}", produces = "application/json")
    public JsonApiResponse delete(@PathVariable(value = "id") String id,
                                  final @AuthenticationPrincipal Authentication authentication,
                                  HttpServletResponse response)
            throws ResourceNotFoundException {
        logger.info("Received saved-search DELETE request");
        SavedSearch savedSearch = savedSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Save search not found for requested id :: " + id));
        logger.info("Deleting saved-search with id: " + id);
        savedSearchRepository.delete(savedSearch);
        logger.info("Delete complete for search with id: " + id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("deleted", Boolean.TRUE);
        return new JsonApiSuccessResponse.Builder()
            .setStatus(HttpStatus.OK.value(), response)
            .setMeta(new JsonApiMeta.Builder().setNonStandardMetadata(result).build()).build();
    }

    /**
     * Generate a List of JsonApiData to be used in a JsonApiResponse.
     * @param searchResults List<SavedSearch> search results
     * @return List<JsonApiData> representing the search results
     */
    private List<JsonApiData> generateListJsonApiData(List<SavedSearch> searchResults) {
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
        return dataList;
    }
}
