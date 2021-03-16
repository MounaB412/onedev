package io.onedev.server.git.hookcallback;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;

public class Output {

	private ServletOutputStream stream;
	
	public Output(ServletOutputStream stream) {
		this.stream = stream;
	}
	
    public void writeLine(String line) {
    	if (line.matches("\\*+"))
    		line = line + " ";
        try {
			stream.println(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void writeLine() {
        try {
			stream.println(" ");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public void markError() {
        writeLine("ERROR");
    }

	public void error(@Nullable String refName, List<String> messages) {
		markError();
		writeLine();
		writeLine("*******************************************************");
		writeLine("*");
		if (refName != null)
			writeLine("*  ERROR PUSHING REF: " + refName);
		else
			writeLine("*  ERROR PUSHING");
		writeLine("-------------------------------------------------------");
		for (String message : messages)
			writeLine("*  " + message);
		writeLine("*");
		writeLine("*******************************************************");
		writeLine();
	}

}
