/* 
 * <Paste your header here>
 */
package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class RoutingTable
{

    private final Set<RoutingTableEntry> entries = new HashSet<>();

    public Set<RoutingTableEntry> getEntries() {
        return new HashSet<>( entries);
    }

    public synchronized void addEntry(RoutingTableEntry entry) {
        List<RoutingTableEntry> duplicates = this.entries.stream()
                                                         .filter(e -> e.getAddress().equals(entry.getAddress()))
                                                         .collect( Collectors.toList());

        if (duplicates.size() == 0) {
            this.entries.add(entry);
        } else if (duplicates.stream().filter(e -> e.getNodeId() == entry.getNodeId()).count() == 1) {
        } else {
            // We have an erroneous entry. Correct it.
            RoutingTableEntry e = duplicates.get(0);
            e.setNodeId(entry.getNodeId());
        }
    }

    public synchronized boolean removeEntry(RoutingTableEntry e) {
        if (this.entries.remove(e)) {

            return true;
        }

        return false;
    }

    public synchronized boolean removeEntry( InetSocketAddress node) {
        Optional<RoutingTableEntry> entry = this.entries.stream()
                                                        .filter(e -> e.getAddress().getAddress().equals(node.getAddress()) &&
                        e.getAddress().getPort() == node.getPort())
                                                        .findFirst();

        if (entry.isPresent()) {
            this.entries.remove(entry.get());
            return true;
        }

        return false;
    }

    /**
     * Removes all the entries in the routing table and clears it.
     */
    public synchronized void clear() {
        this.entries.clear();
    }

    /**
     * Finds the {@link InetSocketAddress} of a given node. Searched by the
     *
     * @param nodeId ID of the node of which IP-port info is required to be found
     * @return Optional of {@link InetSocketAddress}
     */
    public Optional<RoutingTableEntry> findByNodeId( int nodeId) {
        return this.entries.stream()
                .filter(e -> e.getNodeId() == nodeId)
                .findFirst();
    }

    /**
     * Finds the routing table entry corresponding to the nodeId. The entry can be the exact node or the successor of
     * that node.
     *
     * @param nodeId Node name to be found in the routing table
     * @return optional of entry
     */
    public Optional<RoutingTableEntry> findNodeOrSuccessor( int nodeId) {
        List<RoutingTableEntry> sortedEntries = this.entries.stream()
                                                            .sorted( Comparator.comparingInt( RoutingTableEntry::getNodeId))
                                                            .collect( Collectors.toList());

        Optional<RoutingTableEntry> successor = sortedEntries.stream()
                                                             .filter(e -> e.getNodeId() >= nodeId)
                                                             .findFirst();

        if (successor.isPresent()) {
            return successor;
        } else if (sortedEntries.size() > 0) {
            return Optional.of( sortedEntries.get( 0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Finds the successor of a given node
     *
     * @param nodeId ID of the node of which we want to find the successor
     * @return Optional
     */
    public Optional<RoutingTableEntry> findSuccessorOf( int nodeId) {
        List<RoutingTableEntry> sortedEntries = this.entries.stream()
                                                            .sorted( Comparator.comparingInt( RoutingTableEntry::getNodeId))
                                                            .collect( Collectors.toList());

        Optional<RoutingTableEntry> successor = sortedEntries.stream()
                                                             .filter(e -> e.getNodeId() > nodeId)
                                                             .findFirst();

        if (successor.isPresent() && successor.get().getNodeId() != nodeId) {
            return successor;
        } else if (sortedEntries.size() > 0 && sortedEntries.get(0).getNodeId() != nodeId) {
            return Optional.of( sortedEntries.get( 0));
        }

        return Optional.empty();
    }

    /**
     * Finds the predecessor of a given node
     *
     * @param nodeId ID of the node of which we want to find the successor
     * @return Optional
     */
    public Optional<RoutingTableEntry> findPredecessorOf( int nodeId) {
        List<RoutingTableEntry> sortedEntries = this.entries.stream()
                                                            .sorted( Comparator.comparingInt( e -> ((RoutingTableEntry) e).getNodeId()).reversed())
                                                            .collect( Collectors.toList());

        Optional<RoutingTableEntry> predecessor = sortedEntries.stream()
                                                               .filter(e -> e.getNodeId() < nodeId)
                                                               .findFirst();

        if (predecessor.isPresent() && predecessor.get().getNodeId() != nodeId) {
            return predecessor;
        } else if (sortedEntries.size() > 0 && sortedEntries.get(0).getNodeId() != nodeId) {
            return Optional.of( sortedEntries.get( 0));
        }

        return Optional.empty();
    }

}
