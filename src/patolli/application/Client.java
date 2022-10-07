/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.application;

import patolli.game.online.ClientManager;

public class Client {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 95;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final ClientManager client = ClientManager.getInstance();

        client.setIp(SERVER_IP);
        client.setPort(SERVER_PORT);

        if (!client.run()) {
            System.exit(1);
        }
    }

}
