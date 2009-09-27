package frontend.grader;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;

import javax.swing.JScrollBar;

class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy 
{
	private Vector<Component> _order;
	private JScrollBar _scroll;

	public MyOwnFocusTraversalPolicy(Vector<Component> order, JScrollBar scroll) 
	{
		_order = new Vector<Component>(order.size());
		_order.addAll(order);
		_scroll = scroll;
	}
	
	public Component getComponentAfter(Container focusCycleRoot, Component aComponent)
	{
		if (_order.indexOf(aComponent) + 1 < _order.size()) 
		{
			int idx = (_order.indexOf(aComponent) + 1);
			Component toReturn = _order.get(idx);
			if(toReturn.getY() + toReturn.getParent().getY() > _scroll.getValue() + _scroll.getVisibleAmount())
			{
				_scroll.setValue(_scroll.getValue() + _scroll.getBlockIncrement(1));
			}
			return toReturn;
		}
		else
		{
			return _order.lastElement();
		}
	}

	public Component getComponentBefore(Container focusCycleRoot, Component aComponent)
	{
		int idx = _order.indexOf(aComponent) - 1;
		Component toReturn;
		if (idx < 0) 
		{
			toReturn = _order.get(0);
		}
		else
		{
			toReturn = _order.get(idx);
		}
		if(toReturn.getY() + toReturn.getParent().getY() < _scroll.getValue())
		{
			_scroll.setValue(_scroll.getValue() - _scroll.getBlockIncrement(-1));
		}
		return toReturn;
	}

	public Component getDefaultComponent(Container focusCycleRoot) 
	{
		return _order.get(0);
	}

	public Component getLastComponent(Container focusCycleRoot) 
	{
		return _order.lastElement();
	}

	public Component getFirstComponent(Container focusCycleRoot)
	{
		return _order.get(0);
	}
}
