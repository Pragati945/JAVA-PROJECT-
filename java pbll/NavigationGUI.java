import logic.*;
import model.*;
import database.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
public class NavigationGUI extends JFrame {
    private static final Color BG_DARK      = new Color(13, 17, 28);
    private static final Color BG_CARD      = new Color(22, 29, 46);
    private static final Color BG_INPUT     = new Color(30, 40, 62);
    private static final Color ACCENT_TEAL  = new Color(0, 210, 190);
    private static final Color ACCENT_AMBER = new Color(255, 180, 50);
    private static final Color ACCENT_RED   = new Color(255, 80, 80);
    private static final Color TEXT_PRIMARY  = new Color(220, 230, 255);
    private static final Color TEXT_MUTED    = new Color(120, 140, 175);
    private static final Color BORDER_COLOR  = new Color(40, 55, 85);
    private Graph  graph;
    private Map<Integer, Location> cityMap;
    private CityDAO  cityDAO;
    private EdgeDAO  edgeDAO;
    private JComboBox<String>  cbSource, cbDest;
    private JComboBox<String>  cbStrategy;
    private JTextArea taResult;
    private JLabel lblStatus;
    private GraphPanel graphPanel;
    private DefaultTableModel tableModel;
    public NavigationGUI() {
        super("Dehradun Navigation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);
        loadData();
        buildUI();
        refreshCombos();
        setVisible(true);
    }
    private void loadData() {
        cityDAO = new CityDAO();
        edgeDAO = new EdgeDAO();
        cityMap = cityDAO.getCityMap();
        graph   = new Graph();
        for (Location loc : cityMap.values()) graph.addLocation(loc);
        List<Edge> edges = edgeDAO.getAllEdges(cityMap);
        for (Edge e : edges)
            graph.addEdge(e.getSource(), e.getDestination(),
                          e.getDistance(), e.getTime(), e.getTraffic());
    }
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_TEAL));
        p.setPreferredSize(new Dimension(0, 64));
        JLabel title = new JLabel("  🗺  DEHRADUN NAVIGATOR");
        title.setFont(new Font("Monospaced", Font.BOLD, 20));
        title.setForeground(ACCENT_TEAL);
        JLabel sub = new JLabel("Shortest · Fastest · Least Traffic   ");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        p.add(title, BorderLayout.WEST);
        p.add(sub,   BorderLayout.EAST);
        return p;
    }
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(400);
        split.setDividerSize(4);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        return split;
    }
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(16, 16, 16, 8));
        p.add(buildRouteCard());
        p.add(Box.createVerticalStrut(12));
        p.add(buildAddLocationCard());
        p.add(Box.createVerticalStrut(12));
        p.add(buildAddEdgeCard());
        p.add(Box.createVerticalStrut(12));
        p.add(buildResultCard());
        return p;
    }
    private JPanel buildRouteCard() {
        JPanel card = card("ROUTE FINDER");
        cbSource   = styledCombo();
        cbDest     = styledCombo();
        cbStrategy = new JComboBox<>(new String[]{
            "Shortest Path (Distance)",
            "Fastest Path (Time)",
            "Least Traffic Path",
            "All Strategies"
        });
        styleCombo(cbStrategy);
        card.add(label("From"));        card.add(cbSource);
        card.add(Box.createVerticalStrut(6));
        card.add(label("To"));          card.add(cbDest);
        card.add(Box.createVerticalStrut(6));
        card.add(label("Strategy"));    card.add(cbStrategy);
        card.add(Box.createVerticalStrut(10));
        JButton btn = accentButton(" FIND ROUTE", ACCENT_TEAL);
        btn.addActionListener(e -> findRoute());
        card.add(btn);
        return card;
    }
    private JPanel buildAddLocationCard() {
        JPanel card = card("ADD LOCATION");
        JTextField tfName = styledField("City / landmark name");
        card.add(tfName);
        card.add(Box.createVerticalStrut(8));
        JButton btn = accentButton("+ ADD TO MAP", ACCENT_AMBER);
        btn.addActionListener(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { status("Enter a name first.", ACCENT_RED); return; }
            if (graph.getLocations().stream().anyMatch(l -> l.getName().equalsIgnoreCase(name))) {
                status("Location already exists.", ACCENT_RED); return;
            }
            int id = cityDAO.insertCity(name);
            if (id == -1) { status("DB error — location not saved.", ACCENT_RED); return; }
            Location loc = new Location(id, name);
            graph.addLocation(loc);
            cityMap.put(id, loc);
            refreshCombos();
            graphPanel.repaint();
            tfName.setText("");
            status("Location added: " + name, ACCENT_TEAL);
        });
        card.add(btn);
        return card;
    }
    private JPanel buildAddEdgeCard() {
        JPanel card = card("ADD EDGE");
        styleCombo(edgeFromCombo);
        styleCombo(edgeToCombo);
        JTextField tfDist  = styledField("Distance (km)");
        JTextField tfTime  = styledField("Time (min)");
        JTextField tfTraf  = styledField("Traffic factor");
        card.add(label("From"));  card.add(edgeFromCombo);
        card.add(Box.createVerticalStrut(4));
        card.add(label("To"));    card.add(edgeToCombo);
        card.add(Box.createVerticalStrut(4));
        card.add(tfDist);
        card.add(Box.createVerticalStrut(3));
        card.add(tfTime);
        card.add(Box.createVerticalStrut(3));
        card.add(tfTraf);
        card.add(Box.createVerticalStrut(8));
        JButton btn = accentButton("+ ADD EDGE", new Color(160, 100, 255));
        btn.addActionListener(e -> {
            try {
                String fromName = (String) edgeFromCombo.getSelectedItem();
                String toName   = (String) edgeToCombo.getSelectedItem();
                if (fromName == null || toName == null || fromName.equals(toName)) {
                    status("Select two different locations.", ACCENT_RED); return;
                }
                double d  = Double.parseDouble(tfDist.getText().trim());
                double t  = Double.parseDouble(tfTime.getText().trim());
                double tr = Double.parseDouble(tfTraf.getText().trim());
                Location src = findByName(fromName);
                Location dst = findByName(toName);
                if (src == null || dst == null) { status("Location not found.", ACCENT_RED); return; }
                graph.addEdge(src, dst, d, t, tr);
                refreshTable();
                graphPanel.repaint();
                tfDist.setText(""); tfTime.setText(""); tfTraf.setText("");
                status("Edge added: " + fromName + " → " + toName, ACCENT_TEAL);
            } catch (NumberFormatException ex) {
                status("Enter valid numeric values.", ACCENT_RED);
            }
        });
        card.add(btn);
        return card;
    }
    private JComboBox<String> edgeFromCombo = new JComboBox<>();
    private JComboBox<String> edgeToCombo   = new JComboBox<>();
    private JPanel buildResultCard() {
        JPanel card = card("RESULT");
        card.setPreferredSize(new Dimension(360, 140));
        taResult = new JTextArea(5, 28);
        taResult.setEditable(false);
        taResult.setBackground(BG_INPUT);
        taResult.setForeground(ACCENT_TEAL);
        taResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taResult.setBorder(new EmptyBorder(6, 8, 6, 8));
        taResult.setText("Route results will appear here…");
        JScrollPane sp = new JScrollPane(taResult);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.setBackground(BG_INPUT);
        sp.getViewport().setBackground(BG_INPUT);
        card.add(sp);
        return card;
    }
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(16, 8, 16, 16));
        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(700, 460));
        String[] cols = {"From", "To", "Distance", "Time", "Traffic"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.setPreferredSize(new Dimension(700, 160));
        JLabel lbl1 = sectionLabel("GRAPH VIEW");
        JLabel lbl2 = sectionLabel("EDGE TABLE");
        p.add(lbl1,       BorderLayout.NORTH);
        p.add(graphPanel, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setBackground(BG_DARK);
        bottom.add(lbl2, BorderLayout.NORTH);
        bottom.add(sp,   BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        p.setPreferredSize(new Dimension(0, 26));
        lblStatus = new JLabel("  Ready — DB loaded.");
        lblStatus.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblStatus.setForeground(TEXT_MUTED);
        p.add(lblStatus, BorderLayout.WEST);
        return p;
    }
    private void findRoute() {
        String srcName  = (String) cbSource.getSelectedItem();
        String dstName  = (String) cbDest.getSelectedItem();
        if (srcName == null || dstName == null) { status("Select source and destination.", ACCENT_RED); return; }
        if (srcName.equals(dstName))            { status("Source and destination cannot be the same.", ACCENT_RED); return; }
        Location src = findByName(srcName);
        Location dst = findByName(dstName);
        if (src == null || dst == null) { status("Location not found in graph.", ACCENT_RED); return; }
        int strat = cbStrategy.getSelectedIndex();
        StringBuilder sb = new StringBuilder();
        if (strat == 0 || strat == 3) sb.append(runStrategy(new ShortestPathStrategy(),  graph, src, dst, "Shortest Path (Distance)"));
        if (strat == 1 || strat == 3) sb.append(runStrategy(new FastestPathStrategy(),   graph, src, dst, "Fastest Path (Time)"));
        if (strat == 2 || strat == 3) sb.append(runStrategy(new LeastTrafficStrategy(),  graph, src, dst, "Least Traffic Path"));
        taResult.setText(sb.toString().trim());
        taResult.setCaretPosition(0);
        List<Location> highlightPath = null;
        RouteStrategy primary = strat == 1 ? new FastestPathStrategy()
                              : strat == 2 ? new LeastTrafficStrategy()
                              : new ShortestPathStrategy();
        RouteResult rr = primary.calculateRoute(graph, src, dst);
        if (rr != null && rr.getPath() != null && rr.getPath().size() > 1)
            highlightPath = rr.getPath();
        graphPanel.setHighlightPath(highlightPath);
        status("Route computed: " + srcName + " → " + dstName, ACCENT_TEAL);
    }
    private String runStrategy(RouteStrategy s, Graph g, Location src, Location dst, String label) {
        RouteResult r = s.calculateRoute(g, src, dst);
        if (r == null || r.getPath() == null || r.getPath().isEmpty())
            return "[ " + label + " ]\n  No path found.\n\n";
        return "[ " + label + " ]\n"
             + "  Path : " + r.getPath() + "\n"
             + String.format("  Cost : %.2f%n%n", r.getTotalCost());
    }
    private void refreshCombos() {
        List<String> names = new ArrayList<>();
        for (Location l : sortedLocations()) names.add(l.getName());
        List<JComboBox<String>> combos = Arrays.asList(cbSource, cbDest, edgeFromCombo, edgeToCombo);
for (JComboBox<String> cb : combos) {
    String prev = (String) cb.getSelectedItem();
    cb.removeAllItems();
    for (String n : names) cb.addItem(n);
    if (prev != null && names.contains(prev)) cb.setSelectedItem(prev);
} 
        if (tableModel != null) refreshTable();
        if (graphPanel != null) graphPanel.repaint();
    }
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Location loc : graph.getLocations()) {
            List<Edge> edges = graph.getEdges(loc);
            if (edges == null) continue;
            for (Edge e : edges) {
                tableModel.addRow(new Object[]{
                    e.getSource().getName(),
                    e.getDestination().getName(),
                    String.format("%.1f", e.getDistance()),
                    String.format("%.1f", e.getTime()),
                    String.format("%.1f", e.getTraffic())
                });
            }
        }
    }
    private List<Location> sortedLocations() {
        List<Location> list = new ArrayList<>(graph.getLocations());
        list.sort(Comparator.comparing(Location::getName));
        return list;
    }
    private Location findByName(String name) {
        return graph.getLocations().stream()
               .filter(l -> l.getName().equalsIgnoreCase(name))
               .findFirst().orElse(null);
    }
    private void status(String msg, Color color) {
        lblStatus.setText("  " + msg);
        lblStatus.setForeground(color);
    }
    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(8));
        return p;
    }
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        return l;
    }
    private JComboBox<String> styledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        styleCombo(cb);
        return cb;
    }
  private void styleCombo(JComboBox<String> cb) {
    cb.setBackground(BG_INPUT);
    cb.setForeground(TEXT_PRIMARY);
    cb.setFont(new Font("Monospaced", Font.PLAIN, 12));
    cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    cb.setAlignmentX(LEFT_ALIGNMENT);
    cb.setOpaque(true);
    cb.setEditable(true);
cb.setEditor(new BasicComboBoxEditor() {
    private JLabel label = new JLabel();
    {
        label.setForeground(Color.BLACK);   
        label.setBackground(Color.WHITE);  
        label.setFont(new Font("Monospaced", Font.PLAIN, 12));
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(0, 8, 0, 0));
    }
    public Component getEditorComponent() {
        return label;
    }
    public void setItem(Object o) {
        label.setText(o == null ? "" : o.toString());
    }
    public Object getItem() {
        return label.getText();
    }
});
    cb.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            lbl.setBackground(isSelected ? ACCENT_TEAL.darker() : BG_INPUT);
            lbl.setForeground(isSelected ? BG_DARK : TEXT_PRIMARY);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            lbl.setOpaque(true);
            return lbl;
        }
    });
}
    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setForeground(TEXT_MUTED);
        tf.setBackground(BG_INPUT);
        tf.setCaretColor(ACCENT_TEAL);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT_PRIMARY); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(TEXT_MUTED); }
            }
        });
        return tf;
    }
    private JButton accentButton(String text, Color accent) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(accent.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(accent.brighter());
                } else {
                    g2.setColor(accent);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(BG_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setForeground(BG_DARK);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setPreferredSize(new Dimension(340, 34));
        return b;
    }
    private void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("Monospaced", Font.PLAIN, 11));
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(BG_INPUT);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 11));
        table.setSelectionBackground(ACCENT_TEAL.darker());
        table.setSelectionForeground(BG_DARK);
        table.setShowGrid(true);
    }
    private class GraphPanel extends JPanel {
        private List<Location>     highlightPath;
        private Map<Location, Point> positions;
        private static final int   NODE_R = 18;
        GraphPanel() {
            setBackground(BG_DARK);
            setToolTipText("");
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    Location hov = nodeAt(e.getPoint());
                    setToolTipText(hov == null ? null : hov.getName());
                }
            });
        }
        void setHighlightPath(List<Location> path) {
            this.highlightPath = path;
            repaint();
        }
        private Map<Location, Point> computePositions() {
            Map<Location, Point> map = new LinkedHashMap<>();
            List<Location> locs = sortedLocations();
            int n = locs.size();
            if (n == 0) return map;
            int cx = getWidth() / 2, cy = getHeight() / 2;
            double radius = Math.min(cx, cy) * 0.72;
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n - Math.PI / 2;
                int x = (int) (cx + radius * Math.cos(angle));
                int y = (int) (cy + radius * Math.sin(angle));
                map.put(locs.get(i), new Point(x, y));
            }
            return map;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getWidth() == 0) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            positions = computePositions();
            if (positions.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
                g2.drawString("No locations loaded.", getWidth()/2 - 80, getHeight()/2);
                return;
            }
            g2.setStroke(new BasicStroke(1.4f));
            for (Location loc : graph.getLocations()) {
                List<Edge> edges = graph.getEdges(loc);
                if (edges == null) continue;
                Point from = positions.get(loc);
                if (from == null) continue;
                for (Edge e : edges) {
                    Point to = positions.get(e.getDestination());
                    if (to == null) continue;
                    g2.setColor(BORDER_COLOR.brighter());
                    drawArrow(g2, from, to);
                }
            }
            if (highlightPath != null && highlightPath.size() > 1) {
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(ACCENT_TEAL);
                for (int i = 0; i < highlightPath.size() - 1; i++) {
                    Point a = positions.get(highlightPath.get(i));
                    Point b = positions.get(highlightPath.get(i + 1));
                    if (a != null && b != null) drawArrow(g2, a, b);
                }
            }
            for (Map.Entry<Location, Point> entry : positions.entrySet()) {
                Location loc = entry.getKey();
                Point    p   = entry.getValue();
                boolean onPath = highlightPath != null && highlightPath.contains(loc);
                Color fill = onPath ? ACCENT_TEAL : BG_CARD;
                Color border = onPath ? ACCENT_TEAL.brighter() : new Color(60, 80, 120);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillOval(p.x - NODE_R + 2, p.y - NODE_R + 2, NODE_R * 2, NODE_R * 2);
                g2.setColor(fill);
                g2.fillOval(p.x - NODE_R, p.y - NODE_R, NODE_R * 2, NODE_R * 2);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(border);
                g2.drawOval(p.x - NODE_R, p.y - NODE_R, NODE_R * 2, NODE_R * 2);
                g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String name = loc.getName();
                int tx = p.x - fm.stringWidth(name) / 2;
                int ty = p.y + NODE_R + 18;
                g2.setColor(new Color(20, 30, 50, 180));
                g2.fillRoundRect(tx - 4, ty - fm.getAscent(), fm.stringWidth(name) + 8, fm.getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(name, tx, ty);
                g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                String idStr = String.valueOf(loc.getId());
                g2.setColor(onPath ? BG_DARK : TEXT_MUTED);
                g2.drawString(idStr, p.x - g2.getFontMetrics().stringWidth(idStr)/2, p.y + 4);
            }
        }
        private void drawArrow(Graphics2D g2, Point from, Point to) {
            double dx = to.x - from.x, dy = to.y - from.y;
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len < 1) return;
            double ux = dx/len, uy = dy/len;
            // shorten line to node edge
            int x1 = (int)(from.x + ux * NODE_R);
            int y1 = (int)(from.y + uy * NODE_R);
            int x2 = (int)(to.x   - ux * NODE_R);
            int y2 = (int)(to.y   - uy * NODE_R);
            g2.drawLine(x1, y1, x2, y2);
            double angle = Math.atan2(dy, dx);
            int aw = 9;
            int[] ax = { x2, (int)(x2 - aw*Math.cos(angle-0.4)), (int)(x2 - aw*Math.cos(angle+0.4)) };
            int[] ay = { y2, (int)(y2 - aw*Math.sin(angle-0.4)), (int)(y2 - aw*Math.sin(angle+0.4)) };
            g2.fillPolygon(ax, ay, 3);
        }
        private Location nodeAt(Point p) {
            if (positions == null) return null;
            for (Map.Entry<Location, Point> e : positions.entrySet()) {
                Point np = e.getValue();
                if (p.distance(np) <= NODE_R + 4) return e.getKey();
            }
            return null;
        }
    }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(NavigationGUI::new);
    }
}