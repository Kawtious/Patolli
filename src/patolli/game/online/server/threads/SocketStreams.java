/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server.threads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.utils.Console;

public final class SocketStreams {

    /**
     *
     * @param dis
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(final DataInputStream dis) throws IOException {
        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    /**
     *
     * @param dos
     * @param myByteArray
     * @throws IOException
     */
    public static void sendBytes(final DataOutputStream dos, final byte[] myByteArray) throws IOException {
        sendBytes(dos, myByteArray, 0, myByteArray.length);
    }

    /**
     *
     * @param dos
     * @param myByteArray
     * @param start
     * @param len
     * @throws IOException
     */
    public static void sendBytes(final DataOutputStream dos, final byte[] myByteArray, final int start, final int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= myByteArray.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }
        // Other checks if needed.

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
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
    public static void sendObject(final DataOutputStream dos, final Object object) throws IOException {
        sendBytes(dos, readObjectBytes(object));
    }

    /**
     *
     * @param client
     * @param message
     */
    public static void send(final SocketThread client, final byte[] message) {
        try {
            Console.WriteLine("SocketStreams/" + client.getPlayer().getName(), new String(message));
            sendBytes(client.getOutput(), message);
        } catch (IOException ex) {
        }
    }

    /**
     *
     * @param channel
     * @param message
     */
    public static void sendTo(final Channel channel, final byte[] message) {
        for (SocketThread client : channel.getClients()) {
            send(client, message);
        }
    }

    /**
     *
     * @param group
     * @param message
     */
    public static void sendTo(final Group group, final byte[] message) {
        for (SocketThread client : group.getClients()) {
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
    public static void send(final SocketThread client, final String message) {
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

}
