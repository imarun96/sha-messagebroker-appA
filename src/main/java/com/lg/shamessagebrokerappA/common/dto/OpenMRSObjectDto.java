package com.lg.shamessagebrokerappA.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMRSObjectDto {
    private String givenName;
    private String familyName;
    private String gender;
    private String birthdate;
    private String address1;
    private String cityVillage;
    private String country;
    private String postalCode;
}