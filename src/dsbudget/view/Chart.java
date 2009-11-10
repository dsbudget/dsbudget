package dsbudget.view;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import dsbudget.model.Category;

//sample and doc  
//http://www.screaming-penguin.com/node/4005

public class Chart {
	public static void generatePieChart(OutputStream out) { 
		
        // Create a simple pie chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        pieDataset.setValue("A", new Integer(75));
        pieDataset.setValue("B", new Integer(10));
        pieDataset.setValue("C", new Integer(10));
        pieDataset.setValue("D", new Integer(5));
        JFreeChart chart = ChartFactory.createPieChart3D(
               "CSC408 Mark Distribution",
                pieDataset, 
                true, 
                true, 
                false);
        try {
            ChartUtilities.writeChartAsPNG(out, chart, 500, 250);
        } catch (Exception e) {
            System.out.println("Problem occurred creating chart.");
        }		
	}
	public static void generateXYChart(OutputStream out) {
        XYSeries series = new XYSeries("XYGraph");
        series.add(1, 1);
        series.add(1, 2);
        series.add(2, 1);
        series.add(3, 9);
        series.add(4, 10);
        //         Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        //         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("XY Chart", // Title
                "x-axis", // x-axis Label
                "y-axis", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
        chart.setBackgroundPaint(Color.white);
        XYPlot p = chart.getXYPlot(); // Get the Plot object for a bar graph
        p.setBackgroundPaint(Color.black); // Modify the plot background
        p.setRangeGridlinePaint(Color.red);
        try {
            ChartUtilities.writeChartAsPNG(out, chart, 500, 300);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
	}
	public static void generateBarChart(OutputStream out) {
        // Create a simple Bar chart
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.setValue(6, "Profit1", "Jane");
		dataset.setValue(3, "Profit2", "Jane");
		dataset.setValue(7, "Profit1", "Tom");
		dataset.setValue(10, "Profit2", "Tom");
		dataset.setValue(8, "Profit1", "Jill");
		dataset.setValue(8, "Profit2", "Jill");
		dataset.setValue(5, "Profit1", "John");
		dataset.setValue(6, "Profit2", "John");
		dataset.setValue(12, "Profit1", "Fred");
		dataset.setValue(5, "Profit2", "Fred");
		// Profit1, Profit2 represent the row keys
		// Jane, Tom, Jill, etc. represent the column keys
		JFreeChart chart = ChartFactory.createBarChart3D( "Comparison between Salesman",
		"Salesman", "Value ($)", dataset, PlotOrientation.VERTICAL, true, true, false );

        try {
            ChartUtilities.writeChartAsPNG(out, chart, 500, 300);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
	}
	
	public static void generateTimeSeriesChart(OutputStream out) {
		  TimeSeries pop = new TimeSeries("Population", Day.class);
		  pop.add(new Day(10, 1, 2004), 100);
		  pop.add(new Day(10, 2, 2004), 150);
		  pop.add(new Day(10, 3, 2004), 250);
		  pop.add(new Day(10, 4, 2004), 275);
		  pop.add(new Day(10, 5, 2004), 325);
		  pop.add(new Day(10, 6, 2004), 425);
		  TimeSeriesCollection dataset = new TimeSeriesCollection();
		  dataset.addSeries(pop);
		  JFreeChart chart = ChartFactory.createTimeSeriesChart(
		     "Population of CSC408 Town",
		     "Date",
		     "Population",
		     dataset,
		     true,
		     true,
		     false);
		  chart.getPlot().setBackgroundAlpha(0.5F);
		    try {
	            ChartUtilities.writeChartAsPNG(out, chart, 500, 300);
	        } catch (IOException e) {
	            System.err.println("Problem occurred creating chart.");
	        }

	}
	
}
