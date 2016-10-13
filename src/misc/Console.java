package misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by User on 13/10/2016.
 */
public class Console extends OutputStream {

    private static Console capture = new Console();
    public static PrintStream out = System.out;
    public static PrintStream err = System.err;

    public static void captureOutput() {
        System.setOut(new PrintStream(capture));
        //System.setErr(new PrintStream(capture));
    }

    public void write(int b) throws IOException {}
}
