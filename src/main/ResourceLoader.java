package main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class ResourceLoader {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    // 异步加载图片
    public static Future<BufferedImage> loadImageAsync(String path) {
        return executor.submit(() -> ImageIO.read(ResourceLoader.class.getResourceAsStream(path)));
    }

    // 异步加载音效
    public static Future<Clip> loadSoundAsync(URL url) {
        return executor.submit(() -> {
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        });
    }

    public static void shutdown() {
        executor.shutdown();
    }
} 