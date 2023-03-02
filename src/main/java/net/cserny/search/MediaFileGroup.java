package net.cserny.search;

import java.util.List;

public record MediaFileGroup(String path, String name, List<String> videos) {
}
