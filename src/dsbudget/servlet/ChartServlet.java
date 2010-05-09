package dsbudget.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.general.DefaultKeyedValuesDataset;
import org.jfree.data.general.PieDataset;

import dsbudget.Main;
import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartServlet extends ServletBase {

	public ChartServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/png");

		String type = request.getParameter("type");
		if (type.equals("balance")) {
			drawBalance(request, response);
		} else if(type.equals("pie")) {
			drawPie(request, response);
		}
	}

	protected void drawBalance(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Integer pageid = Integer.parseInt(request.getParameter("pageid"));
		Page page = budget.findPage(pageid);
		if(page != null) {
			Integer catid = Integer.parseInt(request.getParameter("catid"));
			Category category = page.findCategory(catid);
			renderBalanceChart(response.getOutputStream(), page, category);
		} else {
			logger.error("Can't find page ID: " + pageid);
		}
	}
	
	protected void drawPie(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Integer pageid = Integer.parseInt(request.getParameter("pageid"));
		Page page = budget.findPage(pageid);
		if(page != null) {
			Integer catid = Integer.parseInt(request.getParameter("catid"));
			Category category = page.findCategory(catid);
			renderPieChart(response.getOutputStream(), page, category);
		} else {
			logger.error("Can't find page ID: " + pageid);
		}
	}

	public void renderBalanceChart(OutputStream out, Page page, Category category) {
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
	
		///////////////////////////////////////////////////////////////////////////////////////////
		TimeSeries pop = new TimeSeries("Balance");
		BigDecimal balance = category.amount;
		Boolean bfirst = true;
		Date last = page.created;
		Date first = page.created;
		
		ArrayList<Expense> expenses = category.getExpensesSortedBy(Category.SortBy.DATE, false);
		for (Expense expense : expenses) {
			if(bfirst && expense.date.compareTo(page.created) > 0) {
				pop.addOrUpdate(new Day(page.created), category.amount);
			}
			bfirst = false;
			balance = balance.subtract(expense.amount);
			pop.addOrUpdate(new Day(expense.date), balance);
			if(last == null || last.compareTo(expense.date) < 0) {
				last = expense.date;
			}
			if(first == null || first.compareTo(expense.date) > 0) {
				first = expense.date;
			}
		}
		dataset.addSeries(pop);

		///////////////////////////////////////////////////////////////////////////////////////////
		TimeSeries zero = new TimeSeries("Zero");
		zero.add(new Day(first), 0);
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

        final XYDifferenceRenderer renderer = new XYDifferenceRenderer(
               new Color(category.color.getRed(),category.color.getGreen(),category.color.getBlue(),32), new Color(0,0,0,127), false
           );
		renderer.setSeriesStroke(0, new BasicStroke(3));
		renderer.setSeriesPaint(0, category.color);
		renderer.setSeriesPaint(1, Color.black);

        plot.setRenderer(renderer);

		try {
			ChartUtilities.writeChartAsPNG(out, chart, 
					Integer.parseInt(Main.conf.getProperty("balance_graph_width").trim()), 
					Integer.parseInt(Main.conf.getProperty("balance_graph_height").trim()));
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
	
	public void renderPieChart(OutputStream out, Page page, Category category) {
		
		DefaultKeyedValuesDataset dataset = new DefaultKeyedValuesDataset();
	
		///////////////////////////////////////////////////////////////////////////////////////////
		/*
		TimeSeries pop = new TimeSeries("Balance");
		BigDecimal balance = category.amount;
		Boolean bfirst = true;
		Date last = page.created;
		Date first = page.created;
		for (Expense expense : category.getExpensesSortByDate()) {
			if(bfirst && expense.date.compareTo(page.created) > 0) {
				pop.addOrUpdate(new Day(page.created), category.amount);
			}
			bfirst = false;
			balance = balance.subtract(expense.amount);
			pop.addOrUpdate(new Day(expense.date), balance);
			if(last == null || last.compareTo(expense.date) < 0) {
				last = expense.date;
			}
			if(first == null || first.compareTo(expense.date) > 0) {
				first = expense.date;
			}
		}
		*/
		//group by where
		TreeMap<String, Double> groups = new TreeMap<String, Double>();
		for (Expense expense : category.getExpenses()) {
			String key = expense.where;
			Double new_value = expense.amount.doubleValue();
			if(groups.containsKey(key)) {
				Double value = groups.get(key);
				groups.put(key, value + new_value);
			} else {
				groups.put(key, new_value);
			}
		}
		Map<String, Double> groups_sorted = sortByValue(groups);
		for(String key : groups_sorted.keySet()) {
			dataset.setValue(key, groups_sorted.get(key));	
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		JFreeChart chart = ChartFactory.createPieChart3D(null, dataset, false, false, false);
		
		Plot plot = chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setOutlineVisible(false);

		try {
			ChartUtilities.writeChartAsPNG(out, chart, 
					Integer.parseInt(Main.conf.getProperty("balance_graph_width").trim()), 
					Integer.parseInt(Main.conf.getProperty("balance_graph_height").trim()));
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
	
	Map sortByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	              .compareTo(((Map.Entry) (o1)).getValue());
	          }
	    });
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
