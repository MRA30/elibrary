package com.elibrary.controllers;

import com.amazonaws.services.s3.model.Bucket;
import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.dto.response.ResponseData;
import com.elibrary.services.AmazonS3Service;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private AmazonS3Service amazonS3Service;

    @GetMapping("/{image}")
    public void getImage(@PathVariable String image, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(amazonS3Service.downloadFile(image), response.getOutputStream());
    }

    @PostMapping("/upload/user")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<?>> uploadImageUser(@RequestParam("image") MultipartFile image, Principal principal) throws IOException {
        Map<String, String> messagesList = new HashMap<>();
        long id = userService.getProfile(principal).getId();
        System.out.println(image.getOriginalFilename());
        imageService.uploadImageUser(image, id);
        messagesList.put(Constans.MESSAGE, "Image uploaded successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }

    @PostMapping("/upload/book/{id}")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<?>> uploadImageBook(@RequestParam("image") MultipartFile image, @PathVariable("id") long id) throws IOException, BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        imageService.uploadImageBook(image, id);
        messagesList.put(Constans.MESSAGE, "Image uploaded successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList , null));
    }

    @GetMapping("/buckets")
    @RolesAllowed("employee")
    public ResponseEntity<ResponseData<List<Bucket>>> getAllBuckets() {
        Map<String, String> messagesList = new HashMap<>();
        messagesList.put(Constans.MESSAGE, "List of buckets S3");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, amazonS3Service.getAllBuckets()));
    }

    @DeleteMapping("delete/{image}")
    @RolesAllowed("member")
    public ResponseEntity<ResponseData<?>> deleteImage(@PathVariable("image") String image) throws BusinessNotFound {
        Map<String, String> messagesList = new HashMap<>();
        imageService.deleteImage(image);
        messagesList.put(Constans.MESSAGE, "Image deleted successfully");
        return ResponseEntity.ok(new ResponseData<>(true, messagesList, null));
    }
}
