package net.cserny.api;

import net.cserny.generated.DownloadedMediaData;

import java.util.List;

public interface AutomoveDownloadedManipulator {

    List<DownloadedMediaData> findForAutoMove(int limit);
    void saveAll(List<DownloadedMediaData> medias);
}
