package org.cedar.onestop.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "onestop_roles")
public class OnestopRole {

  @Id
  @Column(name = "role_id")
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @ManyToMany(mappedBy = "roles")
  private List<OnestopUser> users;

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Instant createdOn;

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Instant lastUpdatedOn;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "onestop_roles_privileges",
      joinColumns = @JoinColumn(
          name = "role_id", referencedColumnName = "role_id"),
      inverseJoinColumns = @JoinColumn(
          name = "privilege_id", referencedColumnName = "privilege_id"))
  private List<OnestopPrivilege> privileges = new ArrayList<>();

  public OnestopRole() {}

  public OnestopRole(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public OnestopRole(String name) {
    this.name = name;
  }

  public OnestopRole(String id, String name, List<OnestopPrivilege> privileges) {
    this.id = id;
    this.name = name;
    this.privileges = privileges != null ? privileges : new ArrayList<>();
  }

  public OnestopRole(String name, List<OnestopPrivilege> privileges) {
    this.name = name;
    this.privileges = privileges != null ? privileges : new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<OnestopPrivilege> getPrivileges() {
    return privileges;
  }

  public void setPrivileges(List<OnestopPrivilege> privileges) {
    this.privileges = privileges != null ? privileges : new ArrayList<>();
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public Instant getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<>();
    result.put("id", id);
    result.put("name", name);
    result.put("privileges", privsToMapList());
    result.put("createdOn", createdOn);
    result.put("lastUpdatedOn", lastUpdatedOn);
    return result;
  }

  public String toString() {
    return toMap().toString();
  }

  public List<Map<String, Object>> privsToMapList() {
    return privileges.stream()
        .map(OnestopPrivilege::toMap)
        .collect(Collectors.toList());
  }
}
