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
            println 'init complete!'
        }
    }

    def initChatBot(Closure closure) {
        def config = grailsApplication.mergedConfig.grails.plugin.teamspeak3
        ts3Api.nickname = config.nick

        ts3Api.unregisterAllEvents()

        Closure closureClone = closure.clone()
        closureClone.delegate = this
        closureClone.resolveStrategy = Closure.DELEGATE_FIRST

        TS3Listener ts3Listener = new TS3Listener() {
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

        ts3Api.channels.each { Channel channel ->
            ts3Api.registerEvent(TS3EventType.TEXT_CHANNEL, channel.id)
            ts3Api.registerEvent(TS3EventType.TEXT_PRIVATE, channel.id)
        }

        ts3Api.addTS3Listeners(ts3Listener)
    }

    def getClients() {
        ts3Api.clients
    }

    def sendMessage(String message) {
        ts3Api.sendChannelMessage(message)
    }

    def sendPrivateMessage(int clientId, String message) {
        ts3Api.sendPrivateMessage clientId, message
    }

    def broadcastMessage(String message) {
        ts3Api.broadcast message
    }
}
