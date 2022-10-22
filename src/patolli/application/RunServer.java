/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.application;

import patolli.game.online.server.PatolliServer;

public class RunServer {

    private static final int PORT = 1001;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        PatolliServer server = new PatolliServer();

        server.setPort(PORT);
        server.setName("server");
        server.setPassword("");

        if (!server.start()) {
            System.exit(1);
        }
    }

}
