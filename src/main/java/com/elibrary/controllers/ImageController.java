package com.elibrary.controllers;

import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.ImageService;
import com.elibrary.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @GetMapping("/{image}")
    public void getImage(@PathVariable String image, HttpServletResponse response) throws IOException {
        var imageFile = new ClassPathResource("/images/" + image);
        System.out.println(imageFile.getInputStream());

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imageFile.getInputStream(), response.getOutputStream());
    }

    @PostMapping("/upload/user")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<?>> uploadImageUser(@RequestParam("image") MultipartFile image, Principal principal) throws IOException {
        Map<String, String> messagesList = new HashMap<>();
            long id = userService.getProfile(principal).getId();
            imageService.uploadImageUser(image, id);
            messagesList.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/upload/book/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> uploadImageBook(@RequestParam("image") MultipartFile image, @PathVariable("id") long id) throws IOException {
        Map<String, String> messagesList = new HashMap<>();
            imageService.uploadImageBook(image, id);
            messagesList.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(new ResponseData<>(true, messagesList , null));
    }
}
