package com.boot.contractmanagaer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Contact")
public class Contacts {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int cid;

    @NotBlank(message = "Name is required")
    private String name;

    private String work;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(unique=true)
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String image;

    @Column(length = 10000)
    @Size(max = 10000, message = "Description should not exceed 1000 characters")
    private String description;

    @ManyToOne
    @JsonIgnore
    private User user;

    // Constructors
    public Contacts() {
        super();
    }

    public Contacts(int cid, String name, String work, String email, String phone, String image, String description) {
        this.cid = cid;
        this.name = name;
        this.work = work;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.description = description;
    }

    // Getters and Setters
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // toString for logging/debugging
    @Override
    public String toString() {
        return "Contacts [cid=" + cid + ", name=" + name + ", work=" + work + ", email=" + email
                + ", phone=" + phone + ", image=" + image + ", description=" + description
                + ", user=" + (user != null ? user.getId() : "null") + "]";
    }

}
