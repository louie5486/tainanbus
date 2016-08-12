package component;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hantek.ttia.module.Utility;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Calendar;

public class LogUploader extends Thread {
    private static final String TAG = LogUploader.class.getName();
    private static LogUploader ourInstance = new LogUploader();
    private Context mContext;
    private boolean running = false;

    private LogInterface interfaces;
    private boolean uploadProcess = false;
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private String fileName = "";
    private String imei = "";

    private int failCounter;
    private Calendar lastUpdate = Calendar.getInstance();

    public static LogUploader getInstance() {
        return ourInstance;
    }

    private LogUploader() {
        this.setName("LogUploader");
    }

    public void setInterface(LogInterface interfaces) {
        if (this.interfaces == null)
            this.interfaces = interfaces;
    }

    public void open(Context context) {
        if (running)
            return;

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

    public String setUploadFile(String imei, int year, int month, int day, String fileName) {
        if (this.uploadProcess) // && Utility.dateDiffNow(this.lastUpdate) < 0)
            return "";

        this.failCounter = 0;
        this.lastUpdate.add(Calendar.MINUTE, 10);
        this.imei = imei;
        this.year = year;
        this.month = month;
        this.day = day;
        this.fileName = fileName;
        StringBuilder sb = new StringBuilder();

        int fileCount = 0;
        if (this.fileName.trim().length() == 0) {
            for (String fName : getLogPath()) {
                String filterName = String.format("%d%02d%02d_", this.year, this.month, this.day);
                //過濾日期
                if (fName.contains(filterName)) {
                    fileCount++;
                    sb.append(fName.replace(filterName, "").replace(".txt", "")).append("^");
                }
            }

            fileName = sb.toString().replaceAll("\\^+$", "");
        } else {
            String[] requestFile = this.fileName.split("\\^");
            for (String tmpFName : requestFile) {
                boolean result = LogUploader.getInstance().checkFileIsExists(this.year, this.month, this.day, tmpFName);
                if (result)
                    fileCount++;
            }
        }

        return fileCount + "^" + fileName;
    }

    public boolean checkFileIsExists(int year, int month, int day, String fileName) {
        try {
            String dirPath = getLogFolder();
            String fullPath = String.format("%s/%d%02d%02d_%s.txt", dirPath, year, month, day, fileName);
            File f = new File(fullPath);
            return f.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public void reset() {
        //資料清空
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.fileName = "";
        this.uploadProcess = false;
        this.lastUpdate.add(Calendar.MINUTE, -10);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(100);
                if (!this.uploadProcess && this.year > 0 && this.month > 0 && this.day > 0) {
                    this.uploadProcess = true;
                    upload();
                }

                LogManager.writeAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (interfaces != null) {
                interfaces.sendUploadCompleted("", 0, 0, 0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upload() {
        // hantek ftp
        final String FTP_HOST = "ftp.hantek.com.tw";// 59.124.205.59
        // hantek test ftp
//        final String FTP_HOST = "59.124.205.58";// 59.124.205.59

        final String FTP_USER = "bus"; // bus
        final String FTP_PASS = "24638389"; // 24638389

        //檔案名稱
        try {
            FTPClient ftpClient = new FTPClient();
            //連接FTP
            ftpClient.connect(InetAddress.getByName(FTP_HOST));
//            ftpClient.connect(InetAddress.getByName(FTP_HOST), 18021);

            //登入FTP
            ftpClient.login(FTP_USER, FTP_PASS);

            final String BUS = "/bus";
            //轉到指定上傳目錄
            ftpClient.changeWorkingDirectory(BUS);

            //創建server端資料夾
            ftpClient.makeDirectory(BUS + "/" + imei);

            //設定檔案類型
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //設定傳輸模式為被動
            ftpClient.enterLocalPassiveMode();

            Log.d(TAG, "getControlKeepAliveReplyTimeout:" + ftpClient.getControlKeepAliveReplyTimeout());
            Log.d(TAG, "getControlKeepAliveTimeout:" + ftpClient.getControlKeepAliveTimeout());
            ftpClient.setConnectTimeout(10000);
            Log.d(TAG, "getConnectTimeout:" + ftpClient.getConnectTimeout());
            ftpClient.setDefaultTimeout(10000);
            Log.d(TAG, "getDefaultTimeout:" + ftpClient.getDefaultTimeout());
            ftpClient.setSoTimeout(10000);
            Log.d(TAG, "getSoTimeout:" + ftpClient.getSoTimeout());

            StringBuilder sb = new StringBuilder();
            String[] requestFile = this.fileName.split("\\^");
            Log.d(TAG, "RequestFile:" + this.fileName);
            for (String fName : getLogPath()) {
                try {
                    String filterName = String.format("%d%02d%02d_", this.year, this.month, this.day);
                    //過濾日期
                    if (!fName.contains(filterName))
                        continue;

                    //過濾檔案
                    if (!this.fileName.equalsIgnoreCase("")) {
                        boolean isRequestFile = false;
                        for (String tmpUploadFileName : requestFile) {
                            if (fName.replace(filterName, "").replace(".txt", "").equalsIgnoreCase(tmpUploadFileName)) { // 20160218_filename
                                isRequestFile = true;
                                break;
                            }
                        }
                        if (!isRequestFile)
                            continue;
                    }

                    Log.d(TAG, fName);
                    //開啟資料流
                    BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(getLogFolder() + "/" + fName));
//                    String filename2 = fName.substring(fName.lastIndexOf("/"), fName.length()).toLowerCase();
                    //上傳檔案
                    ftpClient.storeFile(BUS + "/" + imei + "/" + fName, buffIn);
                    buffIn.close();
                    Log.d(TAG, "upload success~" + BUS + "/" + imei + "/" + fName);
                } catch (Exception e) {
                    this.failCounter += 1;
                    e.printStackTrace();
                    sb.append(fName).append("^");
                    Log.d(TAG, "upload fail~" + BUS + "/" + imei + "/" + fName);
                }
            }

            Log.d(TAG, "upload Complete~ Fail:" + failCounter);
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 上傳結束, 回傳失敗結果
            if (interfaces != null) {
                interfaces.sendUploadCompleted(sb.toString() + failCounter, failCounter == 0 ? 1 : 0, this.year, this.month, this.day);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error:" + e + ".");
        } finally {
            // 上傳結束
            reset();
        }
    }

    private String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private String getLogFolder() {
        return String.format("%s/%s", getExternalPath(), "Log");
    }

    private String[] getLogPath() {
        String dirPath = getLogFolder();
        File dirFile = new File(dirPath);
        return dirFile.getAbsoluteFile().list();
    }


}
