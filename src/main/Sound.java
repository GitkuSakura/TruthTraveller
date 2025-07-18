package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    Clip clip;
    URL[] soundURL = new URL[30];
    public Sound() {
        soundURL[0] = getClass().getResource("/res/sound/CatEarsAndTail.wav");
        soundURL[1] = getClass().getResource("/res/sound/coin.wav");
        soundURL[2] = getClass().getResource("/res/sound/coin.wav"); // 钥匙音效使用coin音效
        soundURL[3] = getClass().getResource("/res/sound/door.wav");
        soundURL[4] = getClass().getResource("/res/sound/coin.wav");
        soundURL[5] = getClass().getResource("/res/sound/sword_swing.wav"); // 挥剑音效
        soundURL[6] = getClass().getResource("/res/sound/get_hurt.wav"); // 玩家受伤音效
        soundURL[7] = getClass().getResource("/res/sound/get_hurt.wav"); // 怪物受伤音效（暂时使用coin音效）
    }
    public void setFile(int i){
        try{
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        }catch (Exception e){
            e.printStackTrace();

        }

    }
    public void play(){
        if (clip != null) {
            clip.start();
        }
    }
    public void loop(){
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    public void stop(){
        if (clip != null) {
            clip.stop();
        }
    }

}
