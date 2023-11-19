package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Node
{
    private static final Logger logger = LoggerFactory.getLogger( Node.class );

    private NodeState state = NodeState.IDLE;

    private final String username;
    private final String ipAddress;
    private final int port;
    private int nodeId;
    private final RoutingTable routingTable = new RoutingTable();

    private final NodeServer server;
    private final Communicator communicationProvider;
    private ScheduledExecutorService executorService;

    private BootstrapConnector bootstrapProvider = new BootstrapConnector();

    public Node( int port )
    {
        this( port, new Communicator(), new NodeServer( port ) );
    }

    public Node( int port, Communicator communicationProvider, NodeServer server )
    {
        this( port, "localhost", communicationProvider, server );
    }

    public Node( int port, String ipAddress, Communicator communicationProvider, NodeServer server )
    {
        this( port, ipAddress, UUID.randomUUID().toString(), communicationProvider, server );
    }

    public Node( int port, String ipAddress, String username, Communicator communicationProvider, NodeServer server )
    {
        this.port = port;
        this.ipAddress = ipAddress;
        this.username = username;
        this.communicationProvider = communicationProvider;
        this.server = server;
    }

    public void start()
    {

        Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );

        executorService = Executors.newScheduledThreadPool( 2 );
        server.start( this );
        communicationProvider.start( this );


        logger.debug( "Connecting to the distributed network" );
        //        while (!stateManager.isState(CONNECTING)) {
        //            if (stateManager.isState(REGISTERED)) {
        //                unregister();
        //            }

        List<InetSocketAddress> peers = register();

        //            if (stateManager.isState(REGISTERED)) {
        Set<RoutingTableEntry> entries = connect( peers );
        // peer size become 0 only when we registered successfully
        if( peers.size() == 0 || entries.size() > 0 )
        {
            this.updateRoutingTable( entries );
            //                    stateManager.setState(CONNECTING);
            logger.info( "Successfully connected to the network and created routing table" );
        }


        // 1. Select a Node Name
        this.nodeId = selectNodeName();
        logger.info( "Selected node ID -> {}", this.nodeId );

        // 2. Add my node to my routing table
        routingTable.addEntry( new RoutingTableEntry( new InetSocketAddress( ipAddress, port ), this.nodeId ) );
        logger.info( "My routing table is -> {}", routingTable.getEntries() );
        state = ( NodeState.CONNECTED );


        configure();


        // TODO: 10/24/17 Periodic synchronization
        /*
         * 1. Find 2 predecessors of mine.
         * 2. Then periodically ping them and synchronize with their entry tables.
         */
//        periodicTask = executorService.scheduleAtFixedRate( () ->
//
//                                                            {
//                                                                try
//                                                                {
//                                                                    runPeriodically();
//                                                                }
//                                                                catch( Exception e )
//                                                                {
//                                                                    logger.error( "Error occurred when running periodic check", e );
//                                                                }
//                                                            }, Constants.HEARTBEAT_INITIAL_DELAY, Constants.HEARTBEAT_FREQUENCY_MS, TimeUnit.MILLISECONDS );
    }

    /**
     * Register and fetch 2 random peers from Bootstrap Server. Also retries until registration becomes successful.
     *
     * @return peers sent from Bootstrap server
     */
    private List<InetSocketAddress> register()
    {
        logger.debug( "Registering node" );
        List<InetSocketAddress> peers = null;
        try
        {
            peers = bootstrapProvider.register( ipAddress, port, username );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when registering node", e );
        }

        if( peers == null )
        {
            logger.warn( "Peers are null" );
        }
        else
        {
            state = NodeState.REGISTERED;
            logger.info( "Node ({}:{}) registered successfully. Peers -> {}", ipAddress, port, peers );
        }

        return peers;
    }


    private void configure()
    {
        // Do some specific work here
    }

    private void runPeriodically()
    {
        /*
           ping to other Nodes
           Synchronize Map
         */

    }


    /**
     * Connect to the peers send by BS and fetch their routing tables. This method will later be reused for
     * synchronization purposes.
     *
     * @param peers peers to be connected
     * @return true if connecting successful and got at least one entry
     */
    private Set<RoutingTableEntry> connect( List<InetSocketAddress> peers )
    {
        logger.debug( "Collecting routing table from peers: {}", peers );
        Set<RoutingTableEntry> entries = new HashSet<>();
        for( InetSocketAddress peer : peers )
        {
            Set<RoutingTableEntry> received = communicationProvider.connect( peer );
            logger.debug( "Received routing table: {} from -> {}", received, peer );
            if( received.size() == 0 )
            {
                logger.error( "Failed to obtain routing table from -> {}", peer );
                entries.clear();
                break;
            }

            entries.addAll( received );
        }
        return entries;
    }

    /**
     * Unregister
     */
    private void unregister()
    {
        try
        {
            bootstrapProvider.unregister( ipAddress, port, username );
           state = ( NodeState.IDLE );
            logger.debug( "Unregistered from Bootstrap Server" );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when unregistering", e );
        }
    }

    /**
     * Selects a Node Name for the newly connected node (this one). When selecting, we chose a random node name within
     * <strong>1 - 180</strong> which maps from <strong>[A-Z0-9] -> [1-180]</strong>.
     *
     * @return The selected node name
     */
    private int selectNodeName()
    {
        Set<Integer> usedNodes = this.routingTable.getEntries().stream()
                                                  .map( entry -> entry.getNodeId() / Constants.ADDRESSES_PER_CHARACTER )
                                                  .collect( Collectors.toSet() );

        Random random = new Random();
        // We can allow up to 36 Nodes in our network this way.
        while( true )
        {
            int candidate = 1 + random.nextInt( Constants.ADDRESS_SPACE_SIZE );
            if( !usedNodes.contains( candidate ) )
            {
                return candidate;
            }
        }
    }



    /**
     * Updates the {@link #routingTable} with entries coming from another node's routing table
     *
     * @param entries routing table entries received from another node
     */
    public void updateRoutingTable( Set<RoutingTableEntry> entries )
    {
        logger.debug( "Adding routing table entries -> {}", entries );
        // TODO: 11/2/17 Should we sync? Remove what is not present?
        entries.forEach( routingTable::addEntry );
    }

    public void removeNode(InetSocketAddress node) {
        logger.warn("Attempting to remove routing table entry -> {} from routing table", node);
        this.routingTable.removeEntry(node);
    }


    public void stop()
    {
        // TODO: graceful departure
        logger.debug( "Stopping node" );
//        if( stateManager.getState().compareTo( REGISTERED ) >= 0 )
//        {
//
//            if( stateManager.getState().compareTo( CONNECTED ) >= 0 )
//            {
//                // TODO: 10/21/17 Notify all the indexed nodes that I'm leaving
//                // TODO: 10/20/17 Should we disconnect from the peers or all entries in the routing table?
//                this.routingTable.getEntries().forEach( entry -> {
//                    if( communicationProvider.disconnect( entry.getAddress() ) )
//                    {
//                        logger.debug( "Successfully disconnected from {}", entry );
//                    }
//                    else
//                    {
//                        logger.warn( "Unable to disconnect from {}", entry );
//                    }
//                } );
//
//                this.routingTable.clear();
//                this.myFiles.clear();
//                this.entryTable.clear();
//                stateManager.setState( REGISTERED );
//            }

            unregister();
//        }

        communicationProvider.stop();
        server.stop();


        logger.debug( "Shutting down periodic tasks" );

        executorService.shutdownNow();
        try
        {
            executorService.awaitTermination( Constants.GRACE_PERIOD_MS, TimeUnit.MILLISECONDS );
        }
        catch( InterruptedException e )
        {
            executorService.shutdownNow();
        }

        state=NodeState.IDLE;
        logger.info( "Distributed node stopped" );
    }

    public int getPort()
    {
        return port;
    }

    public String getUsername()
    {
        return username;
    }


    public String getIpAddress()
    {
        return ipAddress;
    }

    public RoutingTable getRoutingTable()
    {
        return routingTable;
    }



    public NodeState getState()
    {
        return state;
    }

    public Communicator getCommunicationProvider()
    {
        return communicationProvider;
    }

    public int getNodeId()
    {
        return nodeId;
    }


}
