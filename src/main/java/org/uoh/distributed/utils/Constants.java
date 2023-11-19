package org.uoh.distributed.utils;

public class Constants
{


  /** Need to set the bootstrap IP and Port here */
  public static int BOOTSTRAP_PORT = 55555;
  public static String BOOTSTRAP_IP = "127.0.0.1";

  /** Message format to be used when sending a request to the bootstrap server. ${length} ${msg} */
  public static final String MSG_SEPARATOR = "~";
  public static final String MSG_FORMAT = "%04d %s";
  public static final String REG = "REG";
  public static final String UNREG = "UNREG";
  public static final String REGOK = "REGOK";
  public static final String UNREGOK = "UNREGOK";
  public static final String ECHO = "ECHO";
  public static final String ECHOOK = "ECHOOK";

  /** REG ${ip} ${port} ${username} */
  public static final String REG_MSG_FORMAT = "REG~%s~%d~%s";
  /** UNREG ${ip} ${port} ${username} */
  public static final String UNREG_MSG_FORMAT = "UNREG~%s~%d~%s";
  /** NEWNODE ${ip} ${port} ${nodeId} */
  public static final String NEWNODE_MSG_FORMAT = "NEWNODE~%s~%d~%d";
  /** PING - Pings and gets the entries of the corresponding node */
  public static final String PING_MSG_FORMAT = "PING~%d~%s";
  /** SYNC ${type} ${serialized_object} - For syncing table entries and routing tables */
  public static final String SYNC_MSG_FORMAT = "SYNC~%s~%s";


  /** Status Codes **/
  public static final int E0000 = 0;    // No nodes in the network
  public static final int E0001 = 1;    // 1 node in the network
  public static final int E0002 = 2;    // 2 nodes in the network
  public static final int E0003 = 3;    // 3 nodes in the network
  public static final int E9999 = 9999; // Error in command
  public static final int E9998 = 9998; // Already registered
  public static final int E9997 = 9997; // Port not available
  public static final int E9996 = 9996; // Network Full

  /** Types of syncs */
  public static final String TYPE_ROUTING = "RTBL";
  public static final String TYPE_ENTRIES = "ETBL";

  /** Message commands to be used in client server communications **/
  public static final String GET_ROUTING_TABLE = "GETRTBL";
  public static final String NEW_NODE = "NEWNODE";
  public static final String QUERY = "QUERY";
  public static final String KEYWORD = "KEYWORD";
  public static final String NEW_ENTRY = "NEWENTRY";
  public static final String RESPONSE_OK = "OK";
  public static final String RESPONSE_FAILURE = "FAILED";
  /** SYNC - sync the entry table entries by handing over anything that should belong to that node */
  public static final String SYNC = "SYNC";
  public static final String PING = "PING";



  /** How many times a given UDP request be retried */
  public static final int RETRIES_COUNT = 5;
  public static final int RETRY_TIMEOUT_MS = 5000;
  public static final int GRACE_PERIOD_MS = 5000;
  public static final int HEARTBEAT_FREQUENCY_MS = 20000;
  public static final int HEARTBEAT_INITIAL_DELAY = 30000;


  public static final int ADDRESS_SPACE_SIZE = 36;
  public static final int CHARACTER_SPACE_SIZE = 36;
  public static final int ADDRESSES_PER_CHARACTER = ADDRESS_SPACE_SIZE / CHARACTER_SPACE_SIZE;

}
