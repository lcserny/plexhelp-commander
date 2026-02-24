package net.cserny.api;

public interface NameNormalizer {

    NameYear normalize(String name);

    record NameYear(String name, Integer year) {
        public String formatted() {
            return name() + (year != null ? " (" + year + ")" : "");
        }
    }
}
