package ru.kata.spring.boot_seccurity.demo.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_seccurity.demo.model.Role;
import ru.kata.spring.boot_seccurity.demo.model.User;
import ru.kata.spring.boot_seccurity.demo.service.RoleService;
import ru.kata.spring.boot_seccurity.demo.service.UserService;

import java.security.Principal;
import java.util.List;


@Controller
public class AuthController {


    private final UserService userService;
    private final RoleService roleService;



    @Autowired
    public AuthController(
              UserService userService
            , RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }


    @GetMapping("/user")
    public String userPage(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getUsers(Model model, @ModelAttribute("error") String error
            , @ModelAttribute("message") String message) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
        }
        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
        }
        return "admin";
    }


    @GetMapping("/admin/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "new";
    }

    @PostMapping("/admin/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(@ModelAttribute("user") @Valid User user,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "new";
        }
        userService.saveUser(user);
        return "redirect:/admin";
    }


    @PostMapping("/admin/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("Deleting user with id: " + id);
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно удалён!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }


    @GetMapping("/admin/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String getEditUserForm(@RequestParam("id") long id, Model model) {
        User user = userService.getUserById(id);
        List<Role> roles = roleService.getAllRoles(); // Получаем все доступные роли
        model.addAttribute("user", user);
        model.addAttribute("roles", roles); // Передаем роли в модель
        return "update";
    }


    @PostMapping("/admin/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@ModelAttribute("user") @Valid User user,
                             @RequestParam(value = "roles", required = false) List<Long> roleIds,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "update";
        }
        try {
            userService.updateUser(user.getId(), user, roleIds);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно обновлён!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}



