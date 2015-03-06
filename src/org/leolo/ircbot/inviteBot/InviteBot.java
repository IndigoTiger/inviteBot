package org.leolo.ircbot.inviteBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.SASLCapHandler;
import org.pircbotx.exception.IrcException;

public class InviteBot{
	
	public static void main(String [] args) throws IOException{
		Config config;
		if(args.length == 0)
			config = new Config();
		else
			config = new Config(args[0]);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Builder b = new Configuration.Builder()
		.setName(config.nick) //Nick of the bot. CHANGE IN YOUR CODE
		.setLogin(config.ident) //Login part of hostmask, eg name:login@host
		.setAutoNickChange(true) //Automatically change nick when the current one is in use
		.setServer(config.server, config.port)
		.addListener(new Inviter(config));
		if(config.ssl){
			b = b.setSocketFactory(new UtilSSLSocketFactory().disableDiffieHellman().trustAllCertificates());
			b = b.setCapEnabled(true).addCapHandler(new SASLCapHandler(config.nick, config.password));
		}
		
		PircBotX myBot = new PircBotX(b.buildConfiguration());
		try {
			myBot.startBot();
		} catch (IrcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
