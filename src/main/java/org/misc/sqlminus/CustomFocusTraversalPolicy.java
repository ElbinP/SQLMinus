package org.misc.sqlminus;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.List;

// Custom FocusTraversalPolicy using a list
public class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
	private final List<Component> order;

	public CustomFocusTraversalPolicy(List<Component> order) {
		this.order = order;
	}

	public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
		int idx = (order.indexOf(aComponent) + 1) % order.size();
		return order.get(idx);
	}

	public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
		int idx = order.indexOf(aComponent) - 1;
		if (idx < 0)
			idx = order.size() - 1;
		return order.get(idx);
	}

	public Component getFirstComponent(Container focusCycleRoot) {
		return order.get(0);
	}

	public Component getLastComponent(Container focusCycleRoot) {
		return order.get(order.size() - 1);
	}

	public Component getDefaultComponent(Container focusCycleRoot) {
		return order.get(0);
	}
}
