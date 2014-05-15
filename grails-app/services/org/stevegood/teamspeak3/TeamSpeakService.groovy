package org.stevegood.teamspeak3

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent

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
        closureClone.resolveStrategy = Closure.DELEGATE_ONLY

        ts3Api.registerAllEvents()
        ts3Api.addTS3Listeners(new TS3Listener() {
            @Override
            void onTextMessage(TextMessageEvent e) {
                closureClone(e)
            }

            @Override
            void onClientJoin(ClientJoinEvent e) {
                println "${e.clientNickname} joined..."
                if (config.annouceBot && e.clientNickname == ts3Api.nickname) {
                    println 'Announcing chatBot'
                    ts3Api.sendChannelMessage "${ts3Api.nickname} is now online!"
                }
            }

            @Override
            void onClientLeave(ClientLeaveEvent e) {}

            @Override
            void onServerEdit(ServerEditedEvent e) {}

            @Override
            void onChannelEdit(ChannelEditedEvent e) {}

            @Override
            void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {}

            @Override
            void onClientMoved(ClientMovedEvent e) {}
        })
    }

    def getClients() {
        ts3Api.clients
    }

    def sendMessage(String message) {
        ts3Api.sendChannelMessage(message)
    }
}
