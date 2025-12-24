package net.cserny;

import java.util.Arrays;
import net.cserny.command.SshExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommanderApplication {

    public static void main(String[] args) throws Exception {
//        SpringApplication.run(CommanderApplication.class, args);

        System.out.println(Arrays.toString(args));
        SshExecutor executor = new SshExecutor();
        SshExecutor.SshResponse response = executor.execute(String.join(" ", args));
	    System.out.println(response);
    }
}
