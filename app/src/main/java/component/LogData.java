package component;

import java.io.File;

/**
 * Created by HantekPC on 2016/5/12.
 */
public class LogData {
    private String clientID;
    private String content;
    private File logPath;

    public LogData() {
        content = "";
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLogPath(File logPath) {
        this.logPath = logPath;
    }

    public String getClientID() {
        return clientID;
    }

    public String getContent() {
        return content;
    }

    public File getLogPath() {
        return logPath;
    }
}
