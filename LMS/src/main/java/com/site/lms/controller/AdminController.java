package com.site.lms.controller;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.site.lms.entity.MemberRole;
import com.site.lms.entity.User;
import com.site.lms.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    
    public AdminController(UserService us) {
        this.userService = us;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        List<MemberRole> mr = userService.findAllRole();
        model.addAttribute("users", users);
        model.addAttribute("codes", mr);
        return "adminUsers";
    }

    @PostMapping("/create")
    public String createUser(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "authority") Integer authority) {
        userService.createUser(username, password, email, authority);
        return "redirect:/admin/users";
    }

    @PostMapping("/update")
    public String updateUser(
            @RequestParam(name = "id") Long id,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "authority") Integer authority) {
        userService.updateUser(id, username, password, email, authority);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete")
    public String deleteUser(
            @RequestParam(name = "id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    
    @PostMapping("/createCode")
    public String createCode(
            @RequestParam(name = "value") Integer value,
            @RequestParam(name = "authority") Integer authority) {
    	
    	for(int i = 0; i < value; i++) {
    		userService.createRole(authority + "_" + generateRandomString(8));
    	}
    	
        return "redirect:/admin/users";
    }
    
    @PostMapping("/deleteCode")
    public String deleteCode(
            @RequestParam(name = "roleCode") String roleCode) {
        userService.deleteRole(roleCode);
        return "redirect:/admin/users";
    }
    
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }

        return result.toString();
    }
    
}
