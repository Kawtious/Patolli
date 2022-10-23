/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online.server;

import dradacorus.online.ExtendableDragonServer;
import dradacorus.online.IKoboldSocket;
import java.io.IOException;
import java.net.Socket;
import patolli.game.online.client.PatolliActions;
import patolli.game.online.client.PlayerSocket;

public class PatolliServer extends ExtendableDragonServer {

    public PatolliServer(int port) {
        super(port);
    }

    @Override
    public IKoboldSocket createKoboldSocket(Socket socket) throws IOException {
        return new PlayerSocket(this, new PatolliActions(this), socket);
    }

    @Override
    public void createLair(IKoboldSocket kobold, String name, String password) {
        addLair(new GameLair(this, name, password), kobold);
    }

}
