package com.elibrary.services;


import com.elibrary.Constans;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Image;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.ImageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ImageService {

    @Autowired
    private ImageRepo imageRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    public List<String> findAllImageByTypeAndId(String type, long id){
        List<String> images = imageRepo.findAllImageByTypeAndId(type, id);
        if(images.size() == 0){
            return null;
        }
        return images;
    }

    public void uploadImageUser(MultipartFile image, Long id) throws IOException {
        System.out.println(image.getOriginalFilename());
        String originalNameImage = image.getOriginalFilename();
        assert originalNameImage != null;
        int index = originalNameImage.lastIndexOf(".");

        String formatImage = "";
        if(index > 0){
            formatImage = "." + originalNameImage.substring(index + 1);
        }
        String imageName = UUID.randomUUID() + formatImage;
        image.transferTo(new File(Constans.userDirectory + File.separator + imageName));
        User user = userService.findById(id);

        Image imageSave = new Image(
                imageName,
                user
        );
        save(imageSave);
    }

    public void uploadImageBook(MultipartFile image, Long id) throws IOException {
        System.out.println(image.getOriginalFilename());
        String originalNameImage = image.getOriginalFilename();
        assert originalNameImage != null;
        int index = originalNameImage.lastIndexOf(".");

        String formatImage = "";
        if(index > 0){
            formatImage = "." + originalNameImage.substring(index + 1);
        }
        String imageName = UUID.randomUUID() + formatImage;
        image.transferTo(new File(Constans.userDirectory + File.separator + imageName));
        Book book = bookService.findById(id);

        Image imageSave = new Image(
                imageName,
                book
        );
        save(imageSave);
    }

    public Image save(Image image){
        return imageRepo.save(image);
    }

}
