package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.service.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaveSearchRepository extends the CrudRepository interface.

 * The type of entity and ID that it works with, SaveSearch and String, are

 * specified in the generic parameters on CrudRepository.
 * By extending CrudRepository, SaveSearchRepository inherits several

 * methods for working with SaveSearch persistence, including methods for

 * saving, deleting, and finding save search entities.
 *
 */
@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, String> {
  List<SavedSearch> findAllByUserId(String userId);
  long count();
}
