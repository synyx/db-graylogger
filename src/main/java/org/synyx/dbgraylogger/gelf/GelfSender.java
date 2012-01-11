package org.synyx.dbgraylogger.gelf;

import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class GelfSender {

    private static final int DEFAULT_PORT = 12201;

    private static final byte[] GELF_CHUNKED_ID = new byte[]{0x1e, 0x0f};
    private static final int MAXIMUM_CHUNK_SIZE = 1420;

    private static final String ID = "id";

    private InetAddress host;
    private int port;
    private DatagramSocket socket;

    public GelfSender(String host) throws UnknownHostException, SocketException {

        this(host, DEFAULT_PORT);
    }

    public GelfSender(String host, int port) throws UnknownHostException, SocketException {

        this.host = InetAddress.getByName(host);
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public boolean sendMessage(GelfMessage message) {

        return message.isValid() && sendDatagrams(toDatagrams(message));
    }

    private boolean sendDatagrams(List<byte[]> bytesList) {

        for (byte[] bytes : bytesList) {

            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, host, port);

            try {
                socket.send(datagramPacket);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    private List<byte[]> toDatagrams(GelfMessage message) {

        byte[] messageBytes = gzipMessage(toJson(message));

        List<byte[]> datagrams = new ArrayList<byte[]>();

        if (messageBytes.length > MAXIMUM_CHUNK_SIZE) {
            sliceDatagrams(messageBytes, datagrams, message.getHost());
        } else {
            datagrams.add(messageBytes);
        }

        return datagrams;
    }

    private String toJson(GelfMessage message) {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("version", message.getVersion());
        map.put("host", message.getHost());
        map.put("short_message", message.getShortMessage());
        map.put("full_message", message.getFullMessage());
        map.put("timestamp", message.getTimestamp().intValue());

        map.put("level", message.getLevel());
        map.put("facility", message.getFacility());
        map.put("file", message.getFile());
        map.put("line", message.getLine());

        for (Map.Entry<String, Object> additionalField : message.getAdditonalFields().entrySet()) {
            if (!ID.equals(additionalField.getKey())) {
                map.put("_" + additionalField.getKey(), additionalField.getValue());
            }
        }

        return JSONValue.toJSONString(map);
    }

    private void sliceDatagrams(byte[] messageBytes, List<byte[]> datagrams, String host) {

        int messageLength = messageBytes.length;
        byte[] messageId = Arrays.copyOf((new Date().getTime() + host).getBytes(), 32);
        int num = ((Double) Math.ceil((double) messageLength / MAXIMUM_CHUNK_SIZE)).intValue();

        for (int idx = 0; idx < num; idx++) {

            byte[] header = concatByteArray(GELF_CHUNKED_ID, concatByteArray(messageId, new byte[]{0x00, (byte) idx, 0x00, (byte) num}));

            int from = idx * MAXIMUM_CHUNK_SIZE;
            int to = from + MAXIMUM_CHUNK_SIZE;

            if (to >= messageLength) {
                to = messageLength;
            }
            byte[] datagram = concatByteArray(header, Arrays.copyOfRange(messageBytes, from, to));
            datagrams.add(datagram);
        }
    }

    private byte[] gzipMessage(String message) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            OutputStream stream = new GZIPOutputStream(bos);
            stream.write(message.getBytes());
            stream.close();
            byte[] zipped = bos.toByteArray();
            bos.close();
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] concatByteArray(byte[] first, byte[] second) {

        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }

    public void close() {

        socket.close();
    }
}
