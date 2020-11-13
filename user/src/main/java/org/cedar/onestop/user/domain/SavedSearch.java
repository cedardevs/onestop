package org.cedar.onestop.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model class that maps the data stored into save_search table
 * inside database.
 */
@Entity
@Table(name = "onestop_search")
public class SavedSearch {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private OnestopUser user;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "value", nullable = false, length = 2047)
  private String value;

  @Column(name = "filter", length = 2047)
  private String filter;

  @Column(name = "createdOn", updatable = false)
  @CreationTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdOn;

  @Column(name = "lastUpdatedOn")
  @UpdateTimestamp
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastUpdatedOn;

  //constructor will be used by Spring JPA
  protected SavedSearch() {}

  protected SavedSearch(OnestopUser user) {
    this.user = user;
  }

  public SavedSearch(OnestopUser user, String id, String name, String filter, String value) {
    this.user = user;
    this.id = id;
    this.name = name;
    this.filter = filter;
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setUser(OnestopUser user){
    this.user = user;
  }

  public OnestopUser getUser() {
    return user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public Instant getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(Instant lastUpdatedOn) {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public String toString() {
    return "SaveSearchModel{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", value='" + value + '\'' +
        ", createdOn='" + createdOn + '\'' +
        ", lastLogin='" + lastUpdatedOn + '\'' +
        '}';
  }

  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<>();
    result.put("id", id);
    result.put("name", name);
    result.put("value", value);
    result.put("filter", filter);
    result.put("user", user.toMap());
    result.put("createdOn", createdOn);
    result.put("lastUpdatedOn", lastUpdatedOn);
    return result;
  }
}
