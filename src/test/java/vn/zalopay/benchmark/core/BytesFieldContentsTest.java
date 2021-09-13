package vn.zalopay.benchmark.core;

import org.testng.Assert;
import com.google.protobuf.ByteString;

import org.testng.annotations.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BytesFieldContentsTest {
    private static final Path CAT_PIC = Paths.get(System.getProperty("user.dir"), "src/test/resources/cat.jpeg");

    @Test
    public void testReadBytes() {
        long offset = 0;
        int readLength = 10;
        BytesFieldContents bfc = new BytesFieldContents(CAT_PIC.toString(), offset, readLength);
        ByteString read = bfc.readBytes();
        Assert.assertEquals(read, ByteString.copyFrom(new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0, (byte) 0x00, (byte) 0x10, (byte) 0x4a, (byte) 0x46, (byte) 0x49, (byte) 0x46}));
    }
}
