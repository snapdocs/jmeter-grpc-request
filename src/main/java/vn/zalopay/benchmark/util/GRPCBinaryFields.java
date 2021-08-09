package vn.zalopay.benchmark.util;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.zalopay.benchmark.core.ClientCaller;
import vn.zalopay.benchmark.core.specification.GrpcResponse;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class GRPCBinaryFields extends ConfigTestElement implements Serializable {
    // TODO!
    private static final long serialVersionUID = 241L;

    /** The name of the property used to store the files. */
    private static final String BINARY_FIELDS_ARGS = "GRPCBinaryFields.fields";

    /**
     * Create a new GRPCBinaryFields object with no files.
     */
    public GRPCBinaryFields() {
        setProperty(new CollectionProperty(BINARY_FIELDS_ARGS, new ArrayList<GRPCBinaryField>()));
    }

    /**
     * Get the binary fields.
     *
     * @return the fields
     */
    public CollectionProperty getGRPCBinaryFieldsCollection() {
        return (CollectionProperty) getProperty(BINARY_FIELDS_ARGS);
    }

    /**
     * Clear the fields.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(BINARY_FIELDS_ARGS, new ArrayList<GRPCBinaryField>()));
    }

    /**
     * Set the list of fields. Any existing fields will be lost.
     *
     * @param fields the new fields
     */
    public void setGRPCBinaryFields(List<GRPCBinaryField> fields) {
        setProperty(new CollectionProperty(BINARY_FIELDS_ARGS, fields));
    }

    /**
     * Add a new field with the given field path.
     *
     * @param fieldPath the path of the field in the proto request
     * @param filePath the path of the file to set in the field
     */
    public void addGRPCBinaryField(String fieldPath, String filePath) {
        addGRPCBinaryField(new GRPCBinaryField(fieldPath, filePath));
    }

    /**
     * Add a new field.
     *
     * @param field
     *  the new field
     */
    public void addGRPCBinaryField(GRPCBinaryField field) {
        TestElementProperty newGRPCBinaryField = new TestElementProperty(field.getFieldPath(), field);
        if (isRunningVersion()) {
            this.setTemporary(newGRPCBinaryField);
        }
        getGRPCBinaryFieldsCollection().addItem(newGRPCBinaryField);
    }

    /**
     * Adds a new field to the GRPCBinaryFields list to be added to the request.
     *
     * @param fieldPath the path of the bytes field in the request
     * @param filePath full path to file
     * @param offset byte offset to begin reading file
     * @param readLength length of bytes to read from file
     */
    public void addGRPCBinaryField(String fieldPath, String filePath, long offset, long readLength) {
        addGRPCBinaryField(new GRPCBinaryField(fieldPath, filePath, offset, readLength));
    }

    /**
     * Get a PropertyIterator of the files.
     *
     * @return an iteration of the files
     */
    public PropertyIterator iterator() {
        return getGRPCBinaryFieldsCollection().iterator();
    }

    /**
     * Get the current arguments as an array.
     *
     * @return an array of file arguments
     */
    public GRPCBinaryField[] asArray(){
        CollectionProperty props = getGRPCBinaryFieldsCollection();
        final int size = props.size();
        GRPCBinaryField[] args = new GRPCBinaryField[size];
        for(int i=0; i<size; i++){
            args[i]=(GRPCBinaryField) props.get(i).getObjectValue();
        }
        return args;
    }

    /**
     * Create a string representation of the files.
     *
     * @return the string representation of the files
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = getGRPCBinaryFieldsCollection().iterator();
        while (iter.hasNext()) {
            GRPCBinaryField file = (GRPCBinaryField) iter.next().getObjectValue();
            str.append(file.toString());
            if (iter.hasNext()) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    /**
     * Remove the specified file from the list.
     *
     * @param row the index of the file to remove
     */
    public void removeGRPCBinaryField(int row) {
        if (row < getGRPCBinaryFieldCount()) {
            getGRPCBinaryFieldsCollection().remove(row);
        }
    }

    /**
     * Remove the specified file from the list.
     *
     * @param file the file to remove
     */
    public void removeGRPCBinaryField(GRPCBinaryField file) {
        PropertyIterator iter = getGRPCBinaryFieldsCollection().iterator();
        while (iter.hasNext()) {
            GRPCBinaryField item = (GRPCBinaryField) iter.next().getObjectValue();
            if (file.equals(item)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the file with the specified path.
     *
     * @param filePath
     *  the path of the file to remove
     */
    public void removeGRPCBinaryField(String fieldPath) {
        PropertyIterator iter = getGRPCBinaryFieldsCollection().iterator();
        while (iter.hasNext()) {
            GRPCBinaryField file = (GRPCBinaryField) iter.next().getObjectValue();
            if (file.getFieldPath().equals(fieldPath)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all files from the list.
     */
    public void removeAllGRPCBinaryFields() {
        getGRPCBinaryFieldsCollection().clear();
    }

    /**
     * Add a new empty file to the list. The new file will have the
     * empty string as its path.
     */
    public void addEmptyGRPCBinaryField() {
        addGRPCBinaryField(new GRPCBinaryField("", ""));
    }

    /**
     * Get the number of files in the list.
     *
     * @return the number of files
     */
    public int getGRPCBinaryFieldCount() {
        return getGRPCBinaryFieldsCollection().size();
    }

    /**
     * Get a single file.
     *
     * @param row
     *  the index of the file to return.
     * @return the file at the specified index, or null if no file
     *  exists at that index.
     */
    public GRPCBinaryField getGRPCBinaryField(int row) {
        GRPCBinaryField file = null;
        if (row < getGRPCBinaryFieldCount()) {
            file = (GRPCBinaryField) getGRPCBinaryFieldsCollection().get(row).getObjectValue();
        }
        return file;
    }
}
