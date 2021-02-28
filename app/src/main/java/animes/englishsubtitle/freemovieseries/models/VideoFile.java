package animes.englishsubtitle.freemovieseries.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoFile implements Serializable {

    private String fileName;
    private long lastModified;
    private long totalSpace;
    private String path;
    private String fileExtension;
    private String defaultSubPath;
    private List<String> subList = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public List<String> getSubList() {
        return subList;
    }

    public void setSubList(List<String> subList) {
        this.subList = subList;
    }
    public void setDefaultSubPath(String defaultSubPath) {
        this.defaultSubPath = defaultSubPath;
    }

    public String getDefaultSubPath() {
        return defaultSubPath.isEmpty()?subList.get(0):defaultSubPath;
    }
}
