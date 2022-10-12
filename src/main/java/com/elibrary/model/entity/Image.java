package com.elibrary.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MetaValue;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    @Any (metaColumn = @Column(name = "imageable_type"))
    @AnyMetaDef(idType = "long", metaType = "string",
            metaValues = {
                    @MetaValue(targetEntity = User.class, value = "user"),
                    @MetaValue(targetEntity = Book.class, value = "book")
            })
    @JoinColumn(name = "imageable_id")
    private Object imageable;

    public Image(String image, Object imageable) {
        this.image = image;
        this.imageable = imageable;
    }

}
