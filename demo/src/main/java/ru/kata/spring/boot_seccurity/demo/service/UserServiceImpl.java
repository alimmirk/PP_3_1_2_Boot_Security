package ru.kata.spring.boot_seccurity.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_seccurity.demo.model.Role;
import ru.kata.spring.boot_seccurity.demo.model.User;
import ru.kata.spring.boot_seccurity.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByUserName(String name) {
        return userRepository.findByUsername(name).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        Role userRole = roleService.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role 'ROLE_USER' not found"));
        user.getRoles().add(userRole);

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " not found"));
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Нельзя удалить администратора!");
        }
        userRepository.delete(user);
    }

    @Transactional
    @Override
    public void updateUser(Long id, User user, List<Long> roleIds) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " not found"));

        existingUser.setUsername(user.getUsername());
        existingUser.setCountry(user.getCountry());
        existingUser.setEmail(user.getEmail());

        Optional.ofNullable(user.getPassword())
                .filter(pass -> !pass.trim().isEmpty())
                .filter(pass -> !passwordEncoder.matches(pass, existingUser.getPassword()))
                .ifPresent(pass -> existingUser.setPassword(passwordEncoder.encode(pass)));

        Optional.ofNullable(roleIds)
                .filter(ids -> !ids.isEmpty())
                .ifPresent(ids -> {
                    Set<Role> roles = new HashSet<>(roleService.findRolesById(ids));
                    if (roles.isEmpty()) throw new IllegalStateException("Указанные роли не найдены!");
                    existingUser.setRoles(roles);
                });
    }
}
