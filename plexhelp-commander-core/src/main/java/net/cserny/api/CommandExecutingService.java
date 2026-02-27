package net.cserny.api;


import net.cserny.api.dto.CommandResult;

import java.util.Optional;

public interface CommandExecutingService {

    <T> Optional<CommandResult<T>> execute(String name, String[] params);
}
