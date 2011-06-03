package de.tu.dresden.dud.dc.ServerCLI;

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
		Logger log = Logger.getLogger(Server.class);

	    int 	listenPort					= Connection.DEFAULTPORT;
	    short 	keyGenerationMethod			= KeyGenerator.KGMETHOD_PROBAB_FAIL_STOP; 
	    short 	keyExchangeMethod			= KeyExchangeManager.KEX_FULLY_AUTOMATIC;
	    short 	individualMessageLengths	= WorkCycleManager.MESSAGE_LENGTHS_VARIABLE;
				
	    Server  server = null;
		
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
			CommandLine cmd = parser.parse( options, args);

			// there are unknown options
			if (cmd.getArgList().size() != 0){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "dcServerCLI", options );
				return;
			}
		
			// just take the default options
			if (cmd.getOptions().length == 0){
				log.info("Starting the server using the default options. use --help to see available options");
			} else {
				
				// verbosity level
				if(cmd.hasOption("v")){
					Logger.getRootLogger().setLevel(Level.INFO);
					log.info("Using INFO verbosity");
					
				}
				if(cmd.hasOption("d")){
					Logger.getRootLogger().setLevel(Level.DEBUG);
					log.info("Using DEBUG verbosity");
				}
				
				// fixed message length
				if(cmd.hasOption("f")){
					individualMessageLengths = WorkCycleManager.MESSAGE_LENGTHS_FIXED;
					log.info("Starting Server with fixed sized messages");
				}
				
				if(cmd.hasOption("h")){
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp( "dcServerCLI", options );
					return;
				}
				
				// the port
				if(cmd.hasOption("p")){
					listenPort = Integer.valueOf(cmd.getOptionValue("p")).intValue();
					log.info("Listening on port " + cmd.getOptionValue("p"));
				}
				
				// key generation
				if(cmd.hasOption("g")){
					if (cmd.getOptionValue("g").equalsIgnoreCase("null")){
						log.info("Using NULL-keys");
						keyGenerationMethod = KeyGenerator.KGMETHOD_NULL;
					} else if (cmd.getOptionValue("g").equalsIgnoreCase("normalDC")){
						log.info("Using normal DC-keys");
						keyGenerationMethod = KeyGenerator.KGMETHOD_DC;
					} else if (cmd.getOptionValue("g").equalsIgnoreCase("workCycleFailStop")){
						log.info("Using Fail-Stop-Keys within work cycles");
						keyGenerationMethod = KeyGenerator.KGMETHOD_DC_FAIL_STOP_WORK_CYCLE;
					} else if (cmd.getOptionValue("g").equalsIgnoreCase("probabilisticWorkCycleFailStop")){
						log.info("Using Probabilistic-Fail-Stop-Keys within work cycles");
						if (!cmd.hasOption("f"))
							log.warn("Warning! It is highly discurraged to use this key generation method with variable sized messages. Considder using -f.");
						keyGenerationMethod = KeyGenerator.KGMETHOD_PROBAB_FAIL_STOP;
					} else {
						log.error("Exit. Reason: Unknown key generation method:" + cmd.getOptionValue("g"));
						return;
					}
				}

				// key exchange
				if (cmd.hasOption("e")) {
					if (cmd.getOptionValue("e").equalsIgnoreCase("manual")) {
						log.info("Keys have to be exchanged manually");
						keyExchangeMethod = KeyExchangeManager.KEX_MANUAL;
					} else if (cmd.getOptionValue("e").equalsIgnoreCase(
							"fullyautomatic")) {
						log.info("Keys are exchanged fully automatic");
						keyExchangeMethod = KeyExchangeManager.KEX_FULLY_AUTOMATIC;
					} else {
						log.error("Exit. Reason: Unknown key exchange method:"
										+ cmd.getOptionValue("e"));
						return;
					}
				}
			}
			
			server = new Server(listenPort, keyGenerationMethod,keyExchangeMethod,individualMessageLengths);
			new Thread(server, "Server").start();
		
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "dcServerCLI", options );
		}
	}

}
