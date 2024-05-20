package com.capstone.emergency.pharmacy.core.email.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EmailDto {

    private String emailTo;

    private String subject;

    private String text;

    private List<String> ccRecipients = new ArrayList<>();
}
