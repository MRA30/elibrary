package com.elibrary.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

  @NotEmpty(message = "Email is required")
  @Email(message = "Email is not valid")
  private String email;

}
