package ru.kata.spring.boot_seccurity.demo.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_seccurity.demo.model.User;
import ru.kata.spring.boot_seccurity.demo.service.RoleService;
import ru.kata.spring.boot_seccurity.demo.service.UserService;

import java.util.List;

@Controller
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/admin")
    public String getUsers(Model model,
                           @ModelAttribute("error") String error,
                           @ModelAttribute("message") String message) {
        model.addAttribute("users", userService.getAllUsers());
        if (error != null) model.addAttribute("error", error);
        if (message != null) model.addAttribute("message", message);
        return "admin";
    }

    @GetMapping("/admin/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "new";
    }

    @PostMapping("/admin/new")
    public String addUser(@ModelAttribute("user") @Valid User user,
                          @RequestParam(value = "roles", required = false) List<Long> roleIds,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "new";
        try {
            userService.saveUser(user, roleIds); // Передаем roleIds в сервис
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно создан!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно удалён!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/update")
    public String getEditUserForm(@RequestParam("id") long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", roleService.getAllRoles());
        return "update";
    }

    @PostMapping("/admin/update")
    public String updateUser(@ModelAttribute("user") @Valid User user,
                             @RequestParam(value = "roles", required = false) List<Long> roleIds,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "update";
        try {
            userService.updateUser(user.getId(), user, roleIds);
            redirectAttributes.addFlashAttribute("message", "Пользователь успешно обновлён!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}