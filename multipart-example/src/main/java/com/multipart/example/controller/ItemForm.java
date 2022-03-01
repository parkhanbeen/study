package com.multipart.example.controller;

import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemForm {

  private Long itemId;
  private String itemName;
  private MultipartFile attachFile;
  private List<MultipartFile> imageFiles;
}
