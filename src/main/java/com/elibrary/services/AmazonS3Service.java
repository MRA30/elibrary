package com.elibrary.services;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.elibrary.Constans;
import com.elibrary.config.AWSConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AmazonS3Service {

    private final AWSConfig awsConfig;

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        awsConfig.amazonS3().putObject(Constans.AWSS3_BUCKET_NAME, fileName, file);
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
            File file = convertMultiPartToFile(multipartFile);
            String originalNameImage = multipartFile.getOriginalFilename();
            assert originalNameImage != null;
            int index = originalNameImage.lastIndexOf(".");
            String formatImage = "";
            if(index > 0){
                formatImage = "." + originalNameImage.substring(index + 1);
            }
            String fileName = generateFileName() + formatImage;
            uploadFileTos3bucket(fileName, file);
            file.delete();
            return fileName;
    }

    public List<Bucket> getAllBuckets() {
        return awsConfig.amazonS3().listBuckets();
    }

    public List<String> getAllFileInBucket() {
        return awsConfig.amazonS3().listObjectsV2(Constans.AWSS3_BUCKET_NAME).getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    // download s3 file
    public InputStream downloadFile(String keyName) {
        return awsConfig.amazonS3().getObject(Constans.AWSS3_BUCKET_NAME, keyName).getObjectContent();
    }

    //delete s3 file
    public void deleteFile(String fileName) {
        awsConfig.amazonS3().deleteObject(Constans.AWSS3_BUCKET_NAME, fileName);
    }
}
