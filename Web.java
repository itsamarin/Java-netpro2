import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Web {

    private JFrame frame;
    private JTextField txtUrlLeft;
    private JTextField txtUrlRight;
    private JEditorPane editorPaneLeft;
    private JEditorPane editorPaneRight;
    private JTextArea headerAreaLeft;
    private JTextArea headerAreaRight;
    private JTextArea activityLog;
    private JLabel statusLabel;
    private JButton btnFetch;
    private JButton btnStop;
    private JButton btnRefresh;
    private JButton btnClearCookies;

    private MThrd threadLeft;
    private MThrd threadRight;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Web window = new Web();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Web() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Amrin Yanya s5050531@kmitl.ac.th - Java Web Browser (Network Programming Assignment)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 700);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbarPanel = createToolbar();
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);

        JSplitPane browserSplitPane = createBrowserPanes();
        mainPanel.add(browserSplitPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            new EmptyBorder(5, 5, 10, 5)
        ));

        JPanel urlPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel leftUrlPanel = createUrlInputPanel("Left Panel URL:", true);
        JPanel rightUrlPanel = createUrlInputPanel("Right Panel URL:", false);

        urlPanel.add(leftUrlPanel);
        urlPanel.add(rightUrlPanel);

        toolbar.add(urlPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        btnFetch = new JButton("Fetch Both");
        btnFetch.setPreferredSize(new Dimension(100, 30));
        btnFetch.addActionListener(e -> fetchBothUrls());

        btnStop = new JButton("Stop");
        btnStop.setPreferredSize(new Dimension(80, 30));
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopFetching());

        btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(90, 30));
        btnRefresh.addActionListener(e -> refreshBoth());

        btnClearCookies = new JButton("Clear Cookies");
        btnClearCookies.setPreferredSize(new Dimension(120, 30));
        btnClearCookies.addActionListener(e -> {
            MThrd.clearCookies();
            activityLog.append("Cookies cleared\n");
        });

        buttonPanel.add(btnFetch);
        buttonPanel.add(btnStop);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClearCookies);

        toolbar.add(buttonPanel, BorderLayout.SOUTH);

        return toolbar;
    }

    private JPanel createUrlInputPanel(String label, boolean isLeft) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));

        JLabel urlLabel = new JLabel(label);
        panel.add(urlLabel, BorderLayout.WEST);

        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 25));
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fetchBothUrls();
                }
            }
        });

        if (isLeft) {
            txtUrlLeft = textField;
        } else {
            txtUrlRight = textField;
        }

        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    private JSplitPane createBrowserPanes() {
        JPanel leftBrowserPanel = createSingleBrowserPanel(true);
        JPanel rightBrowserPanel = createSingleBrowserPanel(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftBrowserPanel, rightBrowserPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);

        return splitPane;
    }

    private JPanel createSingleBrowserPanel(boolean isLeft) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(isLeft ? "Browser A" : "Browser B"));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText("<html><body><h2>Enter a URL and click Fetch</h2></body></html>");

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        if (isLeft) {
            editorPaneLeft = editorPane;
        } else {
            editorPaneRight = editorPane;
        }

        JTextArea headerArea = new JTextArea();
        headerArea.setEditable(false);
        headerArea.setRows(6);
        headerArea.setText("HTTP Headers will appear here...\n");

        JScrollPane headerScrollPane = new JScrollPane(headerArea);
        headerScrollPane.setBorder(BorderFactory.createTitledBorder("HTTP Headers"));

        if (isLeft) {
            headerAreaLeft = headerArea;
        } else {
            headerAreaRight = headerArea;
        }

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, headerScrollPane);
        verticalSplit.setResizeWeight(0.7);
        verticalSplit.setOneTouchExpandable(true);

        panel.add(verticalSplit, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        activityLog = new JTextArea();
        activityLog.setEditable(false);
        activityLog.setRows(4);
        activityLog.setText("Activity Log:\n");

        JScrollPane logScrollPane = new JScrollPane(activityLog);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));

        bottomPanel.add(logScrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            new EmptyBorder(5, 10, 5, 10)
        ));

        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JLabel authorLabel = new JLabel("Amrin Yanya - s5050531@kmitl.ac.th");
        authorLabel.setForeground(Color.GRAY);
        statusPanel.add(authorLabel, BorderLayout.EAST);

        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    private void fetchBothUrls() {
        headerAreaLeft.setText("");
        headerAreaRight.setText("");
        activityLog.append("\n--- Starting new fetch operation ---\n");

        btnFetch.setEnabled(false);
        btnStop.setEnabled(true);
        statusLabel.setText("Fetching...");

        threadLeft = new MThrd("Thread_A", editorPaneLeft, txtUrlLeft, headerAreaLeft, activityLog, statusLabel);
        threadRight = new MThrd("Thread_B", editorPaneRight, txtUrlRight, headerAreaRight, activityLog, statusLabel);

        threadLeft.start();
        threadRight.start();

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}
            SwingUtilities.invokeLater(() -> {
                btnFetch.setEnabled(true);
                btnStop.setEnabled(false);
            });
        }).start();
    }

    private void stopFetching() {
        if (threadLeft != null) {
            threadLeft.stop();
        }
        if (threadRight != null) {
            threadRight.stop();
        }
        btnFetch.setEnabled(true);
        btnStop.setEnabled(false);
        statusLabel.setText("Stopped");
        activityLog.append("Fetch operation stopped by user\n");
    }

    private void refreshBoth() {
        activityLog.append("Refreshing both panels...\n");
        fetchBothUrls();
    }
}
