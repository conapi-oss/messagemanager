package nl.queuemanager.ui.message;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import nl.queuemanager.ui.GlobalHighlightEvent;
import nl.queuemanager.ui.MessagesTable;
import nl.queuemanager.ui.SearchModeChangedEvent;
import nl.queuemanager.ui.util.DocumentAdapter;
import nl.queuemanager.ui.util.TriStateCheckBox;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchPanel extends JPanel {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledSearchTermPublisher;
    private ScheduledFuture<?> searchResultStatusUpdater;
    private final MessagesTable msgsTable;
    private final JLabel searchResultStatusLabel;

    public SearchPanel(final MessagesTable eventSource, final EventBus eventBus) {
        super();
        msgsTable = eventSource;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        final AtomicBoolean publishSearch = new AtomicBoolean(true);
        final JTextField searchField = new JTextField();
        searchField.setName("MessageSearchField");
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height)); // make it as wide as possible
        searchField.putClientProperty("JTextField.variant", "search");

        // Add ghost text in title case
        final String GHOST_TEXT = "Type to Search";
        final Color GHOST_COLOR = Color.GRAY;
        final Color ACTIVE_COLOR = UIManager.getColor("TextField.foreground");

        searchField.setForeground(GHOST_COLOR);
        searchField.setText(GHOST_TEXT);

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (searchField.getText().equals(GHOST_TEXT)) {
                        searchField.setText("");
                        searchField.setForeground(ACTIVE_COLOR);
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (searchField.getText().isEmpty()) {
                        searchField.setForeground(GHOST_COLOR);
                        searchField.setText(GHOST_TEXT);
                        cancelSearchResultStatusUpdater();
                    }
                });
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            public void updated(DocumentEvent e) {
                if(!publishSearch.get()) return;

                if(searchField.getForeground() == GHOST_COLOR) return; // Don't publish when showing ghost text

                // Cancel the previous scheduled task if it exists
                if (scheduledSearchTermPublisher != null && !scheduledSearchTermPublisher.isDone()) {
                    scheduledSearchTermPublisher.cancel(false);
                }

                // Schedule a new task, this will avoid a search on each key stroke
                scheduledSearchTermPublisher = scheduler.schedule(() -> {
                    publishSearchText(e.getDocument(), searchField, eventBus);
                }, 1000, TimeUnit.MILLISECONDS); // Adjust the delay as needed
            }
        });

        eventBus.register(new Object() {
            @Subscribe
            public void onGlobalHighlightEvent(GlobalHighlightEvent e) {
                // don't publish search text when the search field is the source
                if(e.getSource() != searchField) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            publishSearch.set(false);
                            String highlightString = e.getHighlightString();
                            if (highlightString.isEmpty()) {
                                searchField.setForeground(GHOST_COLOR);
                                searchField.setText(GHOST_TEXT);
                                cancelSearchResultStatusUpdater();
                            } else {
                                searchField.setForeground(ACTIVE_COLOR);
                                searchField.setText(highlightString);
                            }
                        } finally {
                            publishSearch.set(true);
                        }
                    });
                }
            }
        });

        // Create a label to display the search result status
        searchResultStatusLabel = new JLabel();
        searchResultStatusLabel.setText("");
        searchResultStatusLabel.setMaximumSize(searchResultStatusLabel.getPreferredSize());

        // Create the checkbox
        TriStateCheckBox filterCheckBox = new TriStateCheckBox("Filter");//new JCheckBox("Filter");

        filterCheckBox.addPropertyChangeListener("state", evt -> {
            TriStateCheckBox.State oldState = (TriStateCheckBox.State) evt.getOldValue();
            TriStateCheckBox.State state = (TriStateCheckBox.State) evt.getNewValue();
            // Handle state change
        //});

        /*filterCheckBox.addActionListener(e -> {
            final TriStateCheckBox.State state = filterCheckBox.getState();*/
            SearchModeChangedEvent.SearchMode mode;
            switch(state) {
                case CHECKED:
                    mode = SearchModeChangedEvent.SearchMode.FILTER;
                    break;
                case THIRD_STATE:
                    mode = SearchModeChangedEvent.SearchMode.INVERSE_FILTER;
                    break;
                case UNCHECKED:
                default:
                    mode = SearchModeChangedEvent.SearchMode.NO_FILTER;
                    break;
            }
            // Publish an event or update the search behavior based on the checkbox state
            eventBus.post(new SearchModeChangedEvent(eventSource, mode));
        });
        filterCheckBox.setMaximumSize(filterCheckBox.getPreferredSize()); // keep it as small as possible, let the search field the rest

        // Add components to the panel
        add(Box.createHorizontalStrut(15)); // Add some space between components
        add(searchField);
        add(Box.createHorizontalStrut(5)); // Add some space between components
        add(searchResultStatusLabel);
        add(Box.createHorizontalStrut(5)); // Add some space between components
        add(filterCheckBox);
        setBorder(BorderFactory.createEmptyBorder());

        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }


    // Send the search text to the event bus, the various highlighers subscribe to this event
    // NOTE: Even if this event is published on the TopicSubscriber,
    // the event will be handled by the Highlighters on the Queue Browser as well and vice versa.
    // Doubt it harms but would require deeper changes if we want to avoid this.
    private void publishSearchText(Document document, JTextField searchField, EventBus eventBus) {
        try {
            int length = document.getLength();
            String text = document.getText(0, length);
            eventBus.post(new GlobalHighlightEvent(searchField, text));

            startSearchResultStatusUpdater();
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void cancelSearchResultStatusUpdater() {
        if(searchResultStatusUpdater != null && !searchResultStatusUpdater.isDone()) {
            searchResultStatusUpdater.cancel(false);
        }
        SwingUtilities.invokeLater(() -> {
            // reset the label
            searchResultStatusLabel.setText("");
            searchResultStatusLabel.setMaximumSize(searchResultStatusLabel.getPreferredSize());
        });
    }
    private void startSearchResultStatusUpdater() {
        // schedule a search result status update and cancel the previous one
        // the status update will be done every 500 ms
        cancelSearchResultStatusUpdater();
        searchResultStatusUpdater = scheduler.scheduleAtFixedRate(this::updateSearchResultStatus, 500, 500, TimeUnit.MILLISECONDS);
    }

    private void updateSearchResultStatus() {
        SwingUtilities.invokeLater(() -> {
            int highlightedRowCount = msgsTable.getHighlightedRowCount();
            if (highlightedRowCount == 0) {
                searchResultStatusLabel.setText("(No hits)");
            } else if (highlightedRowCount == 1) {
                searchResultStatusLabel.setText("(1 hit)");
            } else {
                searchResultStatusLabel.setText("(" + highlightedRowCount + " hits)");
            }
            searchResultStatusLabel.setMaximumSize(searchResultStatusLabel.getPreferredSize());
        });
    }

    /**
     * Shutdown method to be called when the application is closing.
     * This ensures that the scheduler is properly shut down.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("SearchPanel scheduler did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
