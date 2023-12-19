import hudson.plugins.gradle.BaseGradleIntegrationTest
import hudson.plugins.gradle.BuildScanPublishedListener
import hudson.plugins.gradle.GradleConsoleAnnotator
import hudson.plugins.gradle.GradleOutcomeNote
import hudson.plugins.gradle.GradleTaskNote

import java.nio.charset.StandardCharsets

class GradleConsoleAnnotatorTest extends BaseGradleIntegrationTest {
    def 'run with normal output'()  {
        given:
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        BuildScanPublishedListener listener = new BuildScanPublishedListener() {
            @Override
            void onBuildScanPublished(String scanUrl) { }
        }

        when:
        new GradleConsoleAnnotator(out, StandardCharsets.UTF_8, true, listener).with {
            write(":task1\n".getBytes(StandardCharsets.UTF_8))
            write("    some message\n".getBytes(StandardCharsets.UTF_8))
            write("> Task :task2\n".getBytes(StandardCharsets.UTF_8))
            write("BUILD SUCCESSFUL\n".getBytes(StandardCharsets.UTF_8))
        }

        then:
        String[] lines = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n")

        lines[0] == "${new GradleTaskNote().encode()}:task1"
        lines[1] == "    some message"
        lines[2] == "${new GradleTaskNote().encode()}> Task :task2"
        lines[3] == "${new GradleOutcomeNote().encode()}BUILD SUCCESSFUL"
    }

    def 'run with timestamper output'()  {
        given:
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        BuildScanPublishedListener listener = new BuildScanPublishedListener() {
            @Override
            void onBuildScanPublished(String scanUrl) { }
        }

        when:
        new GradleConsoleAnnotator(out, StandardCharsets.UTF_8, true, listener).with {
            write("[2023-12-01T15:42:34.883Z] :task1\n".getBytes(StandardCharsets.UTF_8))
            write("[2023-12-01T15:42:35.201Z]     some message\n".getBytes(StandardCharsets.UTF_8))
            write("[2023-12-01T15:42:35.459Z] > Task :task2\n".getBytes(StandardCharsets.UTF_8))
            write("[2023-12-01T15:42:36.405Z] BUILD SUCCESSFUL\n".getBytes(StandardCharsets.UTF_8))
        }

        then:
        String[] lines = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n")

        lines[0] == "${new GradleTaskNote().encode()}[2023-12-01T15:42:34.883Z] :task1"
        lines[1] == "[2023-12-01T15:42:35.201Z]     some message"
        lines[2] == "${new GradleTaskNote().encode()}[2023-12-01T15:42:35.459Z] > Task :task2"
        lines[3] == "${new GradleOutcomeNote().encode()}[2023-12-01T15:42:36.405Z] BUILD SUCCESSFUL"
    }
}