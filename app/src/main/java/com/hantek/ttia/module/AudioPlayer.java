package com.hantek.ttia.module;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.hantek.ttia.dl.DownloadService;
import com.hantek.ttia.module.roadutils.Station;

import java.util.ArrayList;
import java.util.List;

import component.LogManager;

public class AudioPlayer extends Thread implements MediaPlayer.OnCompletionListener {
    private static AudioPlayer ourInstance = new AudioPlayer();
    private boolean running = false;
    private Context mContext;

    //進出站撥放參數
    private Radius inRadiusStruct = null;
    private Radius outRadiusStruct = null;

    private boolean playing = false;// 是否正在播報
    private MediaPlayer mMediaPlayer = null;

    private List<SoundFile> playList = new ArrayList<>();
    private final Object playListObj = new Object();

    //客運歡迎詞
    private boolean useWelcome;
    private String welcomeFile;
    private String welcomeGender;
    private String welcomeLang;
    private boolean playWelcome;

    //路線發車歡迎詞
    private String roadWelcomeFile;
    private String roadWelcomeGender;
    private String roadWelcomeLang;

    // 終點站
    private boolean useStop;
    private String stopFile;
    private String stopGender;
    private String stopLang;
    private boolean playStop;

    public static AudioPlayer getInstance() {
        return ourInstance;
    }

    private AudioPlayer() {
        this.setName("AudioPlayer");

        inRadiusStruct = new Radius();
        inRadiusStruct.mp3first = 0;
        inRadiusStruct.mp3 = "912";
        inRadiusStruct.delay = 0;
        inRadiusStruct.distance = 50;
        inRadiusStruct.type = "in";

        outRadiusStruct = new Radius();
        outRadiusStruct.mp3first = 1;
        outRadiusStruct.mp3 = "910";
        outRadiusStruct.delay = 0;
        outRadiusStruct.distance = 50;
        outRadiusStruct.type = "out";
    }

    public void open(Context context) {
        mContext = context;
        running = true;
        ourInstance.start();
    }

    public void close() {
        running = false;
        try {
            ourInstance.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            ourInstance.interrupt();
        }
    }

    public void setOutRadius(Radius radius) {
        outRadiusStruct = radius;
    }

    public void setInRadius(Radius radius) {
        inRadiusStruct = radius;
    }

    public void play(Station station, String gender, String type, String eventType) {
        // 讓另一隻Thread停止撥報
        if (playing) {
            playing = false;
            LogManager.write("STA", String.format("Audio,%s interrupt by %s,.", station.zhName, eventType), null);
        }

        // 清除等候撥放的語音
        if (playList.size() > 0) {
            synchronized (playListObj) {
                if (playList.size() > 0) {
                    playList.clear();
                }
            }
        }

        try {
            //強制停止語音
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                mMediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 載入撥放參數
            String[] tmp = type.split("/");// => c/e/t
            boolean mp3First;
            Radius radius = null;
            if (eventType.equalsIgnoreCase("out")) {
                radius = this.outRadiusStruct;
            } else if (eventType.equalsIgnoreCase("in")) {
                radius = this.inRadiusStruct;
            }
            if (radius == null) {
                return;
            }

            //放入背景撥放
            mp3First = (radius.mp3first == 1);
            List<SoundFile> tmpPlayList = new ArrayList<>();
            for (String s : tmp) {
                SoundFile sound = new SoundFile();
                sound.station = station;
                sound.mp3First = mp3First;
                sound.gender = gender;
                sound.fileName = radius.mp3;
                sound.type = s;
                sound.delay = radius.delay;
                tmpPlayList.add(sound);
            }

            // 一次放入
            synchronized (playListObj) {
                this.playList.clear();
                this.playList.addAll(tmpPlayList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10);

                if (playList.size() > 0) {
                    SoundFile file = null;
                    try {
                        synchronized (playListObj) {
                            if (playList.size() > 0) {
                                file = playList.remove(0);
                            }
                        }
                        playing = true;
                        // 18版本 英文固定念XXX + 站名
                        if (file.type.equalsIgnoreCase("e")) {
                            playAppendVoice(file.gender, file.type, file.fileName);
                            playStation(file.type, file.station, file.gender);
                        } else {
                            if (file.mp3First) {
                                playAppendVoice(file.gender, file.type, file.fileName);
                                playStation(file.type, file.station, file.gender);
                            } else {
                                playStation(file.type, file.station, file.gender);
                                playAppendVoice(file.gender, file.type, file.fileName);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        playing = false;
                    }
                } else if (playWelcome) {
                    try {
                        playing = true;
                        playWelcomeAudio();
                        playWelcome = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        playing = false;
                    }
                } else if (playStop) {
                    try {
                        playing = true;
                        String[] tmp = stopLang.split("/");
                        String[] file = stopFile.split("/");
                        for (String s : tmp) {
                            for (String f : file) {
                                playAppendVoice(stopGender, s, f);
                            }
                        }
                        playStop = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        playing = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean waiting(int duration) throws InterruptedException {
        while (duration > 0) {
            duration -= 100;
            if (complete) {
                Log.d("Audio", "waiting Complete!!!");
                return true;
            }

            // 如果還在播, 等候語音播完
            if (playing)
                Thread.sleep(100);
            else
                return false;// 被中斷了
        }

        return true;
    }

    private void playStation(String s, Station station, String gender) throws InterruptedException {
        String folder = station.audioID.substring(0, 1);
        String fileName = station.audioID.substring(1, 4);

        String path = DownloadService.getFilePath(folder, fileName, audioType(gender, s));
        if (!playing) {
            LogManager.write("STA", "Audio," + station.zhName + "," + path + ", interrupt,.", null);
            return;
        }

        mMediaPlayer = Utility.soundPlay(mContext, path + ".mp3");
        if (mMediaPlayer != null) {
            int duration = mMediaPlayer.getDuration();
            complete = false;
            //http://stackoverflow.com/questions/11145652/mediaplayer-getduration-returning-wrong-duration
            // 檔案問題影響撥放, 放入平均秒數
            if (duration < 1500)
                duration = 2001;
            mMediaPlayer.setOnCompletionListener(this);
            LogManager.write("STA", "Audio," + station.zhName + "," + path + ",." + duration, null);
            if (waiting(duration)) {
                // 正常播完
            } else {
                LogManager.write("STA", "Audio," + station.zhName + "," + path + ", interrupted,.", null);
            }
        } else {
            LogManager.write("STA", "Audio," + station.zhName + "," + path + ", fail,.", null);
        }
    }

    private void playAppendVoice(String gender, String type, String voice) throws InterruptedException {
        String path = DownloadService.getFilePath(audioType(gender, type), voice, "9");
        if (!playing) {
            LogManager.write("STA", "Audio,append," + path + ", interrupt,.", null);
            return;
        }

        mMediaPlayer = Utility.soundPlay(mContext, path + ".mp3");
        if (mMediaPlayer != null) {
            int duration = mMediaPlayer.getDuration();
            complete = false;
            LogManager.write("STA", "Audio,append," + path + ",." + duration, null);
            if (waiting(duration)) {
                // 正常播完
            } else {
                LogManager.write("STA", "Audio,append," + path + ", interrupted,.", null);
            }
        } else {
            LogManager.write("STA", "Audio,append," + path + ", fail,.", null);
        }
    }

    private void playRoadVoice(String langType, String audioID, String gender) throws InterruptedException {
        String folder = audioID.substring(0, 1);
        String fileName = audioID.substring(1, 4);

        String path = DownloadService.getFilePath(folder, fileName, audioType(gender, langType));
        if (!playing) {
            LogManager.write("STA", "Audio,Welcome," + path + ", interrupt,.", null);
            return;
        }

        mMediaPlayer = Utility.soundPlay(mContext, path + ".mp3");
        if (mMediaPlayer != null) {
            int duration = mMediaPlayer.getDuration();
            complete = false;
            LogManager.write("STA", "Audio,Welcome," + path + ",." + duration, null);
            if (waiting(duration)) {
                // 正常播完
            } else {
                LogManager.write("STA", "Audio,Welcome," + path + ", interrupted,.", null);
            }
        } else {
            LogManager.write("STA", "Audio,Welcome," + path + ",fail,.", null);
        }
    }

    private String audioType(String gender, String type) {
        // 播音性別 男:m, 女:f
        // 播音種類 國(c)/台(t)/客(h)/英(e)
        if (gender.equalsIgnoreCase("m")) {
            if (type.equalsIgnoreCase("t"))
                return "2";
            else if (type.equalsIgnoreCase("h"))
                return "3";
            else if (type.equalsIgnoreCase("e"))
                return "1";
            else
                return "0";
        } else {
            if (type.equalsIgnoreCase("t"))
                return "6";
            else if (type.equalsIgnoreCase("h"))
                return "7";
            else if (type.equalsIgnoreCase("e"))
                return "5";
            else
                return "4";
        }
    }

    public void setWelcome(boolean useWelcome, String content, String welcomeGender, String lang) {
        this.useWelcome = useWelcome;
        this.welcomeFile = content;
        this.welcomeGender = welcomeGender;
        this.welcomeLang = lang;

        this.roadWelcomeLang = welcomeLang;
        this.roadWelcomeGender = welcomeGender;
    }

    public void setStop(boolean useWelcome, String content, String welcomeGender, String lang) {
        this.useStop = useWelcome;
        this.stopFile = content;
        this.stopGender = welcomeGender;
        this.stopLang = lang;
    }

    public void setRoadWelcome(String audioFile, String audioLang, String audioType) {
        this.roadWelcomeFile = audioFile;
//        this.roadWelcomeLang = audioLang;
//        this.roadWelcomeGender = audioType;
    }

    public void playWelcome() {
        playWelcome = true;
    }

    /**
     * 終點站, 未測
     */
    public void playStop() {
        if (!useStop)
            return;

        playStop = true;
    }

    private void playWelcomeAudio() throws InterruptedException {
        // 指定路線歡迎詞或客運歡迎詞
        if (this.roadWelcomeFile != null && this.roadWelcomeFile.trim().length() >= 4) {
            playRoadVoice(roadWelcomeLang.trim(), roadWelcomeFile.trim(), roadWelcomeGender.trim());
        } else {
            if (!useWelcome)
                return;
            String[] tmpLang = welcomeLang.split("/");
            String[] tmpFile = welcomeFile.split("/");
            for (String lang : tmpLang) {
                for (String file : tmpFile) {
                    playAppendVoice(welcomeGender, lang, file);
                }
            }
        }
    }

    boolean complete = false;

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("Audio", "onCompletion!!!");
        complete = true;
    }

    public void interruptPlay() {
        if (playing) {
            playing = false;
            LogManager.write("STA", "Audio, interrupt,.", null);
        }

        // 清除等候撥放的語音
        if (playList.size() > 0) {
            synchronized (playListObj) {
                if (playList.size() > 0) {
                    playList.clear();
                }
            }
        }

        try {
            //強制停止語音
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
//                mMediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SoundFile {
        Station station;

        /**
         * 語助詞優先
         */
        boolean mp3First;

        /**
         * 性別
         */
        String gender;

        /**
         * 國c 英e 台t
         */
        String type;

        /**
         * 語助詞檔名
         */
        String fileName;

        /**
         * 撥完延遲
         */
        int delay;
    }
}
