package dev.moetz.buzzer.template

import io.ktor.html.*
import kotlinx.html.*

class InfoSiteTemplate(
    private val buzzerButtonColorReady: String,
    private val buzzerButtonColorBuzzed: String
) : Template<FlowContent> {
    override fun FlowContent.apply() {
        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"What is this?"
            }

            content {
                +"You can use this for any game / situation you would need a multiplayer-buzzer-system."
                br()
                +"Anybody can create a new lobby, which can be used for others to join and buzz. "
                +"There is always one host, who will see the order of buzzes and can reset the buzzing-state for each round."
            }
        }

        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"How to host a lobby?"
            }

            content {
                +"To create a new buzzing session ('lobby'), just click on 'Create a lobby'."
                br()
                +"This will navigate you to the host-page of the newly created lobby."
                br()
                br()
                i { +"Technical detail:" }
                br()
                +"Note, that there is no real "
                i { +"lobby-creation" }
                +", only navigating to a url in the declared schema will "
                i { +"open" }
                +" that lobby. So, basically, you can just navigate to '/host/{lobbyCode}' with your preferred lobby code."
                br()
                +"A lobby does not need to be cleared / closed, once the host as well as all participants have closed the page, the lobby will destroy itself."
            }
        }

        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"How to join / use as participant?"
            }

            content {
                +"You either get shared a link or QR code from a host to a page where you only need to enter your nickname, "
                +"or you can enter the lobby-code and nickname directly on the main page."
                br()
                +"Once you've entered the nickname and joined the lobby, you will see the buzz-button. "
                +"Once you buzz, our buzz wll be displayed to the host in the order all the participants buzzed. "
                +"You can only buzz once until the host resets all buzzes again for the next round. "
                +"The button will have a different color when you "
                span {
                    style = "color: $buzzerButtonColorReady;"
                    +"can buzz"
                }
                +", and when you "
                span {
                    style = "color: $buzzerButtonColorBuzzed;"
                    +"already buzzed"
                }
                +"."
                br()
                +"Note, that once you close the page / connection is lost, the buzz for this round will be lost."
            }
        }

        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"Which data is stored on the server?"
            }

            content {
                +"The server will only have information you provided it. This includes exclusively the lobby-code, the nickname you entered, as well as potentially your IP address in server logs. "
                +"The nickname and lobby-code will not be stored persistently, but may show up in application logs, which will be reset on application restart. "
                +"While the application itself will not log your IP address, the server this application is hosted on may for monitoring / statistical reasons."
            }
        }

        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"Stream / OBS Overlay?"
            }

            content {
                +"Yes. Once you created the lobby, the host will see a "
                i { +"'Buzzes Overlay Link'" }
                +" in the bottom right corner, which navigates to a link you can use as OBS Browser source, which "
                +"basically shows the part of the host view where you see who buzzed. "
                +"As OBS browser source, enter 400 as width, and height to as much as you prefer to have displayed."
            }
        }

        insert(InfoSiteItem()) {
            titlePlaceholder {
                +"Can I host this on my own server / look at the source code?"
            }

            content {
                b { +"Yes!" }
                br()
                +"Have a look over at "
                a(href = "https://github.com/FlowMo7/kotlin-buzzer") { +"github.com/FlowMo7/kotlin-buzzer" }
                +" for the source code as well as how to host this on your own using docker."
            }
        }
    }
}
