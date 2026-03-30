package org.company.cs455project2;

import java.io.*;

/**
 * MessageProtocol handles serialization and deserialization of Request and Response objects
 * for UDP transmission. Uses Java object serialization.
 */
public class MessageProtocol {
    
    /**
     * Serialize a Request object to a byte array for UDP transmission.
     * 
     * @param request the Request object to serialize
     * @return byte array representation of the request
     * @throws IOException if serialization fails
     */
    public static byte[] serializeRequest(Request request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.close();
        return baos.toByteArray();
    }
    
    /**
     * Deserialize a byte array to a Request object.
     * 
     * @param data byte array containing serialized request
     * @return Request object
     * @throws IOException if deserialization fails (network issue)
     * @throws ClassNotFoundException if Request class is not found
     */
    public static Request deserializeRequest(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Request request = (Request) ois.readObject();
        ois.close();
        return request;
    }
    
    /**
     * Serialize a Response object to a byte array for UDP transmission.
     * 
     * @param response the Response object to serialize
     * @return byte array representation of the response
     * @throws IOException if serialization fails
     */
    public static byte[] serializeResponse(Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.close();
        return baos.toByteArray();
    }
    
    /**
     * Deserialize a byte array to a Response object.
     * 
     * @param data byte array containing serialized response
     * @return Response object
     * @throws IOException if deserialization fails (network issue)
     * @throws ClassNotFoundException if Response class is not found
     */
    public static Response deserializeResponse(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Response response = (Response) ois.readObject();
        ois.close();
        return response;
    }
    
    /**
     * Check if the serialized data size exceeds the buffer limit.
     * 
     * @param data serialized data
     * @return true if size is within limits, false otherwise
     */
    public static boolean isValidSize(byte[] data) {
        return data.length <= Constants.BUFFER_SIZE;
    }
}
