package com.ddb.metrics.falcon;


import java.io.Closeable;
import java.io.IOException;

/**
 * @author evan
 * @Date 2015年11月16日T14:07
 */
public interface FalconSender extends Closeable {

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws IOException if there is an error connecting
     */
    void connect() throws IllegalStateException, IOException;

    /**
     * Sends the given measurement to the server.
     *
     * @param name         the name of the metric
     * @param value        the value of the metric
     * @param timestamp    the timestamp of the metric
     * @throws IOException if there was an error sending the metric
     */
    void send(String name, String value, long timestamp)
            throws IOException;

    /**
     * Sends the given measurement to the server.
     *
     * @param endpoint     标明Metric的主体(属主)比如metric是cpu_idle，那么Endpoint就表示这是哪台机器的cpu_idle
     * @param name         the name of the metric
     * @param value        the value of the metric
     * @param timestamp    the timestamp of the metric
     * @param step
     * @param counterType
     * @param tags
     * @throws IOException if there was an error sending the metric
     */
    void send(String endpoint, String name, String value, long timestamp, int step, String counterType, String tags)
            throws IOException;

    void send(String name, String value, long timestamp, String counterType)
            throws IOException;
    /**
     * Flushes buffer, if applicable
     *
     * @throws IOException
     */
    void flush() throws IOException;

    /**
     * Returns true if ready to send data
     */
    boolean isConnected();

    /**
     * Returns the number of failed writes to the server.
     *
     * @return the number of failed writes to the server
     */
    public int getFailures();

}
