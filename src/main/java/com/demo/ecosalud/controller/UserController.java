package com.demo.ecosalud.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDTO UserRegister(@RequestBody UserDTO user) {
         return userService.register(user);
    }
    
    
}
