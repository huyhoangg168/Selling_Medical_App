package com.example.clientsellingmedicine.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class District {
    private Integer id;
    private Integer idProvince;
    private String name;
}
