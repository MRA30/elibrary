package com.elibrary.services;


import com.elibrary.Constans;
import com.elibrary.Exception.BusinessNotFound;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.Image;
import com.elibrary.model.entity.User;
import com.elibrary.model.repos.ImageRepo;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepo imageRepo;

    private final UserService userService;

    private final BookService bookService;


    private final AmazonS3Service amazonS3Service;

    public void uploadImageUser(MultipartFile image, Long id) throws IOException {
        if(image.isEmpty()){
            throw new IOException("Image is empty");
        }
//        String originalNameImage = image.getOriginalFilename();
//        assert originalNameImage != null;
//        int index = originalNameImage.lastIndexOf(".");
//
//        String formatImage = "";
//        if(index > 0){
//            formatImage = "." + originalNameImage.substring(index + 1);
//        }
//        String imageName = UUID.randomUUID() + formatImage;
//        image.transferTo(new File(Constans.userDirectory + File.separator + imageName));
        User user = userService.findById(id);

        String imageName = amazonS3Service.uploadFile(image);

        Image imageSave = new Image(
                imageName,
                user
        );
        save(imageSave);
    }

    public void uploadImageBook(MultipartFile image, Long id) throws IOException, BusinessNotFound {
        if(image.isEmpty()){
            throw new IOException("Image is empty");
        }
        if(bookService.findById(id) != null){
//            System.out.println(image.getOriginalFilename());
//            String originalNameImage = image.getOriginalFilename();
//            assert originalNameImage != null;
//            int index = originalNameImage.lastIndexOf(".");
//
//            String formatImage = "";
//            if(index > 0){
//                formatImage = "." + originalNameImage.substring(index + 1);
//            }
//            String imageName = UUID.randomUUID() + formatImage;
//            image.transferTo(new File(Constans.userDirectory + File.separator + imageName));
            String imageName = amazonS3Service.uploadFile(image);
            Book book = bookService.findById(id);

            Image imageSave = new Image(
                    imageName,
                    book
            );
            save(imageSave);
        }else{
            throw new BusinessNotFound("Book not found");
        }

    }

    public Image save(Image image){
        return imageRepo.save(image);
    }

    public void deleteImage(String image) throws BusinessNotFound {
        if(imageRepo.findByImage(image) != null) {
            Image imageDelete = imageRepo.findByImage(image);
            amazonS3Service.deleteFile(image);
            imageRepo.deleteById(imageDelete.getId());
        }else {
            throw new BusinessNotFound("Image not found");
        }

    }

}
