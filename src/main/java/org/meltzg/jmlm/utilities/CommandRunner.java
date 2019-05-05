package org.meltzg.jmlm.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
public class CommandRunner {
    public static CommandResults runCommand(List<String> commandArgs) throws IOException {
        var builder = new ProcessBuilder().redirectErrorStream(true).command(commandArgs);

        var process = builder.start();
        var exitVal = 0;
        try {
            exitVal = process.waitFor();
        } catch (InterruptedException e) {
            log.error("Command interrupted: {}", commandArgs);
            log.error("Exception", e);
            exitVal = 1;
        }

        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append('\n');
            log.info(line);
        }

        return new CommandResults(output.toString(), exitVal);
    }

    @Data
    @AllArgsConstructor
    public static class CommandResults {
        String output;
        int exitValue;
    }
}
