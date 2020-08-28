package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnestopPrivilegeRepository extends JpaRepository<OnestopPrivilege, String> {
    OnestopPrivilege findByName(String name);
}
