package de.tu.dresden.dud.dc.ServerCLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.KeyExchangeManager;
import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DCServerCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	    BasicConfigurator.configure();
		
	    Logger.getRootLogger().setLevel(Level.WARN);
		Logger log = Logger.getLogger(DCServerCLI.class);

	    int 	listenPort					= Connection.DEFAULTPORT;
	    short 	keyGenerationMethod			= KeyGenerator.KGMETHOD_PROBAB_FAIL_STOP; 
	    short 	keyExchangeMethod			= KeyExchangeManager.KEX_FULLY_AUTOMATIC;
	    short 	individualMessageLengths	= WorkCycleManager.MESSAGE_LENGTHS_VARIABLE;
				
	    Server  server = null;
	    Thread  t = null;
		
	    InputStreamReader 	input 	= new InputStreamReader(System.in);
	    BufferedReader 		reader 	= new BufferedReader(input); 
	    String 				cmd		= null;
	    
		Options options = new Options();
		
		options.addOption( "v", "verbose", 	false, "Increase verbosity" );
		options.addOption( "d", "debug", false, "Increase verbosity to debug lebel" );
		options.addOption( "f", "fixed-sized-messages", false, "Force fixed sized messages (default is to allow variable sizes)" );
		options.addOption( "h", "help", false, "Display this help text and exit" );
		
		options.addOption(
				OptionBuilder.withLongOpt( "port" )
				.withDescription( "Port to listen on (default ist 6867)" )
		        .hasArg()
		        .withArgName("PORT")
		        .create("p"));

		options.addOption(
				OptionBuilder.withLongOpt( "key-generation" )
				.withDescription( "Which key generation method is to be used by the participants." +
							"Following options are available:\n\tnull\n\tnormalDC\n\tworkCycleFailStop\n" + 
							"probabilisticWorkCycleFailStop; probabilistic fail stop is default.")
				.hasArg()
		        .withArgName("GMETHOD")
		        .create("g"));
		
		options.addOption(
				OptionBuilder.withLongOpt( "key-exchange" )
				.withDescription( "How should keys being exchanged between participants?" +
							"Following options are available:\n\tmanual\n\tfullyautomatic\n\t" + 
							"fully automatic is default.")
		        .hasArg()
				.withArgName("EMETHOD")
		        .create("e"));

//		options.addOption(
//				OptionBuilder.withLongOpt( "message-length" )
//				.withDescription( "Define the (max) length of a message. Max is only valid for variable sized messages. Default is 1024 bytes.")
//		        .hasArg()
//				.withArgName("MLENGTH")
//		        .create("l"));
		
		GnuParser parser = new GnuParser();
		
		try {
			CommandLine startArgs = parser.parse( options, args);

			// there are unknown options
			if (startArgs.getArgList().size() != 0){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "dcServerCLI", options );
				System.exit(-1);
			}
		
			// just take the default options
			if (startArgs.getOptions().length == 0){
				log.info("Starting the server using the default options. use --help to see available options");
			} else {
				
				// verbosity level
				if(startArgs.hasOption("v")){
					Logger.getRootLogger().setLevel(Level.INFO);
					log.info("Using INFO verbosity");
					
				}
				if(startArgs.hasOption("d")){
					Logger.getRootLogger().setLevel(Level.DEBUG);
					log.info("Using DEBUG verbosity");
				}
				
				// fixed message length
				if(startArgs.hasOption("f")){
					individualMessageLengths = WorkCycleManager.MESSAGE_LENGTHS_FIXED;
					log.info("Starting Server with fixed sized messages");
				}
				
				if(startArgs.hasOption("h")){
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp( "dcServerCLI", options );
					System.exit(-1);
				}
				
				// the port
				if(startArgs.hasOption("p")){
					listenPort = Integer.valueOf(startArgs.getOptionValue("p")).intValue();
					log.info("Listening on port " + startArgs.getOptionValue("p"));
				}
				
				// key generation
				if(startArgs.hasOption("g")){
					if (startArgs.getOptionValue("g").equalsIgnoreCase("null")){
						log.info("Using NULL-keys");
						keyGenerationMethod = KeyGenerator.KGMETHOD_NULL;
					} else if (startArgs.getOptionValue("g").equalsIgnoreCase("normalDC")){
						log.info("Using normal DC-keys");
						keyGenerationMethod = KeyGenerator.KGMETHOD_DC;
					} else if (startArgs.getOptionValue("g").equalsIgnoreCase("workCycleFailStop")){
						log.info("Using Fail-Stop-Keys within work cycles");
						keyGenerationMethod = KeyGenerator.KGMETHOD_DC_FAIL_STOP_WORK_CYCLE;
					} else if (startArgs.getOptionValue("g").equalsIgnoreCase("probabilisticWorkCycleFailStop")){
						log.info("Using Probabilistic-Fail-Stop-Keys within work cycles");
						if (!startArgs.hasOption("f"))
							log.warn("Warning! It is highly discurraged to use this key generation method with variable sized messages. Considder using -f.");
						keyGenerationMethod = KeyGenerator.KGMETHOD_PROBAB_FAIL_STOP;
					} else {
						log.error("Exit. Reason: Unknown key generation method:" + startArgs.getOptionValue("g"));
						System.exit(-1);
					}
				}

				// key exchange
				if (startArgs.hasOption("e")) {
					if (startArgs.getOptionValue("e").equalsIgnoreCase("manual")) {
						log.info("Keys have to be exchanged manually");
						keyExchangeMethod = KeyExchangeManager.KEX_MANUAL;
					} else if (startArgs.getOptionValue("e").equalsIgnoreCase(
							"fullyautomatic")) {
						log.info("Keys are exchanged fully automatic");
						keyExchangeMethod = KeyExchangeManager.KEX_FULLY_AUTOMATIC;
					} else {
						log.error("Exit. Reason: Unknown key exchange method:"
										+ startArgs.getOptionValue("e"));
						System.exit(-1);
					}
				}
			}
			
			server = new Server(listenPort, keyGenerationMethod,keyExchangeMethod,individualMessageLengths);
			t = new Thread(server, "Server");
			t.start();
			
			while(true){
				try {
					cmd = reader.readLine();
					
					if (cmd.equalsIgnoreCase("")){
						
					}
					else if (cmd.equalsIgnoreCase("h") || cmd.equalsIgnoreCase("help")){
						System.out.println("da :\t Display active participants");
						System.out.println("dp :\t Display all participants");
						System.out.println("dwc :\t Display current work cycle number");
						System.out.println("h, help:\t Show this help");
						System.out.println("q, quit:\t Quit the service");
					}

					else if (cmd.equalsIgnoreCase("q") || cmd.equalsIgnoreCase("quit")){
						log.info("Good bye!");
						System.exit(0);
					}

					else if (cmd.equalsIgnoreCase("da") || cmd.equalsIgnoreCase("dp")){
						log.warn("not implemented yet!");
					}
					else if (cmd.equalsIgnoreCase("dwc")){
						log.info(String.valueOf(server.getWorkCycleManager().getCurrentWorkCycleNumber()));
					}
					else {
						log.warn("unknown command!");
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
		
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "dcServerCLI", options );
		}
	}

}
