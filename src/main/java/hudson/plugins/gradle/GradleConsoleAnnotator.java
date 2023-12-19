package hudson.plugins.gradle;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author ikikko
 * @see <a href="https://github.com/jenkinsci/ant-plugin/blob/master/src/main/java/hudson/tasks/_ant/AntConsoleAnnotator.java">AntConsoleAnnotator</a>
 */
public final class GradleConsoleAnnotator extends AbstractGradleLogProcessor {
    private final boolean annotateGradleOutput;
    private final BuildScanLogScanner buildScanLogScanner;

    public GradleConsoleAnnotator(OutputStream out,
                                  Charset charset,
                                  boolean annotateGradleOutput,
                                  BuildScanPublishedListener buildScanListener) {
        super(out, charset);
        this.annotateGradleOutput = annotateGradleOutput;
        this.buildScanLogScanner = new BuildScanLogScanner(buildScanListener);
    }

    @Override
    protected void processLogLine(String line) throws IOException {
        // TODO: do we need to trim EOL?
        line = trimEOL(line);
        line = trimTimestamp(line);

        if (annotateGradleOutput) {
            if (line.startsWith(":") || line.startsWith("> Task :")) { // put the annotation
                new GradleTaskNote().encodeTo(out);
            }

            if (line.startsWith("BUILD SUCCESSFUL") || line.startsWith("BUILD FAILED")) {
                new GradleOutcomeNote().encodeTo(out);
            }
        }

        buildScanLogScanner.scanLine(line);
    }

    // Removes timestamps injected by the timestamper plugin
    private String trimTimestamp(String line) {
        // Quick way to check if the line starts with a timestamp without resorting to an expensive regex
        if (line.length() > 27 && line.charAt(0) == '[' && "Z] ".equals(line.substring(24, 27))) {
            return line.substring(27);
        }

        return line;
    }
}
