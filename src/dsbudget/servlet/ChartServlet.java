package dsbudget.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dsbudget.model.Category;
import dsbudget.model.Expense;
import dsbudget.model.Page;
import dsbudget.view.Chart;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

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

	protected void drawBalance(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Integer pageid = Integer.parseInt(request.getParameter("pageid"));
		Page page = budget.findPage(pageid);
		Integer catid = Integer.parseInt(request.getParameter("catid"));
		Category category = page.findCategory(catid);
		renderBalanceChart(response.getOutputStream(), page, category);
	}

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
		plot.getRenderer().setSeriesStroke(0, new BasicStroke(3));
		plot.getRenderer().setSeriesPaint(0, category.color);
		// plot.getRenderer().setSeriesShape(0, new Rectangle(5,5));
		try {
			ChartUtilities.writeChartAsPNG(out, chart, 600, 150);//650 is bit too large for printing with current 40px padding on the right
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
}
