/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package histogrammer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.statistics.HistogramDataset;

/**
 *
 * @author Paul
 */
public class ChartPanel extends JComponent {

    private JFreeChart _chart;

    public ChartPanel() {
    }

    public BufferedImage getImage(int w, int h) {
        return _chart.createBufferedImage(w, h);
    }

    public void loadData(String assignmentName, double[] data) {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        BarRenderer.setDefaultShadowsVisible(false);
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries(assignmentName, data, 50, 0, 105);
        _chart = ChartFactory.createHistogram("", "Scores", "Number of People", dataset, PlotOrientation.VERTICAL, false, false, false);
        _chart.setBackgroundPaint(Color.white);
        _chart.setBorderPaint(Color.darkGray);
        XYPlot p = _chart.getXYPlot();
        ((NumberAxis) p.getRangeAxis()).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        p.getRenderer().setSeriesPaint(0, Color.darkGray);//new Color(79, 129, 189));
        p.getRenderer().setBaseOutlinePaint(Color.white);
        p.setBackgroundPaint(Color.white);
        p.setRangeGridlinePaint(Color.darkGray);

        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (_chart != null) {
            _chart.draw((Graphics2D) g, new Rectangle2D.Double(0.0, 0.0, (double) this.getWidth(), (double) this.getHeight()));
        }
    }
}
