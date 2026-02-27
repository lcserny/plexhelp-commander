package net.cserny.api.dto;

public record CommandResult<R>(boolean success, boolean delayed, R result) {}
