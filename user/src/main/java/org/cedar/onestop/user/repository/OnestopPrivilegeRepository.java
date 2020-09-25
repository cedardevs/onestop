package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnestopPrivilegeRepository extends JpaRepository<OnestopPrivilege, String> {
    Optional<OnestopPrivilege> findByName(String name);
}
