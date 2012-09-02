package support.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
public class ModalJFrameHostHelper
{
    private ModalJFrameHostHelper() { }
    
    public static CloseAction host(final JFrame frame, JComponent component, int padding, boolean useTransparency)
    {
        final CloseAction closeAction = new CloseAction(frame, frame.getGlassPane());
        
        final Color glassColor = useTransparency ? new Color(192, 192, 192, 200) : Color.LIGHT_GRAY;
        JPanel glassPane = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                g.setColor(glassColor);
                g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
            }
        };
        handleMouseEvents(glassPane, closeAction);
        glassPane.setOpaque(false);
        frame.setGlassPane(glassPane);
        glassPane.setVisible(true);
        
        glassPane.setLayout(new BorderLayout(0, 0));
        glassPane.add(Box.createVerticalStrut(padding), BorderLayout.NORTH);
        glassPane.add(Box.createVerticalStrut(padding), BorderLayout.SOUTH);
        glassPane.add(Box.createHorizontalStrut(padding), BorderLayout.WEST);
        glassPane.add(Box.createHorizontalStrut(padding), BorderLayout.EAST);
        
        JPanel componentHostPanel = new JPanel(new BorderLayout(0,0));
        componentHostPanel.setBorder(BorderFactory.createEtchedBorder());
        glassPane.add(componentHostPanel, BorderLayout.CENTER);
        componentHostPanel.setOpaque(true);
        componentHostPanel.add(component, BorderLayout.CENTER);
        
        frame.validate();
        
        return closeAction;
    }
    
    public static class CloseAction
    {
        private final JFrame _frame;
        private final Component _initialGlassPane;
        
        CloseAction(JFrame frame, Component initialGlassPane)
        {
            _frame = frame;
            _initialGlassPane = initialGlassPane;
        }
        
        public void close()
        {
            _frame.setGlassPane(_initialGlassPane);
        }
    }
    
    private static void handleMouseEvents(final Component component, final CloseAction closeAction)
    {
        component.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent me)
            {
                me.consume();
            }

            @Override
            public void mousePressed(MouseEvent me)
            {
                me.consume();
            }

            @Override
            public void mouseReleased(MouseEvent me)
            {
                me.consume();
                
                if(me.getSource() == component)
                {
                    closeAction.close();
                }
            }

            @Override
            public void mouseEntered(MouseEvent me)
            {
                me.consume();
            }

            @Override
            public void mouseExited(MouseEvent me)
            {
                me.consume();
            }
        });
        
        component.addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent me)
            {
                me.consume();
            }

            @Override
            public void mouseMoved(MouseEvent me)
            {
                me.consume();
            }
        });
        
        component.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe)
            {
                mwe.consume();
            }
        });
    }
}