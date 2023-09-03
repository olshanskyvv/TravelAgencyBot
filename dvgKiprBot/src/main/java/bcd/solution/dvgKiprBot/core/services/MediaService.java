package bcd.solution.dvgKiprBot.core.services;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import bcd.solution.dvgKiprBot.core.models.CustomTour;
import bcd.solution.dvgKiprBot.core.models.Hotel;
import bcd.solution.dvgKiprBot.core.models.Resort;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Service
public class MediaService {
    private final ResourcePatternResolver resourcePatternResolver;

    public MediaService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    //TODO: absolute path is piece of shit has to be rewrote quickly!
    @SneakyThrows
    public InputMedia getStartMedia(){
        InputMedia file = new InputMediaPhoto();
        file.setMedia((new ClassPathResource("images/kiprstart.jpg")).getInputStream(), "kiprstart.jpg");
        //TODO logic
        return file;
    }

    @SneakyThrows
    public InputMedia getNoPhotoMedia(){
        InputMedia file = new InputMediaPhoto();
        file.setMedia((new ClassPathResource("images/no_photo.jpeg")).getInputStream(), "no_photo.jpeg");
        //TODO logic
        return file;
    }

    @SneakyThrows
    public InputFile getNoPhotoFile(){
        InputFile file = new InputFile();
        file.setMedia((new ClassPathResource("images/no_photo.jpeg")).getInputStream(), "no_photo.jpeg");
        //TODO logic
        return file;
    }

    @SneakyThrows
    private InputFile getFileByPath(String path) {
        Resource[] resources = this.resourcePatternResolver.getResources("classpath:" + path + "*");
        Optional<Resource> resource = Arrays.stream(resources)
                .filter(resource1 -> Objects.requireNonNull(resource1.getFilename()).contains("."))
                .findFirst();
        if (resource.isEmpty()) {
            return getNoPhotoFile();
        }
        return new InputFile(resource.get().getInputStream(), resource.get().getFilename());
    }

    @SneakyThrows
    private List<List<InputMedia>> getMediasByPath(String path) {
        if(!(new ClassPathResource(path).exists())) {
            throw new FileNotFoundException();
        }
        Resource[] resources = this.resourcePatternResolver.getResources("classpath:" + path + "big/*");
        List<List<InputMedia>> result = new ArrayList<>();
        
        List<InputMedia> tmpList = new ArrayList<>();
        for (Resource resource : resources) {
            if (tmpList.size() == 10) {
                result.add(tmpList);
                tmpList.clear();
            }
            InputMedia media = new InputMediaPhoto();
            try {
                media.setMedia(resource.getInputStream(), resource.getFilename());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            tmpList.add(media);
        }
        if (!tmpList.isEmpty()) {
            result.add(tmpList);
        }

        return result;
    }

    @SneakyThrows
    private InputMedia getMediaByPath(String path) {
        Resource[] resources = this.resourcePatternResolver.getResources("classpath:" + path + "*");
        Optional<Resource> resource = Arrays.stream(resources)
                .filter(resource1 -> Objects.requireNonNull(resource1.getFilename()).contains("."))
                .findFirst();
        if (resource.isEmpty()) {
            return getNoPhotoMedia();
        }

        // Load the image from the resource
        BufferedImage originalImage = ImageIO.read(resource.get().getInputStream());

        // Resize the image to the desired dimensions
        BufferedImage resizedImage = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        resizedImage.createGraphics().drawImage(originalImage, 0, 0, 400, 300, null);

        // Convert the resized image to bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);

        // Create InputMediaPhoto from the resized image
        InputMediaPhoto media = new InputMediaPhoto();
        media.setMedia(new ByteArrayInputStream(outputStream.toByteArray()), "photo.png");

        return media;
    }


/*    @SneakyThrows
    private InputMedia getMediaByPath(String path) {
        Resource[] resources = this.resourcePatternResolver.getResources("classpath:" + path + "*");
        Optional<Resource> resource = Arrays.stream(resources)
                .filter(resource1 -> Objects.requireNonNull(resource1.getFilename()).contains("."))
                .findFirst();
        if (resource.isEmpty()) {
            return getNoPhotoMedia();
        }
        InputMedia media = new InputMediaPhoto();
        media.setMedia(resource.get().getInputStream(), resource.get().getFilename());
        return media;
    }*/

    @SneakyThrows
    public List<List<InputMedia>> getHotelMedias(Hotel hotel) {
        return getMediasByPath(hotel.media);
    }

    @SneakyThrows
    public List<List<InputMedia>> getCustomTourMedias(CustomTour customTour) {return getMediasByPath(customTour.media);}

    @SneakyThrows
    public List<List<InputMedia>> getResortMedias(Resort resort) {
        return getMediasByPath(resort.media);
    }

    @SneakyThrows
    public InputMedia getActivityMedia() {
        InputMedia file = new InputMediaPhoto();
        file.setMedia((new ClassPathResource("images/00.jpg")).getInputStream(), "activity.png");
        return file;
    }

    @SneakyThrows
    public InputMedia getHotelMedia(Hotel hotel) {
        return getMediaByPath(hotel.media);
    }

    @SneakyThrows
    public InputMedia getResortMedia(Resort resort) {
        return getMediaByPath(resort.media);
    }

    @SneakyThrows
    public InputMedia getCustomTourMedia(CustomTour customTour) {
        if (customTour.media == null) {
            return getStartMedia();
        }
        return getMediaByPath(customTour.media);
    }

    @SneakyThrows
    public InputFile getHotelFile(Hotel hotel) {
        return getFileByPath(hotel.media);
    }

    @SneakyThrows
    public InputFile getResortFile(Resort resort) {
        return getFileByPath(resort.media);
    }

    @SneakyThrows
    public InputFile getCustomTourFile(CustomTour customTour) {
        if (customTour.media == null) {
            return getStartMessageMedia();
        }
        return getFileByPath(customTour.media);
    }

    @SneakyThrows
    public InputMedia getFeedbackMedia() {
        InputMedia file = new InputMediaPhoto();
        file.setMedia((new ClassPathResource("images/hotelType_2.png")).getInputStream(), "finish.png");
        return file;
    }

    @SneakyThrows
    public InputFile getStartMessageMedia() {
        InputFile file = new InputFile();
        file.setMedia(new ClassPathResource("images/kiprstart.jpg").getInputStream(), "kiprstart.jpg");
        return file;
    }

    @SneakyThrows
    public InputFile getTourChoosingMedia() {
        return getStartMessageMedia();
    }

    @SneakyThrows
    public InputFile getAuthFile(){
        return getStartMessageMedia();
    }

    @SneakyThrows
    public InputMedia getAuthMedia(){
        return getStartMedia();
    }

    @SneakyThrows
    public InputMedia getDefaultResortMedia() {
//        TODO: add default resort media
        return getStartMedia();
    }
    
    @SneakyThrows
    public InputMedia getTourConstructorMedia() {
//        TODO: add default constructor media
        return getStartMedia();
    }
}
