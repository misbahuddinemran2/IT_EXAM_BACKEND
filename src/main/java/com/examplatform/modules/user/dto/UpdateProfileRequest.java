package com.examplatform.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String fullNameBn;
    private String avatarUrl;
    private String gender;          // MALE, FEMALE, OTHER
    private LocalDate dateOfBirth;
    private String district;
    private String educationLevel;  // SSC, HSC, HONORS, MASTERS, OTHER
    private String targetExam;
}