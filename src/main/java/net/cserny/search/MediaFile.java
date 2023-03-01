package net.cserny.search;

import java.util.List;

public record MediaFile(String path, String name, List<String> videos) {
}
