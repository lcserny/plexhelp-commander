package net.cserny.rename;

import java.util.List;

public record MediaDescription(String posterUrl, String title, String description, List<String> cast) {
}
