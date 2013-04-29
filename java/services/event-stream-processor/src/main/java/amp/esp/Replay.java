package amp.esp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import amp.esp.datastreams.ActiveRange;
import amp.esp.datastreams.Stream;
import amp.esp.datastreams.ValueStreams;
import amp.esp.monitors.ConsensusSearchDetector;
import amp.esp.monitors.CorrelateRequestResponsesEventDetector;
import amp.esp.monitors.DocumentCollectionWithHitFrequencySearchResultsDetector;
import amp.esp.monitors.EnvelopeCounter;
import amp.esp.monitors.EnvelopeLogger;
import amp.esp.monitors.EventTypeDetector;
import amp.esp.monitors.InferredEventCatcher;
import amp.esp.monitors.InferredEventPrinter;
import amp.esp.monitors.StorageRepository;
import amp.esp.monitors.UnauthorizedAccessAttemptsDetector;
import pegasus.eventbus.client.Envelope;

import com.google.common.collect.Lists;

/**
 * Utility program to test envelope handling by reading JSON versions of envelopes from
 * a text file (see {@link EnvelopeLogger} for an Event Monitor that saves traffic to
 * a file) and replays those envelopes.
 * 
 * @author israel
 *
 */
public class Replay {

    private static EventStreamProcessor esp;
    private static EnvelopeCounter monitor;

    /**
     * Read the contents from a text file.
     *
     * @return String consisting of the file's contents
     * @param fileName - the name of the file to read
     */
    public static String readTextFile(String filename) throws IOException {
        File f = new File(filename);
        BufferedReader r = null;
        char[] data;
        try {
            r = new BufferedReader(new FileReader(f));
            int size = (int) f.length();
            data = new char[size];
            int charsRead = 0;
            while (charsRead < size) {
                charsRead += r.read(data,charsRead,size - charsRead);
            }
        }
        catch (IOException e) {
            throw new IOException(e.getMessage()); }
        finally {
            try {
                if (r != null) { r.close(); }
            } catch (IOException ignore) {} }
        return new String(data);
    }

    public static void main(String[] args) throws IOException {
        setupESP();
        for (String infile : args) {
            replayFile(infile, esp);
        }
        monitor.displayStats();
    }

    private static void replayFile(String infile, EventStreamProcessor esp) throws IOException {
        String items = readTextFile(infile);
        String[] lines = items.split("\n");
        int count = 0;
        for (String line : lines) {
            count++;
            // instantiate an envelope from JSON version
            Envelope env = EnvelopeUtils.fromJson(line);
            // TODO: use timestamps in envelope to reproduce delays
            // send envelope into ESP
            esp.sendEvent(env);
            // TODO: add capability to put envelope onto Event Bus instead of directly into ESP
        }
        System.out.println(count + " envelopes processed.");
    }

    private static void setupESP() {
        StorageRepository monitors = new StorageRepository();
        monitor = new EnvelopeCounter();
        monitors.addMonitor(monitor);
        esp = new EventStreamProcessor(monitors);
    }
}

