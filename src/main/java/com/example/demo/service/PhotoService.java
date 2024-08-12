// PhotoService.java
package com.example.demo.service;

import com.example.demo.model.Photo;
import com.example.demo.model.User;
import com.example.demo.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;

    private static final Logger logger = Logger.getLogger(PhotoService.class.getName());

    public void save(Photo photo) {
        logger.info("Saving photo: " + photo);
        photoRepository.updatePhoto(photo.getData(), photo.getSubject(), photo.isEncrypted(), photo.getId());
    }

    public void encryptPhoto(Photo photo, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(photo.getData());
            photo.setData(encryptedData);
            photo.setEncrypted(true);
        } catch (Exception e) {
            logger.severe("Error encrypting photo: " + e.getMessage());
        }
    }

    public byte[] decryptPhoto(Photo photo, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = cipher.doFinal(photo.getData());
            photo.setEncrypted(false);
            return decryptedData;
        } catch (Exception e) {
            logger.severe("Error decrypting photo: " + e.getMessage());
            return null;
        }
    }

    public List<Photo> findByUser(User user) {
        return photoRepository.findByUserId(user.getId());
    }

    public Photo findById(Long id) {
        return photoRepository.findById(id).orElse(null);
    }
}