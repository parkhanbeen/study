package com.multipart.example.domain;

import lombok.Getter;

@Getter
public class UploadFile {
  private final String uploadFileName;
  private final String storeFileName;

  public UploadFile(String uploadFileName, String storeFileName) {
    this.uploadFileName = uploadFileName;
    this.storeFileName = storeFileName;
  }
}
