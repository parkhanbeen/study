package com.multipart.example.controller;

import com.multipart.example.domain.Item;
import com.multipart.example.domain.ItemRepository;
import com.multipart.example.domain.UploadFile;
import com.multipart.example.file.FileStore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

  private final ItemRepository itemRepository;
  private final FileStore fileStore;

  @GetMapping("/items/new")
  public String newItem(@ModelAttribute ItemForm form) {
    return "item-form";
  }

  @PostMapping("/items/new")
  public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes)
      throws IOException {
    UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
    List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

    Item item = new Item();
    item.setItemName(form.getItemName());
    item.setAttachFile(attachFile);
    item.setImageFiles(storeImageFiles);
    itemRepository.save(item);

    redirectAttributes.addAttribute("itemId", item.getId());
    return "redirect:/items/{itemId}";
  }

  @GetMapping("/items/{id}")
  public String items(@PathVariable Long id, Model model) {
    Item findItem = itemRepository.findById(id);
    model.addAttribute("item", findItem);
    return "item-view";
  }

  @ResponseBody
  @GetMapping("/images/{fileName}")
  public Resource downloadImage(@PathVariable String fileName) throws MalformedURLException {
    return new UrlResource("file:" + fileStore.getFullPath(fileName));
  }

  @GetMapping("/attach/{itemId}")
  public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId)
      throws MalformedURLException {
    Item findItem = itemRepository.findById(itemId);
    String storeFileName = findItem.getAttachFile().getStoreFileName();
    String uploadFileName = findItem.getAttachFile().getUploadFileName();

    UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

    log.info("uploadFileName = {}", uploadFileName);

    String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
    String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
        .body(resource);
  }
}
