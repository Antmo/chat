import ChatApp.*;          // The package containing our stubs
import org.omg.CosNaming.*; // HelloClient will use the naming service.
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;     // All CORBA applications need these classes.
import org.omg.PortableServer.*;   
import org.omg.PortableServer.POA;

import java.util.Scanner;
import java.util.Arrays;
import java.lang.NumberFormatException;

 
class ChatCallbackImpl extends ChatCallbackPOA
{
    private ORB orb;
    public void setORB(ORB orb_val) 
    {
        orb = orb_val;
    }

    public void callback(String notification)
    {
        System.out.println(notification);
    }
}

public class ChatClient
{
    static Chat chatImpl;
    static String name = "default";
    static String commands = 
	"# Available commands\tDescription\n# 'join <name>'\t\tjoin a chat room\n# 'post <message>'\tpost a message\n# 'list'\t\tlist members in chat room\n# 'play <marker>'\tjoin a game of five in a row\n# 'set X Y'\t\tset marker at position (X,Y)\n# 'quit'\t\tleave the game\n# 'leave'\t\tleave the chat\n# 'help'\t\tdisplay this message\n# '?'\t\t\tequivalent to 'help'\n";

    public static void main(String args[])
    {
	try {
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null);

	    // create servant (impl) and register it with the ORB
	    ChatCallbackImpl chatCallbackImpl = new ChatCallbackImpl();
	    chatCallbackImpl.setORB(orb);

	    // get reference to RootPOA and activate the POAManager
	    POA rootpoa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
	    
	    // get the root naming context 
	    org.omg.CORBA.Object objRef = 
		orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	    
	    // resolve the object reference in naming
	    String name = "Chat";
	    chatImpl = ChatHelper.narrow(ncRef.resolve_str(name));
	    
	    // obtain callback reference for registration w/ server
	    org.omg.CORBA.Object ref = 
		rootpoa.servant_to_reference(chatCallbackImpl);
	    ChatCallback cref = ChatCallbackHelper.narrow(ref);
	    
	    // Application code goes below
	    String chat = chatImpl.say(cref, "\n  Hello....");
	    System.out.println(chat);
	    
	    String action;
	    Scanner in = new Scanner(System.in);
	    

	    while(true)
		{
		    action = in.nextLine();
		    executeCommand(cref, action);
		    //		    chatImpl.say(cref, action);
		}
	    
	    
	} catch(Exception e){
	    System.out.println("ERROR : " + e);
	    e.printStackTrace(System.out);
	}
    }

    private static void executeCommand(ChatCallback cref, String action)
    {

	/* TODO 
	 * Improve the input parsing below
	 * Some inputs may cause a crash
	 */

	String [] args = action.split(" ");
	switch (args[0])
	    {
	    case "join" :
		if (args.length != 2 )
		    chatImpl.say(cref, "usage: join <name>");
		else if( !name.equals("default") )
		    chatImpl.say(cref, "you're already in the chat");
		else if (chatImpl.join(cref,args[1]) )
		    name = args[1];
		break;
	    case "post" :
		StringBuilder b = new StringBuilder(args.length);

		for(int i = 1; i < args.length; ++i)
		    b.append(args[i] + " ");

		String msg = b.toString();
		if( !("".equals(msg)) )
		    chatImpl.post(cref, msg, name);
		else
		    chatImpl.say(cref, "usage: post <message>");
		break;
	    case "leave" :
		chatImpl.leave(cref, name);
		name = "default";
		break;
	    case "list" :
		chatImpl.list(cref);
		break;
	    case "play" :
		if ( args.length == 2 )
		    chatImpl.play(cref, name, args[1].charAt(0));
		else
		    chatImpl.say(cref, "usage: play <marker>");
		break;
	    case "quit" :
		chatImpl.quit(cref, name);
		break;
	    case "set" :
		/* Crashes on some input */
		if ( args.length == 3 )
		    {
			try 
			    {
				chatImpl.set(cref, name, Integer.parseInt(args[1]), Integer.parseInt(args[2]));			    
			    }
			catch(NumberFormatException e)
			    {
				chatImpl.say(cref, "unable to parse input, try again");
			    }
		    }
		else
		    chatImpl.say(cref, "usage: set <X-coord> <Y-coord>");
		break;
	    case "help" :
		chatImpl.say(cref, commands);
		break;
	    case "?" :
		chatImpl.say(cref, commands);
		break;
	    default:
		chatImpl.say(cref, "command not found");
	    }
	return;
    }
}

