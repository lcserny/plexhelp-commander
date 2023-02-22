package net.cserny.download;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class DownloadedMediaService {

    @Inject
    DownloadedMediaRepository repository;

    // TODO: use in REST endpoint?
}
