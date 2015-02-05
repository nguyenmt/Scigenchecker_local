package scigen_checker_v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class commandexecutor {
	public static String execute(String command){
        StringBuilder sb = new StringBuilder();
        String[] commands = new String[]{"/bin/sh","-c", command};
        try {
            Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new 
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                    InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "done";
    }
}
