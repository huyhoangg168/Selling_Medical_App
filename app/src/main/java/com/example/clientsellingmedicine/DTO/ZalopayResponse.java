package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZalopayResponse implements Serializable {
    private String order_url;
}
