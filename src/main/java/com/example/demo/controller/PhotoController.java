package com.example.demo.controller;

import com.example.demo.model.Photo;
import com.example.demo.model.User;
import com.example.demo.service.PhotoService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "http://localhost:8080")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserService userService;

    private static final Logger logger = Logger.getLogger(PhotoController.class.getName());

    @PostMapping
    public ResponseEntity<Photo> addPhoto(@RequestParam("file") MultipartFile file, @RequestParam(value = "subject", required = false) String subject) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        try {
            byte[] imageData = file.getBytes();

            User user = userService.findByUsername(userDetails.getUsername());

            Photo photo = Photo.builder()
                    .data(imageData)
                    .subject(subject)
                    .user(user)
                    .encrypted(false)
                    .build();

            logger.info("Creating photo: " + photo);

            photoService.save(photo);

            logger.info("Saved photo: " + photo);

            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            logger.severe("Error saving photo: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/duplicate")
    public ResponseEntity<Photo> duplicatePhoto(@RequestParam("photoID") Long photoID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        Photo photo = photoService.findById(photoID);
        if (photo == null || !photo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        Photo duplicatePhoto = Photo.builder()
                .data(photo.getData())
                .subject(photo.getSubject())
                .user(user)
                .build();

        photoService.save(duplicatePhoto);

        return ResponseEntity.ok(duplicatePhoto);
    }

    @GetMapping
    public ResponseEntity<List<Photo>> getUserPhotos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        List<Photo> photos = photoService.findByUser(user);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        Photo photo = photoService.findById(id);
        if (photo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photo.getData());
    }

    @PatchMapping("/subject/{id}")
    public ResponseEntity<Photo> updatePhotoSubject(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String subject = requestBody.get("subject");
        if (subject == null || subject.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        Photo photo = photoService.findById(id);
        if (photo == null || !photo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        photo.setSubject(subject);
        photoService.save(photo);
        return ResponseEntity.ok(photo);
    }

    @PatchMapping("/encrypt/{id}")
    public ResponseEntity<Photo> encryptPhoto(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        Photo photo = photoService.findById(id);
        if (photo == null || !photo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        photoService.encryptPhoto(photo, password);
        photo.setEncrypted(true);
        photoService.save(photo);
        return ResponseEntity.ok(photo);
    }

    @GetMapping("/decrypt/{id}")
    public ResponseEntity<byte[]> decryptPhoto(@PathVariable Long id, @RequestParam("password") String password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(401).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        Photo photo = photoService.findById(id);
        if (photo == null || !photo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] decryptedData = photoService.decryptPhoto(photo, password);
        if (decryptedData == null) {
            return ResponseEntity.status(403).build();
        }
        photoService.save(photo);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(decryptedData);
    }
}