package org.leolo.ircbot.inviteBot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.leolo.ircbot.inviteBot.util.PropertyMapper;
import org.leolo.ircbot.inviteBot.util.Property;
import org.leolo.ircbot.inviteBot.util.PropertyMapperException;
import org.leolo.ircbot.inviteBot.util.Glob;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Config {
	final Logger logger = LoggerFactory.getLogger(Config.class);
	class Channel {
		@Property( name = ".join", description = "IRC channel to join.", required = true )
		private String channelName;

		@Property( name = ".exemptMask", description = "Comma-separated list of mask that is exempt from remove" )
		private ArrayList<String> exemptMask;

		@Property( name = ".listen", description = "Where the bot will listen to incoming join." )
		private String listenChannel;

		@Property( name = ".report", description = "Where the bot will reports join, invite and remove" )
		private String reportChannel;

		@Property( name = ".admin", description = "Comma-separated list of mask that is able to invite for this channel" )
		private ArrayList<String> admins;

		@Property( name = ".key", description = "Unused property" )
		@Deprecated
		private String adminkey;
		
		private String key;
		
		Channel(Properties setting, String key) throws PropertyMapperException {
			PropertyMapper mapper = new PropertyMapper(this);
			mapper.fillDefaults();
			mapper.map(key, setting);
			mapper.checkRequired();
		}

		public String getChannelName() {
			return channelName;
		}

		public Collection<String> getExemptMask() {
			return exemptMask;
		}

		public String getListenChannel() {
			return listenChannel;
		}

		public String getReportChannel() {
			return reportChannel;
		}

		public boolean isAdmin(User user){
			for(String admin:admins){
				if(Glob.match(admin, user.getNick()+"!"+user.getLogin()+"@"+user.getHostmask()))
					return true;
				
			}
			return false;
		}

		protected String getAdminkey() {
			return adminkey;
		}
		
		public boolean isExempted(User user){
			String hostmask = user.getNick()+"!"+user.getLogin()+"@"+user.getHostmask();
			for(String mask:exemptMask){
				if(Glob.match(mask, hostmask))
					return true;
			}
			return false;
		}
		
		public boolean removeAdmin(String mask){
			Iterator<String> iAdmin = admins.iterator();
			while(iAdmin.hasNext()){
				String admin = iAdmin.next();
				if(admin.equalsIgnoreCase(mask)){
					iAdmin.remove();
					return true;
				}
			}
			return false;
		}
		
		public void addAdmin(String mask){
			admins.add(mask);
		}
		
		public boolean removeExempt(String mask){
			Iterator<String> iExemptMask = exemptMask.iterator();
			while(iExemptMask.hasNext()){
				String admin = iExemptMask.next();
				if(admin.equalsIgnoreCase(mask)){
					iExemptMask.remove();
					return true;
				}
			}
			return false;
		}
		
		public void addExempt(String mask){
			exemptMask.add(mask);
		}
		
		public String getKey(){
			return key;
		}
	}

	@Property( description = "IRC server to connect to.", required = true )
	private String server;

	@Property( description = "Port to connect to.", defaultValue = "6667" )
	private int port;

	@Property( description = "Whether to use secure TLS/SSL connection.", defaultValue = "false" )
	private boolean ssl;

	@Property( description = "Password which bot will use to authenticate itself on IRC network." )
	private String password;

	@Property( name = "admin", description = "Comma-separated list of admins who can control the bot." )
	private String[] admins;

	@Property( description = "Escape prefix used for commands given to bot.", defaultValue = "!" )
	private String escape;

	@Property( description = "IRC nickname used by the bot.", defaultValue = "inviteBot" )
	private String nick;

	@Property( description = "Login name seen in IRC beside hostname.", defaultValue = "inviteBot" )
	private String username;

	@Property( name = "key", description = "Comma-separated list of prefixes used to configure channels.", defaultValue = "" )
	private String channelStringList[];

	@Property( description = "IRC profile identity field.", defaultValue = "pircbot" )
	private String ident;

	@Property( name = "welcome", description = "Welcome message.", defaultValue = "Welcome to %t!" )
	private String welcomeMessage;

	private ArrayList<Channel> channelList;

	private Properties prop;

	public Config() throws FileNotFoundException, IOException, PropertyMapperException {
		this("settings.properties");
	}

	public Config(String file) throws FileNotFoundException, IOException, PropertyMapperException {
		channelList = new ArrayList<>();
		prop = new java.util.Properties();
		prop.load(new java.io.FileInputStream(file));
		PropertyMapper mapper = new PropertyMapper(this);
		mapper.fillDefaults();
		mapper.map(prop);
		mapper.checkRequired();
		String[] keys = channelStringList;
		for (String s : keys) {
			Channel c = new Channel(prop, s);
			c.key = s;
			channelList.add(c);
		}
	}

	public String[] getAdmins() {
		return admins;
	}

	public String getEscape() {
		return escape;
	}

	public String getIdent() {
		return ident;
	}

	public ArrayList<Channel> getList() {
		return channelList;
	}

	public String getNick() {
		return nick;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public String getServer() {
		return server;
	}

	public boolean isSSL() {
		return ssl;
	}
	
	public java.util.List<Channel> getChannels(){
		return channelList;
	}
	
	public String[] getChannelList(){
		Hashtable<String,String> list = new Hashtable<>();
		for(Channel c:channelList){
			list.put(c.channelName, c.channelName);
			list.put(c.listenChannel,c.listenChannel);
			if( c.reportChannel.length() > 0)
				list.put(c.reportChannel,c.reportChannel);
		}
		String [] result = new String[list.size()];
		list.values().toArray(result);
		return result;
		
	}
	
	/**
	 * To see is user <b>GLOBAL ADMIN</b>
	 * @param user User to be checked
	 * @return true if user is super admin
	 */
	public boolean isGlobalAdmin(User user){
		if(user.isIrcop())
			return true;
		for(String admin:admins){
			if(Glob.match(admin, user.getNick()+"!"+user.getLogin()+"@"+user.getHostmask()))
				return true;
			
		}
		return false;
	}
	
	public boolean isAdmin(User user){
		if(isGlobalAdmin(user)){
			return true;
		}
		for(Channel c:channelList){
			if(c.isAdmin(user)){
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isAdmin(User user,String channel){
		logger.warn("User {}!{}@{} checking admin for channel {}", user.getNick(),user.getLogin(),user.getHostmask(), channel);
		if(isGlobalAdmin(user))
			return true;
		for(Channel c:channelList){
			if(channel.equalsIgnoreCase(c.channelName) ||
					channel.equalsIgnoreCase(c.listenChannel) ||
					channel.equalsIgnoreCase(c.reportChannel)){
				logger.warn("Checking channel {}",c.channelName);
				if(c.isAdmin(user)){
					logger.warn("Found admin right in channel {} for {}",
							c.channelName,user.getNick());
					return true;
				}
			}	
		}
		return false;
	}
	
	public boolean isAdmin(String key,String channel){
		logger.warn("Checking admin key {} in channel {}",key,channel);
		for(Channel c:channelList){
			if(channel.equalsIgnoreCase(c.channelName) ||
					channel.equalsIgnoreCase(c.listenChannel) ||
					channel.equalsIgnoreCase(c.reportChannel)){
				if(c.adminkey.equalsIgnoreCase(key))
					return true;
			}	
		}
		return false;
	}
	
	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public boolean isListenChannel(String source) {
		for(Channel channel:channelList){
			if(channel.listenChannel.equalsIgnoreCase(source))
				return true;
		}
		return false;
	}
	
	public boolean isExempted(User user,String target){
		for(Channel channel:channelList){
			if(channel.channelName.equalsIgnoreCase(target)){
				return channel.isExempted(user);
			}
		}
		return false;
	}
	
	public String getReportChannel(String channel){
		for(Channel c:channelList){
			if(c.channelName.equalsIgnoreCase(channel) || c.listenChannel.equalsIgnoreCase(channel)){
				String target = c.reportChannel;
				if(target.length() == 0)
					return null;
				return target;
			}
		}
		return null;
	}
	
	public String writeBackup(){
		String target = "settings.properties."+Integer.toHexString(prop.hashCode());
		try {
			prop.store(new PrintWriter(target), "Backup at "+new Date());
		} catch (IOException e) {
			logger.error(e.toString(), e);
			e.printStackTrace();
		}
		return target;
	}

	public String write(){
		String target = "settings.properties";
		try {
			prop.store(new PrintWriter(target), "Backup at "+new Date());
		} catch (IOException e) {
			logger.error(e.toString(), e);
			e.printStackTrace();
		}
		return target;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public static Property[] getGlobalSettings() {
		return PropertyMapper.getProperties(Config.class);
	}

	public static Property[] getChannelSettings() {
		return PropertyMapper.getProperties(Channel.class);
	}
}
