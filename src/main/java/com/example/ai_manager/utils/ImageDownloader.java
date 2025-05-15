package com.example.ai_manager.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageDownloader {
    public static BufferedImage download(String url) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            return ImageIO.read(in);
        }
    }

}