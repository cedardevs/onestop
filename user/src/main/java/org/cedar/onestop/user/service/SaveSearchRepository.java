package org.cedar.onestop.user.service;

import org.springframework.data.jpa.repository.JpaRepository;

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
public interface SaveSearchRepository extends JpaRepository<SaveSearch, String> {
  List<SaveSearch> findAllByUserId(String userId);
  long count();
}
