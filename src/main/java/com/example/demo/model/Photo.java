// Photo.java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Type(type = "org.hibernate.type.ImageType")
    @Column(name = "data")
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Column(name = "subject", nullable = true)
    private String subject;

    @Column(name = "encrypted", nullable = false)
    private boolean encrypted;

    // Getters and setters

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Builder pattern
    public static class Builder {
        private byte[] data;
        private User user;
        private String subject;
        private boolean encrypted;

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder encrypted(boolean encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        public Photo build() {
            Photo photo = new Photo();
            photo.setData(this.data);
            photo.setUser(this.user);
            photo.setSubject(this.subject);
            photo.setEncrypted(this.encrypted);
            return photo;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}