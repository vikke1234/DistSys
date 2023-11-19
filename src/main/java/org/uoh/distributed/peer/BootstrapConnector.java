/*
 * <Paste your header here>
 */
package org.uoh.distributed.peer;

import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;


/**
 * Connects to bootstrap server and register using UDP packets
 *
 * @author Imesha Sudasingha
 */
public class BootstrapConnector
{


    private final int numOfRetries = 3;

    public BootstrapConnector()
    {
    }


    public List<InetSocketAddress> register( String ipAddress, int port, String username ) throws SocketException
    {
        String msg = String.format( Constants.REG_MSG_FORMAT, ipAddress, port, username );
        String request = RequestBuilder.buildRequest( msg );

        int retriesLeft = numOfRetries;
        while( retriesLeft > 0 )
        {
            try (DatagramSocket datagramSocket = new DatagramSocket())
            {
                String response = RequestBuilder.sendRequest( datagramSocket, request, InetAddress.getByName( Constants.BOOTSTRAP_IP ), Constants.BOOTSTRAP_PORT );
                return RequestBuilder.processRegisterResponse( response );
            }
            catch( IOException e )
            {
                e.printStackTrace();
                retriesLeft--;
            }
        }

        return null;
    }

    public boolean unregister( String ipAddress, int port, String username ) throws SocketException
    {
        String msg = String.format( Constants.UNREG_MSG_FORMAT, ipAddress, port, username );
        String request = RequestBuilder.buildRequest( msg );

        int retriesLeft = numOfRetries;
        while( retriesLeft > 0 )
        {
            try (DatagramSocket datagramSocket = new DatagramSocket())
            {
                String response = RequestBuilder.sendRequest( datagramSocket, request, InetAddress.getByName( Constants.BOOTSTRAP_IP ), Constants.BOOTSTRAP_PORT );
                if( RequestBuilder.processUnregisterResponse( response ) )
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch( IOException e )
            {
                retriesLeft--;
                e.printStackTrace();
            }
        }

        return false;
    }

}
