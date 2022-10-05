/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

}
