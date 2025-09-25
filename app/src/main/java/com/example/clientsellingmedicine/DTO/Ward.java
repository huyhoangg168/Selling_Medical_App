package com.example.clientsellingmedicine.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Ward {
    private Integer id;
    private Integer idDistrict;
    private String name;
}
