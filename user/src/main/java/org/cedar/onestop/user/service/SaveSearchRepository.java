package org.cedar.onestop.user.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaveSearchRepository extends JpaRepository<SaveSearch, String> {
}
