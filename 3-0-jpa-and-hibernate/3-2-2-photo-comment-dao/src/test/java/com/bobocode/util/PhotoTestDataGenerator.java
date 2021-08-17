package com.bobocode.util;

import com.bobocode.model.Photo;
import com.bobocode.model.PhotoComment;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PhotoTestDataGenerator {
    public static Photo createRandomPhoto(){
        Photo photo = new Photo();
        photo.setUrl(RandomStringUtils.randomAlphabetic(30));
        photo.setDescription(RandomStringUtils.randomAlphabetic(50));
        return photo;
    }

    public static PhotoComment createRandomPhotoComment() {
        PhotoComment photoComment = new PhotoComment();
        photoComment.setCreatedOn(LocalDateTime.now());
        photoComment.setText(RandomStringUtils.randomAlphabetic(50));
        return photoComment;
    }

    public static List<Photo> createListOfRandomPhotos(int size) {
        return Stream.generate(PhotoTestDataGenerator::createRandomPhoto).limit(size).collect(toList());
    }
    public static List<PhotoComment> createListOfRandomComments(int size) {
        return Stream.generate(PhotoTestDataGenerator::createRandomPhotoComment).limit(size).collect(toList());
    }
}

