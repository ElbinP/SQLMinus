package org.misc.sqlminus;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A non-blocking pagination control panel that allows users to load additional
 * rows from a result set without blocking the UI with modal dialogs.
 * 
 * This panel displays in the status bar area and provides:
 * - Current row count information
 * - "Load Next N Rows" button
 * - "Stop Loading" button
 */
public class PaginationControlPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JLabel statusLabel;
	private JButton loadNextButton;
	private JButton stopButton;
	private int batchSize;
	private int currentRowCount;
	private PaginationCallback callback;
	private Timer blinkTimer;
	private boolean isHighlighted = false;
	
	/**
	 * Callback interface for pagination events
	 */
	public interface PaginationCallback {
		/**
		 * Called when user clicks "Load Next" button
		 */
		void onLoadNext();
		
		/**
		 * Called when user clicks "Stop" button
		 */
		void onStop();
	}
	
	/**
	 * Creates a new pagination control panel
	 */
	public PaginationControlPanel() {
		super(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		this.batchSize = 100;
		this.currentRowCount = 0;
		
		initializeComponents();
	}
	
	/**
	 * Initialize UI components
	 */
	private void initializeComponents() {
		statusLabel = new JLabel();
		add(statusLabel);
		
		loadNextButton = new JButton("Load Next");
		loadNextButton.setToolTipText("Load the next batch of rows");
		loadNextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (callback != null) {
					callback.onLoadNext();
				}
			}
		});
		add(loadNextButton);
		
		stopButton = new JButton("Stop");
		stopButton.setToolTipText("Stop loading additional rows");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (callback != null) {
					callback.onStop();
				}
			}
		});
		add(stopButton);
		
		updateStatusLabel();
	}
	
	/**
	 * Update the status label text
	 */
	private void updateStatusLabel() {
		if (currentRowCount > 0) {
			statusLabel.setText(String.format("  |  %d row(s) loaded. More rows available! ", currentRowCount));
			startBlinking();
		} else {
			statusLabel.setText("  |  ");
			stopBlinking();
		}
		loadNextButton.setText(String.format("Load Next %d Rows", batchSize));
	}
	
	/**
	 * Start blinking the status label to draw attention
	 */
	private void startBlinking() {
		if (blinkTimer != null && blinkTimer.isRunning()) {
			return; // Already blinking
		}
		
		blinkTimer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isHighlighted = !isHighlighted;
				if (isHighlighted) {
					statusLabel.setBackground(Color.YELLOW);
					statusLabel.setOpaque(true);
				} else {
					statusLabel.setBackground(null);
					statusLabel.setOpaque(false);
				}
			}
		});
		blinkTimer.start();
	}
	
	/**
	 * Stop blinking the status label
	 */
	private void stopBlinking() {
		if (blinkTimer != null) {
			blinkTimer.stop();
			blinkTimer = null;
		}
		statusLabel.setBackground(null);
		statusLabel.setOpaque(false);
		isHighlighted = false;
	}
	
	/**
	 * Update the pagination status
	 * 
	 * @param rowsLoaded Total number of rows loaded so far
	 * @param hasMore Whether more rows are available
	 */
	public void updateStatus(int rowsLoaded, boolean hasMore) {
		this.currentRowCount = rowsLoaded;
		updateStatusLabel();
		loadNextButton.setEnabled(hasMore);
	}
	
	/**
	 * Set the batch size for loading rows
	 * 
	 * @param batchSize Number of rows to load per batch
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		updateStatusLabel();
	}
	
	/**
	 * Get the current batch size
	 * 
	 * @return The batch size
	 */
	public int getBatchSize() {
		return batchSize;
	}
	
	/**
	 * Set the callback for pagination events
	 * 
	 * @param callback The callback to handle pagination events
	 */
	public void setCallback(PaginationCallback callback) {
		this.callback = callback;
	}
	
	/**
	 * Reset the panel to initial state
	 */
	public void reset() {
		this.currentRowCount = 0;
		stopBlinking();
		updateStatusLabel();
		loadNextButton.setEnabled(true);
	}
}

// Made with Bob
