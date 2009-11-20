package dsbudget.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;
import dsbudget.view.Chart;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ChartServlet extends ServletBase {

	public ChartServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/png");
		// Chart.generatePieChart(response.getOutputStream());
		// Chart.generateXYChart(response.getOutputStream());
		// Chart.generateBarChart(response.getOutputStream());
		// Chart.generateTimeSeriesChart(response.getOutputStream());

		String type = request.getParameter("type");
		if (type.equals("balance")) {
			drawBalance(request, response);
		}
	}

	protected void drawBalance(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Integer pageid = Integer.parseInt(request.getParameter("pageid"));
		Page page = budget.findPage(pageid);
		Integer catid = Integer.parseInt(request.getParameter("catid"));
		Category category = page.findCategory(catid);
		renderBalanceChart(response.getOutputStream(), page, category);
	}
/*
	public void renderBalanceChart(OutputStream out, Page page, Category category) {
		TimeSeries pop = new TimeSeries("Balance", Day.class);
		BigDecimal balance = category.amount;
		
		Boolean first = true;
		for (Expense expense : category.getExpensesSortByDate()) {
			if(first && expense.date.compareTo(page.created) > 0) {
				pop.addOrUpdate(new Day(page.created), category.amount);
			}
			first = false;
			balance = balance.subtract(expense.amount);
			pop.addOrUpdate(new Day(expense.date), balance);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(pop);

		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null,
				"Balance", dataset, false, false, false);

		XYPlot plot = chart.getXYPlot();
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setBackgroundPaint(Color.white);
		plot.setOutlineVisible(false);
		
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(3));
		renderer.setSeriesPaint(0, category.color);
        if (renderer instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer r = (XYLineAndShapeRenderer)renderer;
            r.setBaseShapesVisible(true);
            r.setBaseShapesFilled(true);
            r.setDrawSeriesLineAsPath(true);
        }
		try {
			ChartUtilities.writeChartAsPNG(out, chart, 600, 150);//650 is bit too large for printing with current 40px padding on the right
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
*/
	public void renderBalanceChart(OutputStream out, Page page, Category category) {
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
	
		///////////////////////////////////////////////////////////////////////////////////////////
		TimeSeries pop = new TimeSeries("Balance");
		BigDecimal balance = category.amount;
		Boolean first = true;
		Date last = page.created;
		for (Expense expense : category.getExpensesSortByDate()) {
			if(first && expense.date.compareTo(page.created) > 0) {
				pop.addOrUpdate(new Day(page.created), category.amount);
			}
			first = false;
			balance = balance.subtract(expense.amount);
			pop.addOrUpdate(new Day(expense.date), balance);
			if(last.compareTo(expense.date) < 0) {
				last = expense.date;
			}
		}
		dataset.addSeries(pop);

		///////////////////////////////////////////////////////////////////////////////////////////
		TimeSeries zero = new TimeSeries("Zero");
		zero.add(new Day(page.created), 0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(page.created);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)+1);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		if(last.compareTo(cal.getTime()) < 0) {
			zero.addOrUpdate(new Day(cal.getTime()), 0);
		} else {
			zero.addOrUpdate(new Day(last), 0);
		}
		dataset.addSeries(zero);
		
		///////////////////////////////////////////////////////////////////////////////////////////
		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null, "Balance", dataset, false, false, false);
		
		XYPlot plot = chart.getXYPlot();
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setBackgroundPaint(Color.white);
		plot.setOutlineVisible(false);
/*
	     final GradientPaint gp0 = new GradientPaint(
	             0.0f, 0.0f, Color.yellow, 
	             0.0f, 0.0f, new Color(0, 0, 64)
	         );
	         */
       final XYDifferenceRenderer renderer = new XYDifferenceRenderer(
               new Color(category.color.getRed(),category.color.getGreen(),category.color.getBlue(),32), new Color(0,0,0,127), false
           );
		renderer.setSeriesStroke(0, new BasicStroke(3));
		renderer.setSeriesPaint(0, category.color);
		//renderer.setSeriesPaint(1, new Color(0,0,0,0));
		renderer.setSeriesPaint(1, Color.black);
		
		/*
        if (renderer instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer r = (XYLineAndShapeRenderer)renderer;
            r.setBaseShapesVisible(true);
            r.setBaseShapesFilled(true);
            r.setDrawSeriesLineAsPath(true);
        }*/
        
        plot.setRenderer(renderer);
        
        /*
		try {
			KeypointPNGEncoderAdapter encorder = new KeypointPNGEncoderAdapter();
			encorder.setEncodingAlpha(true);
			out.write(encorder.encode(chart.createBufferedImage(600, 150, BufferedImage.BITMASK, null)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		try {
			ChartUtilities.writeChartAsPNG(out, chart, 600, 150);//650 is bit too large for printing with current 40px padding on the right
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
		
	}
}
