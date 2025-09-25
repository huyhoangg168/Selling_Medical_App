package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification implements Serializable {
    private Integer id;
    private Integer idUser;
    private String title;
    private String content;
    private Date createAt;
    private String image;
}
