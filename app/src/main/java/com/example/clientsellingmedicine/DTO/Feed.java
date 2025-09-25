package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Feed  implements Serializable {
    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String image;

    public Feed(String title, String link, String description, String pubDate, String image) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.image = image;
    }


}
