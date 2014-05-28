grails-plugin-teamspeak3
========================

A Grails plugin that integrates with TeamSpeak3.

Configuration
-------------

Add the following to your Config.groovy, replacing the values with those that correspond to your TS3 server.

    grails {
        plugin {
            teamspeak3 {
                host = 'voice.teamspeak.com'
                queryPort = 10011
                username = ''
                password = ''
                nick = 'TeamSpeak3GrailsPlugin'
                announceBot = false
                autoconnect = false
            }
    
        }
    }

Chat bot
--------

To add chat bot functionality add the teamSpeakService similar to the following:

    teamSpeakService.initChatBot { TextMessageEvent event ->
        if (event.message.startsWith('!')) { // check to see if the message is formatted as a message
            switch(event.message) {
                case '!sayHello':
                    sendMessage "Hello ${event.invokerName}, it's nice to see you."
                    break
            }
        }
    }
    
The ```initChatBot``` method takes a closure as its only argument and will execute the closure each time a message is received.  The delegate of the closure is set to the ```teamSpeakService``` and will have a ```sendMessage``` method exposed to it that can be used to send a message back to the channel.

Additional Integration
----------------------

Additional functionality will be exposed through the ```teamSpeakService``` as the project moves forward.  However, all functionality can be accessed through ```teamSpeakService.ts3Api```, ```teamSpeakService.ts3Query``` and ```teamSpeakService.ts3Config```.  More documentation for the TeamSpeak3 library can be found [here](https://github.com/stevegood/TeamSpeak-3-Java-API).
