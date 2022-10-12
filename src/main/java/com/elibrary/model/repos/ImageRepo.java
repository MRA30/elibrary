package com.elibrary.model.repos;

import com.elibrary.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepo extends JpaRepository<Image, Long> {

    @Query(nativeQuery = true, value = "select image from image where imageable_type = ?1 and imageable_id = ?2")
    List<String> findAllImageByTypeAndId(String type, long id);

}
