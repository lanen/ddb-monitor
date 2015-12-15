package com.ddb.metrics.falcon;


import org.apache.commons.lang3.StringUtils;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 *
 * @author evan
 * @Date 2015年11月16日T14:10
 */
public class Falcon implements FalconSender {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    // this may be optimistic about Carbon/Graphite
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final String hostname;
    private final int port;
    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;

    private String endpoint;

    private String tags;

    private int step;

    private Socket socket;
    private Writer writer;
    private int failures;

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     */
    public Falcon(String hostname, int port) {
        this(hostname, port, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     * @param socketFactory the socket factory
     */
    public Falcon(String hostname, int port, SocketFactory socketFactory){
        this(hostname, port, socketFactory, UTF_8);
    }
    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     */
    public Falcon(String hostname, int port, SocketFactory socketFactory, Charset charset) {
        this.hostname = hostname;
        this.port = port;
        this.address = null;
        this.socketFactory = socketFactory;
        this.charset = charset;
    }


    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param address the address of the Carbon server
     */
    public Falcon(InetSocketAddress address) {
        this(address, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     */
    public Falcon(InetSocketAddress address, SocketFactory socketFactory) {
        this(address, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     */
    public Falcon(InetSocketAddress address, SocketFactory socketFactory, Charset charset) {
        this.hostname = null;
        this.port = -1;
        this.address = address;
        this.socketFactory = socketFactory;
        this.charset = charset;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        InetSocketAddress address = this.address;
        if (address == null) {
            address = new InetSocketAddress(hostname, port);
        }
        if (address.getAddress() == null) {
            // retry lookup, just in case the DNS changed
            address = new InetSocketAddress(address.getHostName(),address.getPort());

            if (address.getAddress() == null) {
                throw new UnknownHostException(address.getHostName());
            }
        }

        this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));

    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        send(endpoint,sanitize(name),value,timestamp,step,"GAUGE",tags);
    }

    //    @Override
    public void send0000(String name, String value, long timestamp) throws IOException {
        try {
            writer.write("POST /v1/push HTTP/1.1\r\n");
            writer.write("Host: "+hostname+"\r\n");
            writer.write("\r\n");

            writer.write(sanitize(name));
            writer.write(' ');
            writer.write(sanitize(value));
            writer.write(' ');
            writer.write(Long.toString(timestamp));
            writer.write('\n');
            this.failures = 0;
        } catch (IOException e) {
            failures++;
            throw e;
        }
    }

    private StringBuilder builder = new StringBuilder();

    void doPost() throws IOException{
        try {

            writer.write("POST /v1/push HTTP/1.1\r\n");
            writer.write("Host: "+hostname+"\r\n");
            writer.write("Content-Type: application/json\r\n");
            writer.write("Content-Length: " + (builder.length()+2) + "\r\n");
            writer.write("\r\n");


            writer.append('[');
            writer.write(builder.toString());
            writer.append(']');

            writer.write('\n');
            this.failures = 0;
        } catch (IOException e) {
            failures++;
            throw e;
        }
    }

    @Override
    public void send(String endpoint, String name, String value, long timestamp, int step, String counterType, String tags) throws IOException {

        StringBuilder b = new StringBuilder();
        if (builder.length()>2){
            b.append(',');
        }
        b.append('{');
        b.append("\"endpoint\":").append("\"").append(endpoint).append("\",");
        b.append("\"metric\":").append("\"").append(name).append("\",");
        b.append("\"timestamp\":").append(timestamp).append(",");
        b.append("\"step\":").append(step).append(",");
        b.append("\"value\":").append(value).append(",");
        b.append("\"counterType\":").append("\"").append(counterType).append("\",");
        b.append("\"tags\":").append("\"").append(tags).append("\"");
        b.append('}');

        builder.append(b);
    }

    @Override
    public void send(String name, String value, long timestamp, String counterType) throws IOException {
        send(endpoint,sanitize(name),value,timestamp,step,counterType,tags);
    }

    @Override
    public void flush() throws IOException {
        if (writer != null) {
            doPost();
            writer.flush();
            builder = null;
            builder = new StringBuilder();
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }



    @Override
    public int getFailures() {
        return failures;
    }

    @Override
    public void close() throws IOException {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            if (socket != null) {
                socket.close();
            }
        } finally {
            this.socket = null;
            this.writer = null;
        }
    }
    protected String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
        if (StringUtils.isBlank(tags))this.tags = "";
    }
}

