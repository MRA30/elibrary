package com.elibrary.dto.request;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class UpdateImageRequest {

    @NotEmpty(message = "image is required")
    private String image;

}
