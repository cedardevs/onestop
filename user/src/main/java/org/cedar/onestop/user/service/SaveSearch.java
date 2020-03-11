package org.cedar.onestop.user.service;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "savesearch") // table name need to be in external config
public class SaveSearch {
  public String id;
  public String userId;
  public String name;
  public String value;

  @Column(name = "creation_date", updatable = false)
  @CreationTimestamp
  protected Date createdOn; //TODO get it as a query or generate it in user

  @Column(name = "last_updated")
  @UpdateTimestamp
  protected Date lastUpdatedOn; //TODO get it as a query or generate it in user

  public SaveSearch(String id, String userId, String name, String value, Date createdOn, Date lastUpdatedOn) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.value = value;
    this.createdOn = createdOn;
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public SaveSearch() {
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  @Column(name = "PR_KEY")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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

  public Date getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(Date lastUpdatedOn) {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public String toString() {
    return "SaveSearchModel{" +
        "id=" + id +
        ", userId='" + userId + '\'' +
        ", name='" + name + '\'' +
        ", value='" + value + '\'' +
        ", createdOn='" + createdOn + '\'' +
        ", lastLogin='" + lastUpdatedOn + '\'' +
        '}';
  }


}
