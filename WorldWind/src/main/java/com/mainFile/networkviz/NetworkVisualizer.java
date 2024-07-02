package com.mainFile.networkviz;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.geojson.GeoJSONDoc;
import gov.nasa.worldwind.formats.geojson.GeoJSONObject;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.GeoJSONLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.io.File;


public class NetworkVisualizer extends ApplicationTemplate {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private AppFrame appFrame;
    private int updateIntervalInSeconds = 60; // Default update interval is 60 seconds
    private javax.swing.Timer timer;
    private long startTime;
    private RenderableLayer mainLayer;

    public void launchApplication() {
        appFrame = start("World Wind JSON Network Viewer", AppFrame.class);

        // Reconfigure the size of the World Window to take up the space typically used by the layer panel
        Dimension dimension = new Dimension(1400, 800);
        appFrame.setPreferredSize(dimension);
        appFrame.pack();
        WWUtil.alignComponent(null, appFrame, AVKey.CENTER);

        addMenusToFrame();

        // Start the timer with the default update interval
        startTime = System.currentTimeMillis();
        stopTimer();
    }

    private void addMenusToFrame() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openFileMenuItem = makeOpenFileMenu();
        JMenuItem liveDataMenuItem = makeLiveDataMenu();
        JMenuItem updateIntervalMenu = makeUpdateIntervalMenu();


        appFrame.setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(openFileMenuItem);
        fileMenu.add(liveDataMenuItem);
        fileMenu.add(updateIntervalMenu);

    }
    // Uploads and makes JSON files readable
    private JMenuItem makeOpenFileMenu() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON File", "json", "json"));

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...") {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int status = fileChooser.showOpenDialog(appFrame);
                    if (status == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();

                        GeoJSONDoc doc = new GeoJSONDoc(selectedFile);
                        doc.parse();

                        if (doc.getRootObject() instanceof GeoJSONObject) {
                            GeoJSONObject rootObject = (GeoJSONObject) doc.getRootObject();

                            // Create a layer
                            RenderableLayer layer = new RenderableLayer();

                            // Names layer
                            layer.setName("JSON upload");

                            // Add the network graph to the layer
                            GeoJSONLoader loader = new GeoJSONLoader();
                            loader.addGeoJSONGeometryToLayer(rootObject, layer);

                            // Add the layer to the WorldWind model
                            appFrame.getWwd().getModel().getLayers().add(layer);
                          
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error loading JSON file: {}", e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error loading JSON upload:\n", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return openFileMenuItem;
    }
    // Creates menu for live JSON data
    private JMenuItem makeLiveDataMenu() {
        JMenuItem liveDataMenuItem = new JMenuItem(new AbstractAction("Live Data URL...") {
            public void actionPerformed(ActionEvent actionEvent) {
                String defaultUrl ="https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson";
                String url = javax.swing.JOptionPane.showInputDialog("Enter the URL for live JSON feed: ", defaultUrl);
                if (url != null && !url.isEmpty()) {
                    // Parse the live JSON feed
                    try {

                            GeoJSONDoc doc = new GeoJSONDoc(new URL(url));
                            doc.parse();

                        if (doc.getRootObject() instanceof GeoJSONObject) {
                            GeoJSONObject rootObject = (GeoJSONObject) doc.getRootObject();

                            // Create a layer
                            mainLayer = new RenderableLayer();

                            // Add the earthquake data to the layer
                            GeoJSONLoader loader = new GeoJSONLoader();
                            loader.addGeoJSONGeometryToLayer(rootObject, mainLayer);

                            // Set a custom name for the layer
                            long elapsedTime = (System.currentTimeMillis() - startTime) / (1000 * 60);
                            mainLayer.setName("Earthquake Data - " + elapsedTime + " Minutes");

                            // Add the layer to the WorldWind model
                            appFrame.getWwd().getModel().getLayers().add(mainLayer);

                        }
                    } catch (Exception e) {
                        logger.error("Error loading live JSON feed: {}", e.getMessage());
                        JOptionPane.showMessageDialog(null, "Error loading live JSON feed:\n", "Error", JOptionPane.ERROR_MESSAGE);

                    }
                }else{
                    JOptionPane.showMessageDialog(null, "Error loading live JSON feed:\n", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return liveDataMenuItem;
    }
    // Sets interval to time chosen
    private JMenuItem makeUpdateIntervalMenu() {
        JMenu intervalMenu = new JMenu("Update Interval");
        ButtonGroup intervalGroup = new ButtonGroup();

        JRadioButtonMenuItem item1 = new JRadioButtonMenuItem("1 Second");
        item1.addActionListener(e -> setUpdateInterval(1));
        intervalGroup.add(item1);
        intervalMenu.add(item1);

        JRadioButtonMenuItem item2 = new JRadioButtonMenuItem("5 Seconds");
        item2.addActionListener(e -> setUpdateInterval(5));
        intervalGroup.add(item2);
        intervalMenu.add(item2);

        JRadioButtonMenuItem item3 = new JRadioButtonMenuItem("10 Seconds");
        item3.addActionListener(e -> setUpdateInterval(10));
        intervalGroup.add(item3);
        intervalMenu.add(item3);

        JRadioButtonMenuItem item4 = new JRadioButtonMenuItem("30 Seconds");
        item4.addActionListener(e -> setUpdateInterval(30));
        intervalGroup.add(item4);
        intervalMenu.add(item4);

        JRadioButtonMenuItem item5 = new JRadioButtonMenuItem("1 Minute");
        item5.addActionListener(e -> setUpdateInterval(60));
        intervalGroup.add(item5);
        intervalMenu.add(item5);

        JRadioButtonMenuItem item6 = new JRadioButtonMenuItem("5 Minutes");
        item6.addActionListener(e -> setUpdateInterval(300));
        intervalGroup.add(item6);
        intervalMenu.add(item6);

        JRadioButtonMenuItem item7 = new JRadioButtonMenuItem("STOP TIMER");
        item7.addActionListener(e -> stopTimer());
        intervalGroup.add(item7);
        intervalMenu.add(item7);


        // Set the default selected interval
        item7.setSelected(true);

        return intervalMenu;
    }



    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }

        timer = new javax.swing.Timer(updateIntervalInSeconds * 1000, e -> queryLiveStream());
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void setUpdateInterval(int seconds) {
        updateIntervalInSeconds = seconds;
        startTimer();
    }
    // Query that creates new layer per interval chosen
    private void queryLiveStream() {
        try {
            GeoJSONDoc doc = new GeoJSONDoc(new URL("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson"));
            doc.parse();
            System.out.println("UPDATING LIVE STREAM");
            if (doc.getRootObject() instanceof GeoJSONObject) {
                GeoJSONObject rootObject = (GeoJSONObject) doc.getRootObject();

                // Clear existing data from mainLayer
                mainLayer.removeAllRenderables();

                // Add the earthquake data to the layer
                GeoJSONLoader loader = new GeoJSONLoader();
                loader.addGeoJSONGeometryToLayer(rootObject, mainLayer);

                // Set a custom name for the layer
                long elapsedTime = (System.currentTimeMillis() - startTime) / (1000 * 60);
                mainLayer.setName("Earthquake Data - " + elapsedTime + " Minutes");

                // Update the display
                appFrame.getWwd().redraw();
            }
        } catch (Exception e) {
            logger.error("Error loading live JSON feed: {}", e.getMessage());
        }
    }




    public static void main(String[] args) {
        NetworkVisualizer networkVisualizer = new NetworkVisualizer();
        networkVisualizer.launchApplication();
    }
}
