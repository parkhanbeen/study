package com.multipart.example.file;

import com.multipart.example.domain.UploadFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStore {

  @Value("${file.dir}")
  private String fileDir;

  public String getFullPath(String fileName) {
    return fileDir + fileName;
  }

  public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
    List<UploadFile> storeFileResult = new ArrayList<>();
    for (MultipartFile uploadFile : multipartFiles) {
      if (!uploadFile.isEmpty()) {
        storeFileResult.add(storeFile(uploadFile));
      }
    }
    return storeFileResult;
  }

  public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
    if (multipartFile.isEmpty()) {
      return null;
    }

    String originalFilename = multipartFile.getOriginalFilename();
    String storeFileName = createStoreFileName(originalFilename);
    multipartFile.transferTo(new File(getFullPath(storeFileName)));

    return new UploadFile(originalFilename, storeFileName);
  }

  private String createStoreFileName(String originalFilename) {
    String ext = extractExt(originalFilename);
    String uuid = UUID.randomUUID().toString();
    return uuid + "." + ext;
  }

  private String extractExt(String originalFilename) {
    int position = originalFilename.lastIndexOf(".");
    return originalFilename.substring(position + 1);
  }
}
