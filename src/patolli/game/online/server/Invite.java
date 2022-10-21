/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import patolli.game.online.IClientSocket;

public class Invite {

    private final IClientSocket sender;

    private final Group group;

    private final Channel channel;

    public Invite(IClientSocket sender, Group group, Channel channel) {
        this.sender = sender;
        this.group = group;
        this.channel = channel;
    }

    public IClientSocket getSender() {
        return sender;
    }

    public Group getGroup() {
        return group;
    }

    public Channel getChannel() {
        return channel;
    }

}
