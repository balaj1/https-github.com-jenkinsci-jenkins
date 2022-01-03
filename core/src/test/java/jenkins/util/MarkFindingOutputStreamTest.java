package jenkins.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class MarkFindingOutputStreamTest {

    String mark = MarkFindingOutputStream.MARK;
    String markHead = mark.substring(0, 5);
    String markTail = mark.substring(5);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MarkCountingOutputStream m = new MarkCountingOutputStream(baos);

    @Test
    public void findTwice() throws IOException {
        write("foo" + mark + "bar" + mark);
        assertCount(2);
        assertOutput("foobar");
    }

    @Test
    public void partialMatchTurnsOutToBeWrongIn2ndWrite() throws IOException {
        write("bar" + markHead);
        assertOutput("bar"); // at this point we should just see 'bar'

        write("foo"); // this needs to write what was held off during the first write
        assertCount(0);
        assertOutput("bar" + markHead + "foo");
    }

    /**
     * If a stream closes without completing a match, the partial match should be sent to the output.
     */
    @Test
    public void closeInTheMiddle() throws IOException {
        write("foo" + markHead);
        m.close();
        assertCount(0);
        assertOutput("foo" + markHead);
    }

    @Test
    public void oneByOne() throws IOException {
        m.write('1');
        writeOneByOne(mark);
        m.write('2');
        assertCount(1);
        assertOutput("12");
    }

    @Test
    public void writeOneHoldOff() throws IOException {
        writeOneByOne(markHead);
        assertOutput("");
        writeOneByOne("x");
        assertOutput(markHead + "x");
        assertCount(0);
    }

    private void assertOutput(String s) throws IOException {
        assertEquals(s, baos.toString("UTF-8"));
    }

    private void assertCount(int n) {
        assertEquals(n, m.count);
    }

    private void write(String s) throws IOException {
        m.write(s.getBytes(StandardCharsets.UTF_8));
    }

    private void writeOneByOne(String s) throws IOException {
        for (int i = 0; i < s.length(); i++)
            m.write(s.charAt(i));
    }

    static class MarkCountingOutputStream extends MarkFindingOutputStream {
        int count = 0;

        MarkCountingOutputStream(OutputStream base) {
            super(base);
        }

        @Override
        protected void onMarkFound() {
            count++;
        }
    }
}
