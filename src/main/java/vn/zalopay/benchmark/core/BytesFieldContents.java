package vn.zalopay.benchmark.core;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.protobuf.ByteString;


public class BytesFieldContents {
        private final String path;
        private final long offset;
        private final int readLength;

        public BytesFieldContents(String path, long offset, int readLength) {
                this.path = path;
                this.offset = offset;
                this.readLength = readLength;
        }

        public String getPath() {
                return path;
        }

        public long getOffset() {
                return offset;
        }

        public int getReadLength() {
                return readLength;
        }

        public ByteString readBytes() throws IOException {
                // allocate array of correct length
                byte[] bytes = new byte[readLength];
                int bytesRead = 0;
                int bytesWanted = readLength;

                // open file, skip n bytes, call read -- while loop
                try(FileInputStream file = new FileInputStream(path)) {
                        while (bytesRead < bytesWanted) {
                                bytesRead += file.read (bytes, bytesRead, bytesWanted - bytesRead);
                        }
                }

                return ByteString.copyFrom(bytes);
        }
}
