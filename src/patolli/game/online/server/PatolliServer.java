/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online.server;

import dradacorus.online.client.IDragonSocket;
import dradacorus.online.server.DragonServer;
import java.io.IOException;
import java.net.Socket;
import patolli.game.online.client.PlayerSocket;

public class PatolliServer extends DragonServer {

    @Override
    public IDragonSocket createDragonSocket(Socket socket) throws IOException {
        return new PlayerSocket(this, socket);
    }

    @Override
    public void createLayer(IDragonSocket client, String name, String password) {
        addLayer(new GameLayer(this, name, password), client);
    }

}
