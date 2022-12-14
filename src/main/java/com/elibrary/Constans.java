package com.elibrary;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.FileSystems;

public class Constans {

    public final static String SECRET_KEY = "elibrary";
    public final static long TOKEN_VALIDITY = System.currentTimeMillis() + 1000 * 60 * 60 * 5;
    public final static int COOKIE_VALID = 60 * 60 * 5;
    public final static long TIME = System.currentTimeMillis();
    public final static String ACCESS_TOKEN = "access_token";
    public final static String REFRESH_TOKEN = "refresh_token";

    public final static double PENALTY = 5000;

    public final static String userDirectory = FileSystems.getDefault().getPath("").toAbsolutePath() + "/src/main/resources/images";

    public final static String MESSAGE = "message";

    public final static String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

    public final static String AWSS3_ACCESS_KEY = "AKIAWEISPLJROECY6V4V";

    public final static String AWSS3_SECRET_KEY = "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";

    public final static String AWSS3_BUCKET_NAME = "elibrary-springboot";

    public final static String AWSS3_REGION = "ap-southeast-1";

    public final static String AWSS3_URL = "https://elibrary-springboot.s3-ap-southeast-1.amazonaws.com/";
}
