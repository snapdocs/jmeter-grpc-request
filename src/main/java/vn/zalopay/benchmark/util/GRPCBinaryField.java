package vn.zalopay.benchmark.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Class representing a file parameter for http upload.
 * Consists of a http parameter name/file path pair with (optional) mimetype.
 *
 * Also provides temporary storage for the headers which are sent with files.
 *
 */
public class GRPCBinaryField extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 241L;

    /** Name used to store the file's path. */
    private static final String FIELD_PATH = "BinaryField.field_path";

    /** Name used to store the file's paramname. */
    private static final String FILE_PATH = "BinaryField.file_path";

    /** Name used to store the file's mimetype. */
    private static final String OFFSET = "BinaryField.offset";

    /** Name used to store the file's mimetype. */
    private static final String READ_LENGTH = "BinaryField.read_length";

    /**
     * Constructor for an empty GRPCBinaryField object
     */
    public GRPCBinaryField() {
    }

    /**
     * Constructor for the GRPCBinaryField object with given path.
     *
     * @param fieldPath path to the file to use
     * @param filePath path to the file to use
     * @throws IllegalArgumentException if <code>path</code> is <code>null</code>
     */
    public GRPCBinaryField(String fieldPath, String filePath) {
        this(fieldPath, filePath, 0, 0);
    }

    /**
     * Constructor for the GRPCBinaryField object with full information.
     *
     * @param fieldPath
     *            path of the file to use
     * @param filePath
     *            name of the http parameter to use for the file
     * @param offset
     *            mimetype of the file
     * @param readLength
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public GRPCBinaryField(String fieldPath, String filePath, long offset, long readLength) {
        if (fieldPath == null || filePath == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setFieldPath(fieldPath);
        setFilePath(filePath);
        setOffset(offset);
        setReadLength(readLength);
    }

    /**
     * Constructor for the GRPCBinaryField object with full information,
     * using existing properties
     *
     * @param fieldPath
     *            path of the file to use
     * @param filePath
     *            name of the http parameter to use for the file
     * @param offset
     *            mimetype of the file
     * @param readLength
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public GRPCBinaryField(JMeterProperty fieldPath, JMeterProperty filePath, JMeterProperty offset, JMeterProperty readLength) {
        if (fieldPath == null || filePath == null || offset == null || readLength == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setProperty(FIELD_PATH, fieldPath);
        setProperty(FILE_PATH, filePath);
        setProperty(OFFSET, offset);
        setProperty(READ_LENGTH, readLength);
    }

    private void setProperty(String name, JMeterProperty prop) {
        JMeterProperty jmp = prop.clone();
        jmp.setName(name);
        setProperty(jmp);
    }

    /**
     * Copy Constructor.
     *
     * @param file
     *            {@link GRPCBinaryField} to get information about the path, http
     *            parameter name and mimetype of the file
     * @throws IllegalArgumentException
     *             if any of those retrieved information is <code>null</code>
     */
    public GRPCBinaryField(GRPCBinaryField field) {
        this(field.getFieldPath(), field.getFilePath(), field.getOffset(), field.getReadLength());
    }

    /**
     * Set the proto field path for the bytes field.
     *
     * @param newFieldPath the new field path
     */
    public void setFieldPath(String newFieldPath) {
        setProperty(new StringProperty(FIELD_PATH, newFieldPath));
    }

    /**
     * Get the proto field path for the bytes field.
     *
     * @return the field path
     */
    public String getFieldPath() {
        return getPropertyAsString(FIELD_PATH);
    }

    /**
     * Set the mimetype of the File.
     *
     * @param newMimeType the new mimetype
     */
    public void setFilePath(String newFilePath) {
        setProperty(new StringProperty(FILE_PATH, newFilePath));
    }

    /**
     * Get the mimetype of the File.
     *
     * @return the http parameter mimetype
     */
    public String getFilePath() {
        return getPropertyAsString(FILE_PATH);
    }

    /**
     * Set the path of the File.
     *
     * @param newPath
     *  the new path
     */
    public void setOffset(long newOffset) {
        setProperty(new LongProperty(OFFSET, newOffset));
    }

    /**
     * Get the path of the File.
     *
     * @return the file's path
     */
    public long getOffset() {
        return getPropertyAsLong(OFFSET);
    }

    /**
     * Set the path of the File.
     *
     * @param newPath
     *  the new path
     */
    public void setReadLength(long newReadLength) {
        setProperty(new LongProperty(READ_LENGTH, newReadLength));
    }

    /**
     * Get the path of the File.
     *
     * @return the file's path
     */
    public long getReadLength() {
        return getPropertyAsLong(READ_LENGTH);
    }

    /**
     * returns path, param name, mime type information of
     * GRPCBinaryField object.
     *
     * @return the string demonstration of GRPCBinaryField object in this
     * format:
     *    "path:'&lt;PATH&gt;'|param:'&lt;PARAM NAME&gt;'|mimetype:'&lt;MIME TYPE&gt;'"
     */
    @Override
    public String toString() {
        return "fieldPath:'" + getFieldPath()
            + "'|filePath:'" + getFilePath()
            + "'|offset:'" + getOffset()
            + "'|readLength:'" + getReadLength() + "'";
    }

    /**
     * Check if the entry is not empty.
     * @return true if Path, name or mimetype fields are not the empty string
     */
    public boolean isNotEmpty() {
        return getFieldPath().length() > 0
            || getFilePath().length() > 0;
    }

}
