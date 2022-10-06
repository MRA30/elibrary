package com.elibrary.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "image")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    @Any(metaColumn = @Column(name = "imageable_type"))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(value = "user", targetEntity = User.class),
            @MetaValue(value = "book", targetEntity = Book.class)
    })

    @JoinColumn(name="imageable_id")
    private Object item;


    public Object getItem() {
        return item;
    }

    public Image(String image, Object item) {
        this.image = image;
        this.item = item;
    }

}
