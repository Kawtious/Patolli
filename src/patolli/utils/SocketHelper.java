/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import patolli.game.online.IClientSocket;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;

public final class SocketHelper {

    /**
     *
     * @param dis
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(final DataInputStream dis, byte[] key) throws IOException {
        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return TinkHelper.decryptBytes(data, key);
    }

    /**
     *
     * @param dos
     * @param data
     * @throws IOException
     */
    public static void sendBytes(DataOutputStream dos, byte[] data, byte[] key) throws IOException {
        byte[] bytes = TinkHelper.encryptBytes(data, key);
        sendBytes(dos, bytes, 0, bytes.length);
    }

    /**
     *
     * @param dos
     * @param data
     * @param start
     * @param len
     * @throws IOException
     */
    public static void sendBytes(final DataOutputStream dos, final byte[] data, final int start, final int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= data.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }
        // Other checks if needed.

        dos.writeInt(len);
        if (len > 0) {
            dos.write(data, start, len);
        }
    }

    /**
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(final byte[] bytes) throws IOException, ClassNotFoundException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }

    /**
     *
     * @param object
     * @return
     * @throws IOException
     */
    public static byte[] readObjectBytes(final Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.flush();

        return bos.toByteArray();
    }

    /**
     *
     * @param dos
     * @param object
     * @throws IOException
     */
    public static void sendObject(final IClientSocket client, final Object object) throws IOException {
        send(client, readObjectBytes(object));
    }

    /**
     *
     * @param socket
     * @param message
     */
    public static void send(final IClientSocket client, final byte[] message) {
        try {
            sendBytes(client.getDos(), message, client.getKey());
        } catch (IOException ex) {
        }
    }

    /**
     *
     * @param channel
     * @param message
     */
    public static void sendTo(final Channel channel, final byte[] message) {
        for (IClientSocket client : channel.getClients()) {
            send(client, message);
        }
    }

    /**
     *
     * @param group
     * @param message
     */
    public static void sendTo(final Group group, final byte[] message) {
        for (IClientSocket client : group.getClients()) {
            if (client.getChannel() == null) {
                send(client, message);
            }
        }
    }

    /**
     *
     * @param client
     * @param message
     */
    public static void send(final IClientSocket client, final String message) {
        send(client, message.getBytes());
    }

    /**
     *
     * @param channel
     * @param message
     */
    public static void sendTo(final Channel channel, final String message) {
        sendTo(channel, message.getBytes());
    }

    /**
     *
     * @param group
     * @param message
     */
    public static void sendTo(final Group group, final String message) {
        sendTo(group, message.getBytes());
    }

    private SocketHelper() {
    }

}
