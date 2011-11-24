package cakehat.views.admin.stathist;

import cakehat.config.Assignment;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.DefaultXYDataset;
import cakehat.database.Student;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author psastras
 * @author jeldridg
 * @author jak2
 */
public class StudentChartPanel extends JPanel
{
    private JFreeChart _chart;

    public StudentChartPanel()
    {
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(637, 309));
    }
    
    public void updateChart(Student student, Map<Assignment, Map<Student, Double>> allScores)
    {
        //Sorted list of assignments
        List<Assignment> assignments = new ArrayList<Assignment>(allScores.keySet());
        Collections.sort(assignments);
        
        //For each assignment record the assignment name, the student's score, and the class's average score
        double[][] studentData = new double[2][assignments.size()];
        double[][] classData = new double[2][assignments.size()];
        String[] asgnNames = new String[assignments.size()];

        for(int i = 0; i < assignments.size(); i++)
        {
            Assignment asgn = assignments.get(i);
            asgnNames[i] = asgn.getName();
            
            //Student's score - index 0 corresponds to assignment, index 1 is student score for assignment
            studentData[0][i] = i;
            studentData[1][i] = allScores.get(asgn).get(student) / asgn.getTotalPoints() * 100;

            //Class's average score - index 0 corresponds to assignment, index 1 is class average score for assignment
            Collection<Double> classScores = allScores.get(asgn).values();
            classData[0][i] = i;
            classData[1][i] = (Statistics.calculateMean(classScores) / asgn.getTotalPoints()) * 100;
        }
        
        //Axes
        SymbolAxis xAxis = new SymbolAxis("Assignment Name", asgnNames);
        xAxis.setAutoRange(true);
        ValueAxis yAxis = new NumberAxis("Score (%)");
        yAxis.setRange(0.0, 110.0);
        
        //Data series
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries(student.getLogin() + "'s Scores", studentData);
        dataset.addSeries("Class Average", classData);

        //Configure display of the chart
        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        DecimalFormat decimalformat1 = new DecimalFormat("##,###");
        renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator("{2}", decimalformat1, decimalformat1));
        renderer.setSeriesItemLabelsVisible(0, true);
        renderer.setSeriesItemLabelGenerator(1, new StandardXYItemLabelGenerator("{2}", decimalformat1, decimalformat1));
        renderer.setSeriesItemLabelsVisible(1, true);
        renderer.setBaseItemLabelsVisible(true);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        
        //Create chart
        _chart = new JFreeChart(student.getLogin() + "'s Grade History", new Font("Sans-Serif", Font.BOLD, 14), plot, true);
        _chart.setBackgroundPaint(Color.WHITE);
    }

    public BufferedImage getAsImage(int width, int height)
    {
        return _chart.createBufferedImage(width, height);
    }

    @Override
    protected void paintComponent(Graphics g)
    {   
        if(_chart != null)
        {
            _chart.draw((Graphics2D) g, new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight()));
        }
    }
}