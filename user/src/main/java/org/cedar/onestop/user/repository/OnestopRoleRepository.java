package org.cedar.onestop.user.repository;

import org.cedar.onestop.user.domain.OnestopRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnestopRoleRepository extends JpaRepository<OnestopRole, String> {
    OnestopRole findByName(String name);
}
