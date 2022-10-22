/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.application;

import dradacorus.online.kobold.KoboldClient;

public class PatolliRunClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1001;
    private static String[] testArgs = {};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        KoboldClient client = new KoboldClient();

        client.setIp(SERVER_IP);
        client.setPort(SERVER_PORT);
        client.setArgs(testArgs);

        if (!client.run()) {
            System.exit(1);
        }
    }

}
