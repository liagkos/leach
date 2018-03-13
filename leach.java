// Implementation of LEACH algorithm
// (c) 2017-2018 Liagkos Athanasios
// nasos.liagos@gmail.com
// MIT License

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.concurrent.ThreadLocalRandom;

public class Leach {

    // Node attributes
    private static class nodeDetails {
        private double  possibility;
        private int     counter;
        private boolean selected;
        private boolean valid;
    }

    // Parse nodelist and prepare results to display
    private static List<List<String>> parseNodeList(List<nodeDetails> nodeList, int nodes) {
        List<List<String>> parsed   = new ArrayList<>(4);
        ArrayList<String> below     = new ArrayList<>();
        ArrayList<String> excluded  = new ArrayList<>();
        ArrayList<String> round     = new ArrayList<>();
        ArrayList<String> valid     = new ArrayList<>();

        for(int node=0; node<nodes; node++) {
            nodeDetails current = nodeList.get(node);
            if(!current.valid) {
                below.add("N" + (node + 1));
            }
            if(current.valid) {
                round.add("N" + (node + 1));
            }
            if(!current.valid || !current.selected) {
                excluded.add("N" + (node + 1));
            }
            if(current.selected) {
                valid.add("N" + (node + 1));
            }
        }

        parsed.add(below);
        parsed.add(round);
        parsed.add(excluded);
        parsed.add(valid);

        // index 0 = Nodes below limit, index 1 = Nodes potentially valid,
        // index 2 = Nodes excluded, index 3 = Nodes valid clusterheads
        return parsed;
    }
    public static void main(String args[]) {
        int     node, nodes, round, rounds;
        double  theta, possibility=0;

        Scanner keyboard = new Scanner(System.in);
        System.out.print("Number of nodes (int): ");
        nodes = keyboard.nextInt();
        System.out.print("Number of rounds (int): ");
        rounds = keyboard.nextInt();
        System.out.print("Possibility (p) (double): ");
        try {
            possibility = keyboard.nextDouble();
        } catch (InputMismatchException msg) {
            System.out.println("Exception: Use the correct decimal delimiter!");
            System.exit(1);
        }

        // nodelist array holding all properties of nodes
        // for all rounds
        List<List<nodeDetails>> nodeList = new ArrayList<>(nodes);

        // Initialize array with random and default values
        for(round=0; round<rounds; round++) {
            ArrayList<nodeDetails> nodeDetailsPerNode = new ArrayList<nodeDetails>(rounds);
            for(node=0; node<nodes; node++) {
                nodeDetails current = new nodeDetails();
                current.possibility = ThreadLocalRandom.current().nextDouble(0, 1);
                
                // Node default values
                current.counter = 0;
                current.selected = false;
                current.valid = false;
                nodeDetailsPerNode.add(current);
            }
            nodeList.add(nodeDetailsPerNode);
        }

        // Run LEACH algorithm on array
        for(round=0; round<rounds; round++) {
            theta = possibility / (1 - possibility * ((round + 1) % (int) (1 / possibility)));
            for(node=0; node<nodes; node++) {
                nodeDetails current = nodeList.get(round).get(node);

                // Take care for 1st round in order to get previous'
                // round node details. If round=1 then previous=current
                // else if round>1 previous=current-1
                int roundIdentifier = round > 0 ? 1 : 0;
                nodeDetails previous = nodeList.get(round - roundIdentifier).get(node);

                // Set counter to previous counter from previous round
                current.counter = previous.counter;

                // Obey the algorithm rule and set theta=0
                // if node does not belong to the group of
                // potentially selected nodes
                //
                // This program could work without this rule
                // but it was added to show the correct flow
                // of the algorithm
                if(current.counter < (int) (1 / possibility) && current.counter > 0) {
                    theta = 0;
                }

                // Compare current possibility with theta
                // If node p is <= theta, set it to potentially valid
                // (property valid=true)
                if(current.possibility <= theta) {
                    // If node has never been used (counter=0) or
                    // node was used as CH at least 1/p rounds in the past
                    // then reset the counter to 1 and set the node
                    // to new CH, else just increase the counter
                    if(current.counter > (int) (1 / possibility) || current.counter == 0) {
                        current.counter = 1;
                        current.selected = true;
                    } else {
                        current.counter++;
                    }
                    current.valid = true;
                } else {
                    // Node p > theta, if the node has been CH
                    // in the past (counter>0), just increase the counter
                    // otherwise leave the counter to zero
                    if(current.counter > 0 ) {
                        current.counter++;
                    }
                }
            }
        }

        // Display results
        // Call parseNodeList method and beautify data
        for(round=0; round<rounds; round++) {
            theta = possibility / (1 - possibility * ((round + 1) % (int) (1 / possibility)));
            System.out.println("\nRound " + (round + 1) + ", theta = " + String.format("%.6f", theta));
            System.out.println("-----------------------------------------------------");
            for (node = 0; node < nodes; node++) {
                nodeDetails current = nodeList.get(round).get(node);
                System.out.print("N" + String.format("%-5d", node + 1) + ": ");
                System.out.print(String.format("%.6f", current.possibility));
                System.out.print("\t(Counted " + current.counter + " time(s))");
                System.out.println((current.selected ? "\t<--- Clusterhead" : ""));
            }
            List<List<String>> parsed = parseNodeList(nodeList.get(round), nodes);
            System.out.println("Nodes below limit        : " + parsed.get(0));
            System.out.println("Nodes potentially valid  : " + parsed.get(1));
            System.out.println("Nodes excluded           : " + parsed.get(2));
            System.out.println("Nodes valid clusterheads : " + parsed.get(3));
        }
    }
}
