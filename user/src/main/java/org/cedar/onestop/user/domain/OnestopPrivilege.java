package org.cedar.onestop.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "onestop_privileges")
public class OnestopPrivilege {
  @Id
  @Column(name = "privilege_id")
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdOn;

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedOn;

  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "privileges", fetch = FetchType.EAGER)
  private List<OnestopRole> roles;

  protected OnestopPrivilege() {}

  public OnestopPrivilege(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public OnestopPrivilege(String name) {
    this.name = name;
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
    result.put("createdOn", createdOn);
    result.put("lastUpdatedOn", lastUpdatedOn);
    return result;
  }

  public String toString() {
    return name;
  }
}
