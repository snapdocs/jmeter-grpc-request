package vn.zalopay.benchmark.core;

import com.google.protobuf.ByteString;

public class BytesFieldContents {
        private final String path;
        private final long offset;
        private final long readLength;

        public BytesFieldContents(String path, long offset, long readLength) {
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

        public long getReadLength() {
                return readLength;
        }

        public ByteString readBytes() {
                return ByteString.EMPTY;
        }
}
