package tvseries.koreandramaengsub.freemovieapp.models.single_details_tv;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.models.SubtitleModel;

public class DownloadedFilm {
    long id;
    String subListJson;
    String pathVideo;
    List<String> subList = new ArrayList<>();

    public List<String> getSubList() {
        return subList;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubListJon() {
        Gson gson = new Gson();
        subListJson =  gson.toJson(subList);
        return subListJson;
    }

    public void setSubList(String jsonSubtitles) {
        subListJson = jsonSubtitles;
        Gson gson = new Gson();
        Type type = new TypeToken<List<SubtitleModel>>() {
        }.getType();
        this.subList = gson.fromJson(jsonSubtitles, type);
    }


    public void addPathSub(String pathSub){
        subList.add(pathSub);
    }

    public String getPathVideo() {
        return pathVideo;
    }

    public void setPathVideo(String pathVideo) {
        this.pathVideo = pathVideo;
    }
}
