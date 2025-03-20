package ru.yandex.practicum.shop.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("images")
@Getter
@Setter
public class Image {

    @Id
    private Long id;

    @Column("image_data")
    private byte[] imageData;

}
