package component;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class LogManager extends Thread {
    private static final String TAG = LogManager.class.getName();
    private static Semaphore mutex = null;
    private static Semaphore errorMutex = null;
    private static PrintWriter writer;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    private static final Object lockQueue = new Object();
    private static LinkedList<LogData> logQueue = new LinkedList<>();

    public static void write(String clientID, String content, Context mContext) {
        //放到queue處理
        File logFile = new File(GetFileLogPath(clientID, mContext));
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formatted = format1.format(cal.getTime());

        String msg = String.format("%s,%s", formatted, content);
        LogData data = new LogData();
        data.setClientID(clientID);
        data.setContent(msg);
        data.setLogPath(logFile);
        synchronized (lockQueue) {
            logQueue.add(data);
        }
    }

    public static void writeAll() {
        try {
            if (logQueue.size() > 0) {
                LinkedList<LogData> queue = new LinkedList<>();
                synchronized (lockQueue) {
                    queue.addAll(logQueue);
                    logQueue.clear();
                }

                while (queue.size() > 0) {
                    LogData data = queue.removeFirst();
                    write(data.getLogPath(), data.getContent());
                    Log.d(TAG, String.
                            format("LogSize=%02d, %s=%s,.", queue.size(), data.getClientID(), data.getContent()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write(File logFile, String content) {
        try {
            if (mutex == null) {
                mutex = new Semaphore(1);
            }
            mutex.acquire();

            logFile.getParentFile().mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            writer = new PrintWriter(fileOutputStream);
            writer.print(content + "\r\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create the log file (no stream):" + CheckSDCard());
        } finally {
            if (mutex != null) {
                mutex.release();
            }
        }
    }

//    public static void write(String clientID, String content, Context mContext) {
//        try {
//            if (mutex == null) {
//                mutex = new Semaphore(1);
//            }
//            mutex.acquire();
//
//            File logFile = new File(GetFileLogPath(clientID, mContext));
//            File bakFile = new File(GetBakLogPath(clientID, mContext));
//
//            logFile.getParentFile().mkdirs();
//            bakFile.getParentFile().mkdirs();
//
//            Calendar cal = Calendar.getInstance();
//            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String formatted = format1.format(cal.getTime());
//
//            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
//            writer = new PrintWriter(fileOutputStream);
//            writer.print(String.format("%s,%s\r\n", formatted, content));
//            writer.flush();
//            writer.close();
//
//            Log.d(TAG, clientID + " " + content);
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to create the log file (no stream):" + CheckSDCard());
//        } finally {
//            if (mutex != null) {
//                mutex.release();
//            }
//        }
//    }

    public static void write(Exception content, Context mContext) {
        try {
            if (errorMutex == null) {
                errorMutex = new Semaphore(1);
            }
            errorMutex.acquire();
            content.printStackTrace();

            File logFile = new File(GetFileLogPath("error", mContext));
            File bakFile = new File(GetBakLogPath("error", mContext));

            logFile.getParentFile().mkdirs();
            bakFile.getParentFile().mkdirs();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatted = format1.format(cal.getTime());

            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            writer = new PrintWriter(fileOutputStream);
            writer.print(String.format("%s, %s\r\n", formatted, content.toString()));
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create the log file (no stream):" + CheckSDCard());
        } finally {
            if (errorMutex != null) {
                errorMutex.release();
            }
        }
    }

    public static void clear(Context mContext) {
        File file = new File(GetFileFolder("Log", mContext));
        if (file.getAbsoluteFile().listFiles() != null) {
            for (File f : file.getAbsoluteFile().listFiles()) {
                if (f.isFile()) {
                    if (isOldLog(f, 14)) {
                        System.out.println("delete " + f.getName());
                        f.delete();
                        write("system", "delete " + f.getName(), null);
                    } else
                        System.out.println("delete(ignore) " + f.getName());
                }
            }
        }
    }

    private static boolean CheckSDCard() {
        boolean ret = false;
        try {
            ret = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static String GetFileFolder(String folder, Context context) {
        if (CheckSDCard() || context == null) {
            return String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), folder);
        } else {
            return GetInternalFileFolder(folder, context);
        }
    }

    private static String GetInternalFileFolder(String folder, Context context) {
        return String.format("%s/%s", context.getDir("Log", Context.MODE_PRIVATE).getAbsolutePath(), folder);
    }

    private static String GetFileLogPath(String clientID, Context context) {
        Calendar calendar = Calendar.getInstance();
        if (CheckSDCard()) {
            return String.format("%s/%s_%so.txt", GetFileFolder("Log", context), sdf.format(calendar.getTime()), clientID);
        } else {
            return String.format("%s/%s_%s.txt", GetFileFolder("Log", context), sdf.format(calendar.getTime()), clientID);
        }
    }

    private static String GetBakLogPath(String clientID, Context context) {
        Calendar calendar = Calendar.getInstance();
        if (CheckSDCard()) {
            return String.format("%s/%s_%so.txt", GetFileFolder("Bak", context), sdf.format(calendar.getTime()), clientID);
        } else {
            return String.format("%s/%s_%s.txt", GetFileFolder("Bak", context), sdf.format(calendar.getTime()), clientID);
        }
    }

    private static boolean isTodayLog(String fileName) {
        Calendar calendar = Calendar.getInstance();
        String today = sdf.format(calendar.getTime());

        if (fileName.contains(today)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isOldLog(File f, int limit) {
        try {
            String parse = f.getName().split("_")[0];
            Date beginDate = sdf.parse(parse);
            Date endDate = new Date();
            long day = (endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000);

            //不是我的檔, 刪!!!
            if (parse.length() != 8)
                return true;

            if (day >= limit) {
                return true;
            }
            System.out.println("day=" + day + ", " + sdf.format(beginDate) + ", " + f.getName() + ", " + parse);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                f.delete();
                write("system", "delete " + f.getName(), null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }
}
