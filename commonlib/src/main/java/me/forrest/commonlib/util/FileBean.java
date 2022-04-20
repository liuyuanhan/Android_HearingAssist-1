package me.forrest.commonlib.util;

public class FileBean {
    public final static int IMG_TYPE   =   1;
    public final static int VIDEO_TYPE = 2;
    private String url;
    private String thumbUrl;        //缩略图url
    private int fileType;           //文件类型
    private String fileName;        //文件名
    private String filePath;        //文件绝对路径
    private int duration;           //视频长度(S)
    private boolean selected;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getThumbUrl() {
        return  this.thumbUrl;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getFileType() {
        return this.fileType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isSeletected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
