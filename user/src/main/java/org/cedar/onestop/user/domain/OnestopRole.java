package org.cedar.onestop.user.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="onestop_roles")
public class OnestopRole {

    @Id
    @Column(name= "role_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    public String id;

    @Column(name = "name", nullable = false)
    public String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<OnestopUser> users;

    @Column(name = "createdOn", updatable = false)
    @CreationTimestamp
    public Date createdOn; //TODO do we also accept part of the query

    @Column(name = "lastUpdatedOn")
    @UpdateTimestamp
    public Date lastUpdatedOn; ///TODO do we also accept part of the query

    @ManyToMany
    @JoinTable(
            name = "onestop_roles_privileges",
            joinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "role_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "privilege_id", referencedColumnName = "privilege_id"))
    private Collection<OnestopPrivilege> privileges;

    public OnestopRole(){
    }

    public OnestopRole(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public OnestopRole(String name) {
        this.name = name;
    }

    public OnestopRole(String id, String name, Collection<OnestopPrivilege> privileges) {
        this.id = id;
        this.name = name;
        this.privileges = privileges;
    }

    public OnestopRole(String name, Collection<OnestopPrivilege> privileges) {
        this.name = name;
        this.privileges = privileges;
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

    public Collection<OnestopUser> getUsers() {
        return users;
    }

    public void setUsers(Collection<OnestopUser> users) {
        this.users = users;
    }

    public Collection<OnestopPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Collection<OnestopPrivilege> privileges) {
        this.privileges = privileges;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", id);
        result.put("name", name);
        result.put("privileges", privToStringList());
        result.put("createdOn", createdOn);
        result.put("lastUpdatedOn", lastUpdatedOn);
        return result;
    }

    public String toString(){
        return toMap().toString();
    }

    public List<Map> privToStringList(){
        List<Map> privList = new ArrayList<>();
        if(privileges == null){
            return privList;
        }
        for(int i = 0; i < privileges.size(); i++) {
            privList.add(((List<OnestopPrivilege>)privileges).get(i).toMap());
        }
        return privList;
    }
}
