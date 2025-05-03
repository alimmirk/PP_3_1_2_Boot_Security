package ru.kata.spring.boot_seccurity.demo.service;

import ru.kata.spring.boot_seccurity.demo.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    public Optional<Role> findByName(String roleName);

    public Role save(Role role);

    public List<Role> getAllRoles();

    public List<Role> findRolesByIds(List<Long> roleIds);


}
