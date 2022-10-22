/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online.server;

import dradacorus.online.dragon.DragonServer;
import dradacorus.online.kobold.IKoboldSocket;
import java.io.IOException;
import java.net.Socket;
import patolli.game.online.client.PlayerSocket;

public class PatolliServer extends DragonServer {

    @Override
    public IKoboldSocket createKoboldSocket(Socket socket) throws IOException {
        return new PlayerSocket(this, socket);
    }

    @Override
    public void createLair(IKoboldSocket client, String name, String password) {
        addLair(new GameLayer(this, name, password), client);
    }

}
