/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.shapedrawer;

/**
 *
 * @author Administrator
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class ShapeDrawer extends JFrame {
    private String selectedShape = "Rectangle";
    private boolean isFilled = false;
    private String selectedStyle = "Solid";
    private Color selectedColor = Color.RED;

    private final Stack<Shape> shapes = new Stack<>();
    private final Stack<Shape> redoStack = new Stack<>();
    private Shape previewShape = null;

    public ShapeDrawer() {
        setTitle("Shape Drawer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        String[] shapesOptions = {"Rectangle", "Oval", "Triangle", "Pencil", "Line", "Eraser"};
        JComboBox<String> shapeSelector = new JComboBox<>(shapesOptions);
        shapeSelector.addActionListener(e -> selectedShape = (String) shapeSelector.getSelectedItem());
        controlPanel.add(shapeSelector);

        String[] styleOptions = {"Solid", "Dotted"};
        JComboBox<String> styleSelector = new JComboBox<>(styleOptions);
        styleSelector.addActionListener(e -> selectedStyle = (String) styleSelector.getSelectedItem());
        controlPanel.add(styleSelector);

        JLabel colorLabel = new JLabel("Color:");
        controlPanel.add(colorLabel);

        JComboBox<String> colorSelector = new JComboBox<>(new String[]{"Red", "Blue", "Green", "Yellow", "Black"});
        colorSelector.addActionListener(e -> {
            switch ((String) colorSelector.getSelectedItem()) {
                case "Red" -> selectedColor = Color.RED;
                case "Blue" -> selectedColor = Color.BLUE;
                case "Green" -> selectedColor = Color.GREEN;
                case "Yellow" -> selectedColor = Color.YELLOW;
                case "Black" -> selectedColor = Color.BLACK;
            }
        });
        controlPanel.add(colorSelector);

        JCheckBox fillCheckbox = new JCheckBox("Fill");
        fillCheckbox.addActionListener(e -> isFilled = fillCheckbox.isSelected());
        controlPanel.add(fillCheckbox);

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            if (!shapes.isEmpty()) {
                redoStack.push(shapes.pop());
                repaint();
            }
        });
        controlPanel.add(undoButton);

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> {
            if (!redoStack.isEmpty()) {
                shapes.push(redoStack.pop());
                repaint();
            }
        });
        controlPanel.add(redoButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            shapes.clear();
            redoStack.clear();
            repaint();
        });
        controlPanel.add(clearButton);

        add(controlPanel, BorderLayout.NORTH);

        DrawingPanel drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private class DrawingPanel extends JPanel {
        private Point startPoint, endPoint;

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endPoint = e.getPoint();
                    if (!selectedShape.equals("Pencil") && !selectedShape.equals("Eraser")) {
                        shapes.push(new Shape(selectedShape, selectedStyle, selectedColor, startPoint, endPoint, isFilled));
                        redoStack.clear(); // Clear redo stack on new action
                        previewShape = null;
                    }
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selectedShape.equals("Pencil") || selectedShape.equals("Eraser")) {
                        endPoint = e.getPoint();
                        Color color = selectedShape.equals("Eraser") ? getBackground() : selectedColor;

                        // Set a larger stroke width for the eraser
                        int strokeWidth = selectedShape.equals("Eraser") ? 20 : 1;

                        // Use the pencil drawing for both pencil and eraser, but with different stroke widths
                        shapes.push(new Shape("Pencil", "Solid", color, startPoint, endPoint, false, strokeWidth));
                        redoStack.clear(); // Clear redo stack on new action
                        startPoint = endPoint;
                    } else {
                        endPoint = e.getPoint();
                        previewShape = new Shape(selectedShape, selectedStyle, selectedColor, startPoint, endPoint, isFilled);
                    }
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            for (Shape shape : shapes) {
                drawShape(g2d, shape);
            }

            // Draw the preview shape if it exists
            if (previewShape != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Set transparency for preview
                drawShape(g2d, previewShape);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset transparency
            }
        }

        private void drawShape(Graphics2D g2d, Shape shape) {
            g2d.setColor(shape.color);
            g2d.setStroke(shape.style.equals("Dotted")
                    ? new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0)
                    : new BasicStroke(shape.strokeWidth));

            int x = Math.min(shape.start.x, shape.end.x);
            int y = Math.min(shape.start.y, shape.end.y);
            int width = Math.abs(shape.start.x - shape.end.x);
            int height = Math.abs(shape.start.y - shape.end.y);

            switch (shape.type) {
                case "Rectangle" -> {
                    if (shape.filled) {
                        g2d.fillRect(x, y, width, height);
                    } else {
                        g2d.drawRect(x, y, width, height);
                    }
                }
                case "Oval" -> {
                    if (shape.filled) {
                        g2d.fillOval(x, y, width, height);
                    } else {
                        g2d.drawOval(x, y, width, height);
                    }
                }
                case "Triangle" -> {
                    int[] xPoints = {shape.start.x, shape.end.x, (shape.start.x + shape.end.x) / 2};
                    int[] yPoints = {shape.start.y, shape.end.y, Math.min(shape.start.y, shape.end.y)};
                    if (shape.filled) {
                        g2d.fillPolygon(xPoints, yPoints, 3);
                    } else {
                        g2d.drawPolygon(xPoints, yPoints, 3);
                    }
                }
                case "Line" -> g2d.drawLine(shape.start.x, shape.start.y, shape.end.x, shape.end.y);
                case "Pencil" -> g2d.drawLine(shape.start.x, shape.start.y, shape.end.x, shape.end.y);
            }
        }
    }

    private static class Shape {
        String type, style;
        Color color;
        Point start, end;
        boolean filled;
        int strokeWidth;

        public Shape(String type, String style, Color color, Point start, Point end, boolean filled) {
            this.type = type;
            this.style = style;
            this.color = color;
            this.start = start;
            this.end = end;
            this.filled = filled;
            this.strokeWidth = 1; // Default stroke width is 1 for non-pencil shapes
        }

        public Shape(String type, String style, Color color, Point start, Point end, boolean filled, int strokeWidth) {
            this(type, style, color, start, end, filled);
            this.strokeWidth = strokeWidth; // This is used for pencil and eraser
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShapeDrawer::new);
    }
}
