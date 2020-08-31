package org.cedar.onestop.user.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collection;

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
        this.name = name;
    }

    public OnestopRole(String name) {
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
}
