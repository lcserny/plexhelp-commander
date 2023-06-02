package net.cserny.command;

public record CommandRequest(String name, Param[] params) {
}
