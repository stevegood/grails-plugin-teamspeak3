package org.stevegood.teamspeak3

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.event.*
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel

import javax.annotation.PostConstruct

class TeamSpeakService {

    static transactional = false

    TS3Config ts3Config
    TS3Query ts3Query
    TS3Api ts3Api
    def grailsApplication
    private boolean connected = false
    private TS3Listener chatListener
    private TS3Listener joinListener
    private TS3Listener leaveListener

    @PostConstruct
    void init() {
        if (!connected) {
            println 'init'
            def config = grailsApplication.mergedConfig.grails.plugin.teamspeak3
            println config

            if (!config?.username?.size() || !config?.password?.size())
                throw new Exception('TeamSpeak3 username and password are required to use the TeakSpeak3 plugin!')

            ts3Config = new TS3Config()
            ts3Config.host = config.host
            ts3Config.setLoginCredentials(config.username, config.password)

            ts3Query = new TS3Query(ts3Config)
            ts3Query.connect()
            connected = true

            ts3Api = ts3Query.api
            ts3Api.selectVirtualServerById(1)
            ts3Api.nickname = config.nick
            println 'init complete!'
        }
    }

    def initChatBot(Closure closure) {
        if (chatListener)
            ts3Api.removeTS3Listeners(chatListener)

        Closure closureClone = closure.clone()
        closureClone.delegate = this
        closureClone.resolveStrategy = Closure.DELEGATE_FIRST

        chatListener = new TS3Listener() {
            @Override
            void onTextMessage(TextMessageEvent textMessageEvent) {
                closureClone(textMessageEvent)
            }

            @Override
            void onClientJoin(ClientJoinEvent clientJoinEvent) {

            }

            @Override
            void onClientLeave(ClientLeaveEvent clientLeaveEvent) {

            }

            @Override
            void onServerEdit(ServerEditedEvent serverEditedEvent) {

            }

            @Override
            void onChannelEdit(ChannelEditedEvent channelEditedEvent) {

            }

            @Override
            void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {

            }

            @Override
            void onClientMoved(ClientMovedEvent clientMovedEvent) {

            }
        }

        ts3Api.registerEvent(TS3EventType.TEXT_PRIVATE)
        ts3Api.registerEvent(TS3EventType.TEXT_SERVER)
        ts3Api.channels.each { Channel channel ->
            ts3Api.registerEvent(TS3EventType.TEXT_CHANNEL, channel.id)
        }

        ts3Api.addTS3Listeners(chatListener)
    }

    def onServerLeave(Closure closure) {
        if (leaveListener)
            ts3Api.removeTS3Listeners(leaveListener)

        Closure runClosure = closure.clone()
        runClosure.delegate = this
        runClosure.resolveStrategy = Closure.DELEGATE_FIRST

        leaveListener = new TS3Listener() {
            @Override
            void onTextMessage(TextMessageEvent textMessageEvent) {}

            @Override
            void onClientJoin(ClientJoinEvent clientJoinEvent) {}

            @Override
            void onClientLeave(ClientLeaveEvent clientLeaveEvent) {
                runClosure clientLeaveEvent
            }

            @Override
            void onServerEdit(ServerEditedEvent serverEditedEvent) {}

            @Override
            void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}

            @Override
            void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}

            @Override
            void onClientMoved(ClientMovedEvent clientMovedEvent) {}
        }

        ts3Api.registerEvent(TS3EventType.SERVER)
        ts3Api.addTS3Listeners leaveListener
    }

    def onServerJoin(Closure closure) {
        if (joinListener)
            ts3Api.removeTS3Listeners(joinListener)

        Closure runClosure = closure.clone()
        runClosure.delegate = this
        runClosure.resolveStrategy = Closure.DELEGATE_FIRST

        joinListener = new TS3Listener() {
            @Override
            void onTextMessage(TextMessageEvent textMessageEvent) {}

            @Override
            void onClientJoin(ClientJoinEvent clientJoinEvent) {
                runClosure clientJoinEvent
            }

            @Override
            void onClientLeave(ClientLeaveEvent clientLeaveEvent) {}

            @Override
            void onServerEdit(ServerEditedEvent serverEditedEvent) {}

            @Override
            void onChannelEdit(ChannelEditedEvent channelEditedEvent) {}

            @Override
            void onChannelDescriptionChanged(ChannelDescriptionEditedEvent channelDescriptionEditedEvent) {}

            @Override
            void onClientMoved(ClientMovedEvent clientMovedEvent) {}
        }

        ts3Api.registerEvent(TS3EventType.SERVER)
        ts3Api.addTS3Listeners joinListener
    }

    def getClients() {
        ts3Api.clients
    }

    def sendServerMessage(String message) {
        ts3Api.sendServerMessage message
    }

    def sendMessage(String message) {
        ts3Api.sendChannelMessage(message)
    }

    def sendPrivateMessage(int clientId, String message) {
        ts3Api.sendPrivateMessage(clientId, message)
    }

    def broadcastMessage(String message) {
        ts3Api.broadcast message
    }
}
