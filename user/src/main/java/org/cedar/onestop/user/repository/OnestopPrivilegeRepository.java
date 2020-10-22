package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnestopPrivilegeRepository extends JpaRepository<OnestopPrivilege, String> {
    Optional<OnestopPrivilege> findOneByName(String name);
    Page<OnestopPrivilege> findByRolesId(String roleId, Pageable pageable);
    Page<OnestopPrivilege> findByRolesUsersId(String userId, Pageable pageable);
    Page<OnestopPrivilege> findByRolesIdAndRolesUsersId(String roleId, String userId, Pageable pageable);

}
