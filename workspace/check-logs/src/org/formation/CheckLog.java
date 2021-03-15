package org.formation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckLog {

	public static void main(String[] args) throws URISyntaxException {
		
		try {

            File f = new File(args[0]);

            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = "";

            Map<Integer,List<Integer>> partitionsOffsets = new HashMap<>();
            while ((readLine = b.readLine()) != null) {
            	try {
                String [] data = readLine.split(";");
                int partition =Integer.parseInt(data[1]);
                List<Integer> offsets = partitionsOffsets.get(partition);
                if ( offsets == null ) {
                	offsets = new ArrayList<>();
                	partitionsOffsets.put(partition, offsets);
                }
                offsets.add(Integer.parseInt(data[2]));
            	} catch (Exception e) {
            		System.err.println(readLine + " - " + e);
            	}
            }
            b.close();
            
            Map<Integer,Integer> minOffsets = new HashMap<>();
            Map<Integer,Integer> maxOffsets = new HashMap<>();
            for (int partition : partitionsOffsets.keySet()) {
            	minOffsets.put(partition, Collections.min(partitionsOffsets.get(partition)));
            	maxOffsets.put(partition, Collections.max(partitionsOffsets.get(partition)));
            }
            
            List<String> missedOffsets = new ArrayList<>();
            List<String> doublonOffsets = new ArrayList<>();
            for (int partition : partitionsOffsets.keySet()) {
            	for ( int i = minOffsets.get(partition); i< maxOffsets.get(partition); i++) {
            		int count = 0;
            		for ( int offset : partitionsOffsets.get(partition) ) {
            			if ( offset == i) {
            				count++;
            			}
            		}
            		if ( count == 0) {
            			missedOffsets.add(partition +"/"+i);
            		} else if ( count > 1) {
            			doublonOffsets.add(partition +"/"+i);
            		}
            	}
            }
            System.out.println("Min offset : "+minOffsets);
            System.out.println("Max offset : "+maxOffsets);

            System.out.println("Missed offset : "+missedOffsets);
            System.out.println("Doublon offset : "+doublonOffsets);
            

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
