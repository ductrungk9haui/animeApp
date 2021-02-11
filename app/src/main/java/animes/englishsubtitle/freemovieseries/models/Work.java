package animes.englishsubtitle.freemovieseries.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Work {
    private int id;
    private String workId;
    private int downloadId = 0;
    private List<Integer> downloadSubIdList = new ArrayList<>();
    private String fileName;
    private String totalSize;
    private String downloadSize;
    private String downloadStatus = "";
    private String url;
    private String appCloseStatus;
    private String dir;
    private String subListJson;
    private String path;
    public long currentBytes = 0;
    public long totalBytes = 0;
    List<SubtitleModel> listSubs = new ArrayList<>();

    public String getAppCloseStatus() {
        return appCloseStatus;
    }

    public void setAppCloseStatus(String appCloseStatus) {
        this.appCloseStatus = appCloseStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    public String getDir() {
        return dir;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(String downloadSize) {
        this.downloadSize = downloadSize;
    }

    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    @Override
    public String toString() {
        return "Work{" +
                "id=" + id +
                ", workId='" + workId + '\'' +
                ", downloadId=" + downloadId +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public String getSubListJson() {
        return subListJson;
    }

    public void setSubList(List<SubtitleModel> subLists) {
        Gson gson = new Gson();
        listSubs = subLists;
        this.subListJson = gson.toJson(subLists);
    }
    public void setSubListJon(String jsonSubtitles) {
        subListJson = jsonSubtitles;
        Gson gson = new Gson();
        Type type = new TypeToken<List<SubtitleModel>>() {
        }.getType();
        this.listSubs = gson.fromJson(jsonSubtitles, type);
    }

    public List<SubtitleModel> getListSubs() {
        return listSubs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Integer> getDownloadSubIdList() {
        return downloadSubIdList;
    }

    public void setDownloadSubIdList(List<Integer> downloadSubIdList) {
        this.downloadSubIdList = downloadSubIdList;
    }

    public void addDownloadSubId(int downloadSubId) {
        downloadSubIdList.add(downloadSubId);
    }
}
