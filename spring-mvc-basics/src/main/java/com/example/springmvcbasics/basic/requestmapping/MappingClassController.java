package com.example.springmvcbasics.basic.requestmapping;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MappingClassController {

  @GetMapping("/users")
  public String users() {
    return "users";
  }

  @PostMapping("/users")
  public String add() {
    return "add";
  }

  @GetMapping("/users/{userId}")
  public String find(@PathVariable("userId") String userId) {
    return "find" + userId;
  }

  @PatchMapping("/users/{userId}")
  public String change(@PathVariable("userId") String userId) {
    return "change" + userId;
  }

  @DeleteMapping("/users/{userId}")
  public String delete(@PathVariable("userId") String userId) {
    return "delete" + userId;
  }
}
