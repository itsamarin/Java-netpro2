import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MThrd implements Runnable {
    private JEditorPane editorPane;
    private JTextField txtUrl;
    private JTextArea txtrHeader;
    private Thread thrd;
    private String threadName;
    private JTextArea activityLog;
    private JLabel statusLabel;
    private volatile boolean stopped = false;

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;
    private static final String USER_AGENT = "JavaWebBrowser/1.0 (Network Programming Assignment)";

    private static CookieManager cookieManager;

    static {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    public MThrd(String threadName, JEditorPane editorPane, JTextField txtUrl,
                 JTextArea txtrHeader, JTextArea activityLog, JLabel statusLabel) {
        this.threadName = threadName;
        this.editorPane = editorPane;
        this.txtUrl = txtUrl;
        this.txtrHeader = txtrHeader;
        this.activityLog = activityLog;
        this.statusLabel = statusLabel;
    }

    public void run() {
        if (!stopped) {
            getLink();
        }
        if (!stopped) {
            getHeader();
        }
        updateStatus("Ready");
    }

    public void start() {
        log(threadName + " building Thread");
        if (thrd == null) {
            thrd = new Thread(this, threadName);
            thrd.start();
            log(thrd.getName() + " Starting Thread");
        }
    }

    public void stop() {
        stopped = true;
        if (thrd != null) {
            thrd.interrupt();
            log(threadName + " Stopped");
        }
    }

    private boolean validateUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            log(threadName + " Error: URL is empty");
            showError("Please enter a URL");
            return false;
        }

        String trimmedUrl = urlString.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            trimmedUrl = "http://" + trimmedUrl;
            final String finalUrl = trimmedUrl;
            SwingUtilities.invokeLater(() -> txtUrl.setText(finalUrl));
        }

        try {
            new URL(trimmedUrl);
            return true;
        } catch (MalformedURLException e) {
            log(threadName + " Error: Invalid URL format");
            showError("Invalid URL: " + urlString);
            return false;
        }
    }

    public void getLink() {
        String urlString = txtUrl.getText().trim();

        if (!validateUrl(urlString)) {
            return;
        }

        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "http://" + urlString;
        }

        updateStatus("Loading: " + urlString);
        log(thrd.getName() + " Fetching web page...");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            if (stopped) return;
        }

        try {
            final String finalUrl = urlString;
            SwingUtilities.invokeLater(() -> {
                try {
                    editorPane.setPage(finalUrl);
                } catch (IOException e) {
                    editorPane.setContentType("text/html");
                    editorPane.setText("<html><body><h2>Error Loading Page</h2>" +
                            "<p>Can't load: " + finalUrl + "</p>" +
                            "<p>Error: " + e.getMessage() + "</p></body></html>");
                }
            });
            log(thrd.getName() + " Web page loaded successfully");
        } catch (Exception e) {
            showError("Can't load " + urlString + ": " + e.getMessage());
            log(thrd.getName() + " Error: " + e.getMessage());
        }
    }

    public void getHeader() {
        String urlString = txtUrl.getText().trim();

        if (!validateUrl(urlString)) {
            return;
        }

        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "http://" + urlString;
        }

        updateStatus("Fetching headers: " + urlString);
        log(thrd.getName() + " Fetching HTTP headers...");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            if (stopped) return;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestMethod("GET");

            conn.connect();

            Map<String, List<String>> headerFields = conn.getHeaderFields();

            StringBuilder headerText = new StringBuilder();
            headerText.append("=== Headers for: ").append(urlString).append(" ===\n");
            headerText.append("Response Code: ").append(conn.getResponseCode())
                      .append(" ").append(conn.getResponseMessage()).append("\n\n");

            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    headerText.append("Status: ").append(entry.getValue()).append("\n");
                } else {
                    headerText.append(key).append(": ").append(entry.getValue()).append("\n");
                }
            }

            List<String> contentLength = headerFields.get("Content-Length");
            headerText.append("\n--- Summary ---\n");
            if (contentLength == null) {
                headerText.append("Content-Length: Not specified in header\n");
            } else {
                for (String header : contentLength) {
                    headerText.append("Content-Length: ").append(header).append(" bytes\n");
                }
            }

            List<String> contentType = headerFields.get("Content-Type");
            if (contentType != null) {
                headerText.append("Content-Type: ").append(contentType.get(0)).append("\n");
            }

            if (!cookieManager.getCookieStore().getCookies().isEmpty()) {
                headerText.append("\n--- Cookies ---\n");
                cookieManager.getCookieStore().getCookies().forEach(cookie ->
                    headerText.append(cookie.getName()).append("=").append(cookie.getValue()).append("\n"));
            }

            final String finalHeaderText = headerText.toString();
            SwingUtilities.invokeLater(() -> txtrHeader.append(finalHeaderText));

            log(thrd.getName() + " Headers retrieved successfully");

        } catch (IOException e) {
            final String errorMsg = "Can't load header from " + urlString + ": " + e.getMessage() + "\n";
            SwingUtilities.invokeLater(() -> txtrHeader.append(errorMsg));
            log(thrd.getName() + " Error fetching headers: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> activityLog.append(message + "\n"));
    }

    private void updateStatus(String status) {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            editorPane.setContentType("text/html");
            editorPane.setText("<html><body><h2>Error</h2><p>" + message + "</p></body></html>");
        });
    }

    public static void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }
}
